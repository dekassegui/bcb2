package neo;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import javax.xml.stream.*;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

import static neo.SafeDateFormat.*;

/**
 * Extensão do cliente do web service SGS do BCB para extração de dados dos
 * documentos XML.
*/
public class ClienteBCB extends BCBCliente
{
  private static Logger logger = Logger.getLogger(ClienteBCB.class);

  /** Fornecedor único de StAX parsers. */
  private XMLInputFactory inputFactory;

  private SafeDateFormat dateFormat;

  public ClienteBCB()
  {
    super();

    try {
      inputFactory = XMLInputFactory.newInstance();
      inputFactory.setProperty(
        XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
      inputFactory.setProperty(
        XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    } catch (FactoryConfigurationError e) {
      logger.error(e.toString());
    }

    dateFormat = new SafeDateFormat();
  }

  /**
   * Monta texto com todos os dados extraídos de xml retornado por
   * <b>getUltimoValorXML</b> sem modificar seus formatos.
   *
   * @param xml XML retornado por <b>getUltimoValorXML</b>.
   * @return O texto montado se o xml for adequado senão <b>null</b>.
  */
  public String extraiResumo(String xml)
  {
    StringBuilder sb = null;

    if (xml != null)
    {
      final String separator = ": ";

      sb = new StringBuilder();
      try {

        // combina parser ordinário com filtro de exclusão
        XMLStreamReader rd = inputFactory.createFilteredReader(
          inputFactory.createXMLStreamReader(
            /* Workaround para XML mal formado e.g.: BM&F -> código = 4 */
            new StringReader( xml.replaceAll(BAD_ENTITY, FIX_BAD_ENTITY) )),
          new ExcludeFilter("resposta", "SERIE"));

        // apêndice das tags NOME, CODIGO, PERIODICIDADE e UNIDADE
        do {
          sb.append(rd.getLocalName()).append(separator)
            .append(rd.getElementText()).append(NEWLINE);
          rd.next();
        } while (!rd.getLocalName().equals("DATA"));

        // apêndice das tags DATA e DATA-FIM
        do {
          sb.append(rd.getLocalName()).append(separator);
          rd.next();
          sb.append(rd.getElementText()); // DIA
          rd.next();
          sb.append(DATE_FIELDS_SEPARATOR).append(rd.getElementText()); // MES
          rd.next();
          sb.append(DATE_FIELDS_SEPARATOR).append(rd.getElementText()); // ANO
          rd.next();
          sb.append(NEWLINE);
          rd.next();
        } while (!rd.getLocalName().equals("VALOR"));

        // apêndice da tag VALOR
        sb.append(rd.getLocalName()).append(separator)
          .append(rd.getElementText());

        rd.close();   // libera resources

      } catch(XMLStreamException e) {
        logger.error(e.toString());
      }
    }

    return sb != null ? sb.toString() : null;
  }

  /**
   * Monta objeto encapsulando código, data(s) e valor, extraídos de xml
   * retornado por <b>getUltimoValorXML</b>.
   *
   * @param xml XML retornado por <b>getUltimoValorXML</b>.
   * @return O objeto montado se o xml for adequado senão <b>null</b>.
  */
  public VO extraiUltimoValor(String xml)
  {
    VO vo = null;

    if (xml != null)
    {
      try {

        // combina parser ordinário com fitro de exclusão
        XMLStreamReader rd = inputFactory.createFilteredReader(
          inputFactory.createXMLStreamReader(
            new StringReader( xml.replaceAll(BAD_ENTITY, FIX_BAD_ENTITY) )),
          new ExcludeFilter(
            "resposta", "SERIE", "NOME", "PERIODICIDADE", "UNIDADE"));

        // extrai o código da série
        String sCodigo = rd.getElementText();
        rd.next();

        // extrai as datas
        String sDatas[] = new String[] { null, null };
        int j = 0;
        do {
          rd.next();
          // concatena os componentes da string de data
          char[] buf = new char[10];
          int pos = 0;
          for (int i = 0; i < 3; ++i) {
            char[] s = rd.getElementText().toCharArray();
            rd.next();
            System.arraycopy(s, 0, buf, pos, s.length);
            pos += s.length;
            if (i < 2) buf[pos++] = DATE_FIELDS_SEPARATOR;
          }
          // finaliza a montagem da string de data
          sDatas[j++] = new String(buf, 0, pos);
          rd.next();
        } while (!rd.getLocalName().equals("VALOR"));

        // extrai o valor
        String sValor = rd.getElementText();

        rd.close();

        // monta o objeto
        vo = new VO(sCodigo, sDatas[0], sDatas[1], sValor);

      } catch(XMLStreamException e) {
        logger.error(e.toString());
      }
    }

    return vo;
  }

  /**
   * Monta array de arrays de objetos, cada objeto encapsulando: código, data(s)
   * e valor, extraídos de xml retornado por <b>getValoresSeriesXML</b>.
   * Os arrays de objetos nem sempre terão tamanhos iguais e "zero" é um valor
   * possível, pois observações que contém "valor" nulo serão rejeitadas.
   *
   * @param xml XML retornado por <b>getValoresSeriesXML</b>.
   * @return O array de arrays de objetos montado.
  */
  public VO[][] extraiSeries(String xml)
  {
    List<VO[]> LISTA = new ArrayList<VO[]>();

    if (xml != null)
    {
      try {

        List<VO> lista = new ArrayList<VO>();

        // combina parser ordinário com filtro de exclusão
        XMLStreamReader rd = inputFactory.createFilteredReader(
          inputFactory.createXMLStreamReader(new StringReader(xml)),
          new ExcludeFilter("SERIES", "ITEM", "BLOQUEADO"));

        do {
          // extrai o código da série
          String sCodigo = rd.getAttributeValue(null, "ID");

          // extrai datas e valores da série
          while (rd.next() == XMLStreamConstants.START_ELEMENT
                 && rd.getLocalName().equals("DATA"))
          {
            // extrai as datas completando campo dia se necessário
            String[] sDatas = new String[] { null, null };
            for (int j = 0; !rd.getLocalName().equals("VALOR"); ++j)
            {
              sDatas[j] = dateFormat.complete(rd.getElementText());
              rd.next();
            }

            // extrai o valor da série
            String sValor = rd.getElementText();

            // aceita os dados se valor não for string vazia
            if (sValor.length() > 0) {
              // monta e adiciona o objeto à lista
              VO vo = new VO(sCodigo, sDatas[0], sDatas[1], sValor);
              lista.add(vo);
            }
          }

          // monta e adiciona o array de objetos à LISTA
          VO[] array = lista.toArray( new VO[ lista.size() ] );
          lista.clear();
          LISTA.add( array );

          rd.next();
        } while (rd.isStartElement() && rd.getLocalName().equals("SERIE"));

        rd.close();

      } catch (XMLStreamException e) {
        logger.error(e.toString());
      }
    }

    // monta o array de arrays de objetos
    VO[][] array = LISTA.toArray( new VO[ LISTA.size() ][] );
    LISTA.clear();

    return array;
  }

  /**
   * Monta objeto com dados de observação de série temporal conforme código
   * e data fornecidos.
   * A observação é extraida de xml retornado por "getValoresSeriesXML" para
   * também contemplar séries especiais que contém o campo DATAFIM.
   *
   * @param codigo Código numérico da série temporal.
   * @param data String da Data no formato dd/MM/aaaa
   * @return O objeto montado se a pesquisa for bem sucedida, senão "null".
  */
  public VO extraiValor(long codigo, String data)
  {
    VO v = null;
    String xml = getValoresSeriesXML(new long[] { codigo }, data, data);
    if (xml != null) {
      VO[] array = extraiSeries(xml)[0];
      if (array.length > 0) v = array[0];
    }
    return v;
  }

  /** Consulta o web service via linha de comando. */
  public static void main(String[] args)
  {
    final PrintStream out = System.out;
    int n = args.length;
    if (n == 0) {
      out.format("%nPossíveis argumentos:%n%n");
      out.println("  (1): código");
      out.println("  (2): código data");
      out.println("  (3): código(s) data_inicial data_final");
      out.format("%nFormatos de datas:%n%n");
      out.println("  (1): dd/MM/aaaa");
      out.println("  (2): MM/aaaa");
      out.format("%nO formato (2) equivale a 01/MM/aaaa%n%n");
    } else {
      final String noDataToPrint = "No data to print.";
      final String invalidDate = "Erro: Data inválida.";
      // instancia o cliente do web service
      ClienteBCB c = new ClienteBCB();
      if (n == 1) {
        long codigo = Long.parseLong(args[0]);
        String xml = c.getUltimoValorXML(codigo);
        if (xml != null) {
          // extrai/imprime informação resumida sobre a série temporal
          String text = c.extraiResumo(xml);
          out.println(text);
        } else {
          out.println(noDataToPrint);
        }
      } else if (n == 2) {
        long codigo = Long.parseLong(args[0]);
        String data = args[1].replaceAll(MES_ANO, FIX_MES_ANO);
        if (c.dateFormat.isValid(data)) {
          VO w = c.extraiValor(codigo, data);
          if (w != null) {
            out.println( w.toString() );
          } else {
            out.println(noDataToPrint);
          }
        } else {
          out.println(invalidDate);
        }
      } else {
        // monta/preenche o array de códigos
        long codigos[] = new long[n-2];
        for (int i = 0; i < n-2; ++i) codigos[i] = Long.parseLong(args[i]);
        // obtem as datas inicial/final do intervalo a pesquisar
        String data_ini = args[n-2].replaceAll(MES_ANO, FIX_MES_ANO);
        String data_fim = args[n-1].replaceAll(MES_ANO, FIX_MES_ANO);
        if (c.dateFormat.isValid(data_ini) && c.dateFormat.isValid(data_fim)) {
          // extrai o array das séries do xml retornado
          String xml = c.getValoresSeriesXML(codigos, data_ini, data_fim);
          if (xml != null) {
            ClienteBCB.VO series[][] = c.extraiSeries(xml);
            // loop de impressão das séries na ordem requisitada
            int k = 0;
            for (long codigo : codigos) {
              if (1 < ++k) out.println();
              // pesquisa a ordem da série temporal
              int rank = 0;
              while (rank < codigos.length
                     && (series[rank].length == 0
                         || series[rank][0].getCodigo() != codigo)) ++rank;
              if (rank < codigos.length) {
                // imprime a série temporal
                for (ClienteBCB.VO v : series[rank]) {
                  out.println(v.toString());
                }
              } else {
                out.format("%03d: %s%n", codigo, noDataToPrint);
              }
            }
          } else {
            out.println(noDataToPrint);
          }
        } else {
          out.println(invalidDate);
          System.exit(1);
        }
      }
    }
  }

  /** Representação de observação individual de série temporal. */
  public class VO
  {
    protected long codigo;      // código da série temporal

    protected Date data;        // data da observação ou do inicio de intervalo

    protected Date dataFim;     // data do fim de intervalo

    protected BigDecimal valor; // representação numérica precisa do valor

    /**
     * @param sCodigo String do código da série temporal.
     * @param sData String da data da observação ou do inicio de intervalo.
     * @param sDataFim String da data do fim de intervalo.
     * @param sValor String do valor numérico.
    */
    public VO(String sCodigo, String sData, String sDataFim, String sValor)
    {
      codigo = Long.parseLong(sCodigo);
      try {
        data = dateFormat.parse(sData);
        if (sDataFim != null) dataFim = dateFormat.parse(sDataFim);
      } catch (ParseException e) {
        logger.error(e.toString());
      }
      if (sValor.indexOf('.') >= 0 && sValor.indexOf(',') >= 0)
       sValor = sValor.replaceAll("\\.", "");
      valor = new BigDecimal(sValor.replace(',', '.'));
    }

    public VO(String sCodigo, String sData, String sValor)
    {
      this(sCodigo, sData, null, sValor);
    }

    /**
     * @return Código da série temporal.
    */
    public long getCodigo() { return codigo; }

    /**
     * @return Data da observação ou do inicio de intervalo.
    */
    public Date getData() { return data; }

    /**
     * @return Data do fim de intervalo.
    */
    public Date getDataFim() { return dataFim; }

    /**
     * @return Representação numérica precisa do valor.
    */
    public BigDecimal getValor() { return valor; }

    /**
     * @return Representação textual da observação.
    */
    public String toString()
    {
      return (dataFim == null) ? String.format(fmt3, codigo, data, valor)
        : String.format(fmt4, codigo, data, dataFim, valor);
    }

    public static final String
      fmt3 = "%1$03d %2$td/%2$tm/%2$tY %3$7.4f";

    public static final String
      fmt4 = "%1$03d %2$td/%2$tm/%2$tY %3$td/%3$tm/%3$tY %4$7.4f";
  }

  /** Filtro para exclusão de elementos no StAX Parser. */
  private class ExcludeFilter implements StreamFilter
  {
    /** Array dos nomes de elementos a excluir ordenado em ordem crescente. */
    private String[] tagNames;

    /**
     * Construtor da classe visando performance otimizada.
     *
     * @param tagNames Array contendo os nomes de elementos a excluir.
    */
    public ExcludeFilter(String ... names)
    {
      tagNames = names;
      Arrays.sort(this.tagNames);
    }

    /**
     * Verifica exclusão do elemento sob o cursor no StAX Parser.
     *
     * @param rd StAX Parser de documento XML.
     * @return Status de exclusão do elemento sob o cursor.
    */
    public boolean accept(XMLStreamReader rd)
    {
      return rd.hasName()
             && (Arrays.binarySearch(tagNames, rd.getLocalName()) < 0);
    }
  }

  /** Pattern para pesquisar XML entity mal declarada. */
  public static final String BAD_ENTITY = "&([^;\\W]*([^;\\w]|$))";

  /** Pattern de substituição de XML entity mal declarada. */
  public static final String FIX_BAD_ENTITY = "&amp;$1";
}
