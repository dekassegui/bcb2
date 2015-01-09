package neo;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import static neo.SafeDateFormat.MES_ANO;
import static neo.SafeDateFormat.FIX_MES_ANO;

/**
 * Cliente do web service SGS do BCB.
*/
public class BCBCliente
{
  static Logger logger = Logger.getLogger(BCBCliente.class);

  /** Endereço relativo da wsdl no jarfile. */
  static private final String wsdl = "resources/bcb.wsdl";

  /** web service transport */
  static private final String tns = "https://www3.bcb.gov.br/wssgs/services/FachadaWSSGS";

  static private final String serviceName = "FachadaWSSGSService";

  static private final String portName = "FachadaWSSGS";

  private RPCServiceClient cliente;

  /** Array dos tipos dos valores de retorno. */
  @SuppressWarnings("rawtypes")
  private Class types[] = new Class[1];

  public BCBCliente()
  {
    // verifica se o logger foi configurado no acionamento
    if (System.getProperty("log4j.configuration") == null) {
      final String defFilename = "cfg/log4j.properties";
      URL url = null;
      try {
        // carrega config de arquivo local ou como resource
        File f = new File(defFilename);
        url = f.exists() ? f.toURI().toURL()
          : this.getClass().getClassLoader().getResource(defFilename);
      } catch (MalformedURLException e) {
        System.err.println(e.toString());
      }
      PropertyConfigurator.configure(url);
    }

    Properties p = new Properties();
    try {
      final String defFilename = "cfg/bcb.properties";
      File f = new File(defFilename);
      InputStream in = f.exists() ? new FileInputStream(f)
        : this.getClass().getClassLoader().getResourceAsStream(defFilename);
      p.load(in);
      in.close();
    } catch (IOException e) {
      logger.error(e.getMessage());
    }

    final QName q = new QName(tns, serviceName);

    final URL u = this.getClass().getClassLoader().getResource(wsdl);

    try {

      cliente = new RPCServiceClient(null, u, q, portName);

      Options option = cliente.getOptions();

      // Let them know we are here.
      option.setProperty(HTTPConstants.USER_AGENT, "neo.BCBCliente");

      // desabilita "chunking" a tempo de execução
      option.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);

      // notifica o engine para reutilizar o http client entre chamadas
      option.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Boolean.TRUE);

      String s = p.getProperty("ws.socket.timeout");
      if (s != null) {
        int tm = Integer.parseInt(s);
        option.setProperty(HTTPConstants.SO_TIMEOUT, tm);
      }

      s = p.getProperty("ws.connection.timeout");
      if (s != null) {
        int tm = Integer.parseInt(s);
        option.setProperty(HTTPConstants.CONNECTION_TIMEOUT, tm);
      }

      s = p.getProperty("ws.blocking.timeout");
      if (s != null) {
        int tm = Integer.parseInt(s);
        option.setTimeOutInMilliSeconds(tm);
      }

      // tipo do único valor retornado
      types[0] = Class.forName("java.lang.String");

    } catch (ClassNotFoundException | AxisFault e) {
      logger.error(e.toString());
    }

  }

  /**
   * Realiza a chamada ao procedimento remoto (aka RPC).
   *
   * @param procedure QName do procedimento remoto.
   * @param parameters Array dos parâmetros do procedimento.
   * @return String contendo documento XML ou valor numérico se a requisição foi
   *         atendida com sucesso ou "null" em caso contrário.
  */
  private String invoke(QName procedure, Object[] parameters)
  {
    String result = null;
    try {
      result = (String) cliente.invokeBlocking(procedure, parameters, types)[0];
    } catch (AxisFault e) {
      StringBuilder sb = new StringBuilder(e.getMessage());
      sb.append(NEWLINE).append(NEWLINE).append("Isto ocorreu no \"")
        .append(Thread.currentThread().getStackTrace()[1].getMethodName())
        .append("\" com parâmetros:").append(NEWLINE).append(NEWLINE)
        .append("\t procedure: ").append(procedure.getLocalPart())
        .append(NEWLINE).append("\tparameters:");
      for (Object par : parameters) {
        sb.append(' ');
        if (par instanceof String) {
          sb.append((String) par);
        } else if (par instanceof Long) {
          sb.append((Long) par);
        } else if (par.getClass().isArray()) {
          sb.append(Arrays.toString((long[]) par));
        } else {
          sb.append("UNKNOWN");
        }
      }
      sb.append(NEWLINE);
      logger.error(sb.toString());
      /*
        Importante: Há procedimentos que não retornam "null" indiferentemente
                    a ocorrência de excessões.
      */
      result = null;
    }
    return result;
  }

  /**
   * Encaminha chamada ao procedimento remoto de mesmo nome.
   *
   * @param code Código numérico da série temporal pesquisada.
   * @return String contendo o documento XML resultante da chamada.
  */
  public String getUltimoValorXML(long code)
  {
    // prepara o array dos parâmetros
    Object parameters[] = new Object[] { code };
    // retorna documento XML resultante do rpc
    return invoke(new QName(tns, "getUltimoValorXML"), parameters);
  }

  /** Alias do método <b>getUltimoValorXML</b>. */
  public String get(long code)
  {
    return getUltimoValorXML(code);
  }

  /**
   * Encaminha chamada ao procedimento remoto de mesmo nome.
   *
   * @param code Código numérico da série temporal pesquisada.
   * @param data Data no formato dd/MM/yyyy
   * @return String contendo o valor numérico de observação da série na data.
  */
  public String getValor(long code, String data)
  {
    Object[] parameters = new Object[] { code, data };
    return invoke(new QName(tns, "getValor"), parameters);
  }

  /** Alias do método <b>getValor</b>. */
  public String get(long code, String data)
  {
    return getValor(code, data);
  }

  /**
   * Encaminha chamada ao procedimento remoto de mesmo nome.
   *
   * @param code Código numérico da série temporal pesquisada.
   * @param dataInicial Data inicial de intervalo no formato dd/MM/yyyy.
   * @param dataFinal Data final de intervalo no formato dd/MM/yyyy.
   * @return String contendo o valor numérico de observação da série na data.
  */
  public String getValorEspecial(long code, String dataInicial, String dataFinal)
  {
    Object[] parameters = new Object[] { code, dataInicial, dataFinal };
    return invoke(new QName(tns, "getValorEspecial"), parameters);
  }

  /** Alias do método <b>getValorEspecial</b>. */
  public String get(long code, String dataInicial, String dataFinal)
  {
    return getValorEspecial(code, dataInicial, dataFinal);
  }

  /**
   * Encaminha chamada ao procedimento remoto de mesmo nome.
   *
   * Importante: No documento XML retornado, as séries podem estar em ordem
   *             diferente da ordem original dos códigos no array "codes".
   *
   * @param codes Array de códigos numéricos das séries temporais pesquisadas.
   * @param dataInicial Data inicial de intervalo no formato dd/MM/yyyy.
   * @param dataFinal Data final de intervalo no formato dd/MM/yyyy.
   * @return String contendo o valor numérico de observação da série na data.
  */
  public String getValoresSeriesXML(long codes[], String dataInicial, String dataFinal)
  {
    Object[] parameters = new Object[] { codes, dataInicial, dataFinal };
    return invoke(new QName(tns, "getValoresSeriesXML"), parameters);
  }

  /** Alias do método <b>getValoresSeriesXML</b>. */
  public String get(long codes[], String dataInicial, String dataFinal)
  {
    return getValoresSeriesXML(codes, dataInicial, dataFinal);
  }

  /** Consulta ao web service via linha de comando. */
  public static void main(String[] args)
  {
    final java.io.PrintStream out = System.out;
    int n = args.length;
    if (n == 0) {
      out.format("%nPossíveis argumentos:%n%n");
      out.println("  (1): código");
      out.println("  (2): código data");
      out.println("  (3): *código data_inicial data_final");
      out.println("  (4): código(s) data_inicial data_final");
      out.format("%nEm (3) prefixe o código para invocar \"GetValorEspecial\".%n");
      out.format("%nFormatos de datas:%n%n");
      out.println("  (1): dd/MM/aaaa");
      out.println("  (2): MM/aaaa");
      out.format("%nO formato (2) equivale a 01/MM/aaaa%n%n");
    } else {
      BCBCliente c = new BCBCliente();
      if (n == 1) {
        long codigo = Long.parseLong(args[0]);
        String xml = c.getUltimoValorXML(codigo);
        out.println(xml);
      } else if (n == 2) {
        long codigo = Long.parseLong(args[0]);
        String data = args[1].replaceAll(MES_ANO, FIX_MES_ANO);
        String valor = c.getValor(codigo, data);
        out.println(valor);
      } else if (n == 3 && args[0].matches("^\\D\\d+$")) {
        long codigo = Long.parseLong(args[0].substring(1));
        String data_ini = args[1].replaceAll(MES_ANO, FIX_MES_ANO);
        String data_fim = args[2].replaceAll(MES_ANO, FIX_MES_ANO);
        String valor = c.getValorEspecial(codigo, data_ini, data_fim);
        out.println(valor);
      } else {
        long codigos[] = new long[n-2];
        for (int i = 0; i < n-2; ++i) {
          codigos[i] = Long.parseLong(args[i]);
        }
        String data_ini = args[n-2].replaceAll(MES_ANO, FIX_MES_ANO);
        String data_fim = args[n-1].replaceAll(MES_ANO, FIX_MES_ANO);
        String xml = c.getValoresSeriesXML(codigos, data_ini, data_fim);
        out.println(xml);
      }
    }
  }

  public static final String NEWLINE = System.getProperty("line.separator");
}
