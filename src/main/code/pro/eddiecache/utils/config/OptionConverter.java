package pro.eddiecache.utils.config;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OptionConverter
{
	private static final Log log = LogFactory.getLog(OptionConverter.class);

	private static final String DELIM_START = "${";

	private static final char DELIM_STOP = '}';

	private static final int DELIM_START_LEN = 2;

	private static final int DELIM_STOP_LEN = 1;

	private OptionConverter()
	{
		super();
	}

	public static String[] concatanateArrays(String[] left, String[] right)
	{
		int len = left.length + right.length;
		String[] a = new String[len];

		System.arraycopy(left, 0, a, 0, left.length);
		System.arraycopy(right, 0, a, left.length, right.length);

		return a;
	}

	public static String convertSpecialChars(String str)
	{
		char c;
		int len = str.length();
		StringBuilder sb = new StringBuilder(len);

		int i = 0;
		while (i < len)
		{
			c = str.charAt(i++);
			if (c == '\\')
			{
				c = str.charAt(i++);
				if (c == 'n')
				{
					c = '\n';
				}
				else if (c == 'r')
				{
					c = '\r';
				}
				else if (c == 't')
				{
					c = '\t';
				}
				else if (c == 'f')
				{
					c = '\f';
				}
				else if (c == '\b')
				{
					c = '\b';
				}
				else if (c == '\"')
				{
					c = '\"';
				}
				else if (c == '\'')
				{
					c = '\'';
				}
				else if (c == '\\')
				{
					c = '\\';
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static String getSystemProperty(String key, String def)
	{
		try
		{
			return System.getProperty(key, def);
		}
		catch (Throwable e)
		{
			log.debug("Fail to read system property: " + key);
			return def;
		}
	}

	public static <T> T instantiateByKey(Properties props, String key, T defaultValue)
	{
		String className = findAndSubst(key, props);
		if (className == null)
		{
			if (log.isTraceEnabled())
			{
				log.info("Could not find value for key " + key);
			}
			return defaultValue;
		}
		return OptionConverter.instantiateByClassName(className.trim(), defaultValue);
	}

	public static boolean toBoolean(String value, boolean defaultValue)
	{
		if (value == null)
		{
			return defaultValue;
		}
		String trimmedVal = value.trim();
		if ("true".equalsIgnoreCase(trimmedVal))
		{
			return true;
		}
		if ("false".equalsIgnoreCase(trimmedVal))
		{
			return false;
		}
		return defaultValue;
	}

	public static int toInt(String value, int defaultValue)
	{
		if (value != null)
		{
			String s = value.trim();
			try
			{
				return Integer.parseInt(s);
			}
			catch (NumberFormatException e)
			{
				log.error("[" + s + "] is not in proper int form.");
				e.printStackTrace();
			}
		}
		return defaultValue;
	}

	public static long toFileSize(String value, long defaultValue)
	{
		if (value == null)
		{
			return defaultValue;
		}

		String s = value.trim().toUpperCase();
		long multiplier = 1;
		int index;

		if ((index = s.indexOf("KB")) != -1)
		{
			multiplier = 1024;
			s = s.substring(0, index);
		}
		else if ((index = s.indexOf("MB")) != -1)
		{
			multiplier = 1024 * 1024;
			s = s.substring(0, index);
		}
		else if ((index = s.indexOf("GB")) != -1)
		{
			multiplier = 1024 * 1024 * 1024;
			s = s.substring(0, index);
		}
		if (s != null)
		{
			try
			{
				return Long.parseLong(s) * multiplier;
			}
			catch (NumberFormatException e)
			{
				log.error("[" + s + "] is not in proper int form");
				log.error("[" + value + "] not in expected format", e);
			}
		}
		return defaultValue;
	}

	public static String findAndSubst(String key, Properties props)
	{
		String value = props.getProperty(key);
		if (value == null)
		{
			return null;
		}

		try
		{
			return substVars(value, props);
		}
		catch (IllegalArgumentException e)
		{
			log.error("Error occur, option value [" + value + "]", e);
			return value;
		}
	}

	public static <T> T instantiateByClassName(String className, T defaultValue)
	{
		if (className != null)
		{
			try
			{
				Class<?> classObj = Class.forName(className);

				Object o = classObj.newInstance();

				try
				{
					@SuppressWarnings("unchecked")
					T t = (T) o;
					return t;
				}
				catch (ClassCastException e)
				{
					log.error("ClassCastException occur " + className);
					return defaultValue;
				}
			}
			catch (Exception e)
			{
				log.error("Could not instantiate class [" + className + "]", e);
			}
		}
		return defaultValue;
	}

	public static String substVars(String val, Properties props) throws IllegalArgumentException
	{
		StringBuilder sb = new StringBuilder();

		int i = 0;
		int j = 0;
		int k = 0;

		while (true)
		{
			j = val.indexOf(DELIM_START, i);
			if (j == -1)
			{
				if (i == 0)
				{
					return val;
				}
				sb.append(val.substring(i, val.length()));
				return sb.toString();
			}
			sb.append(val.substring(i, j));
			k = val.indexOf(DELIM_STOP, j);
			if (k == -1)
			{
				throw new IllegalArgumentException("Not in proper form");
			}
			j += DELIM_START_LEN;
			String key = val.substring(j, k);
			String replacement = getSystemProperty(key, null);
			if (replacement == null && props != null)
			{
				replacement = props.getProperty(key);
			}

			if (replacement != null)
			{
				sb.append(replacement);
			}
			i = k + DELIM_STOP_LEN;
		}
	}
}
