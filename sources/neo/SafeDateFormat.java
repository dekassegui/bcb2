package neo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Thread Safe SimpleDateFormat as recommended at viralpatel.net.
*/
public class SafeDateFormat
{
  private DateFormat df;

  public SafeDateFormat(String datePattern, Locale locale)
  {
    df = new SimpleDateFormat(datePattern, locale);
    df.setLenient(false);
  }

  public SafeDateFormat(String datePattern)
  {
    this(datePattern, new Locale("pt", "BR"));
  }

  public SafeDateFormat()
  {
    this("dd/MM/yyyy");
  }

  public synchronized String format(Date date)
  {
    return df.format(date);
  }

  public synchronized Date parse(String string) throws ParseException
  {
    return df.parse(string);
  }

  public synchronized boolean isValid(String data)
  {
    Date d = null;
    try {
      d = parse(data);
    } catch (ParseException e) {
      /* */
    }
    return d != null;
  }

  /**
   * Prefixa a string data com '1' + separador de campos de datas se tem menos
   * de 8 caracteres i.e.: se apenas contém: mês com 1 ou 2 dígitos no máximo,
   * separador de campos de datas e ano com 4 dígitos.
   *
   * @param data String representando mês/ano ou dia/mês/ano.
   * @return String de data prefixada ou a original.
  */
  public synchronized String complete(String data)
  {
    int len = data.length();
    if (len > 7) {
      return data;
    } else {
      char[] buf = new char[len+2];
      System.arraycopy(data.toCharArray(), 0, buf, 2, len);
      buf[0] = '1';
      buf[1] = DATE_FIELDS_SEPARATOR;
      return new String(buf);
    }
  }

  public static final char DATE_FIELDS_SEPARATOR = '/';

  /** Regex pattern para identificar data no formato MES/ANO. */
  public static final String MES_ANO = "^(\\d\\d?/\\d{4})$";

  /** Regex pattern de substituição de data incompleta. */
  public static final String FIX_MES_ANO = "1/$1";
}
