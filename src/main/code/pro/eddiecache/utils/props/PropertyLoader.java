package pro.eddiecache.utils.props;

import java.io.InputStream;
import java.util.Properties;

public abstract class PropertyLoader
{

	private static final String SUFFIX = ".conf";

	private static final String SUFFIX_PROPERTIES = ".properties";

	public static Properties loadProperties(String name, ClassLoader loader)
	{
		boolean isConfSuffix = true;

		if (name == null)
		{
			throw new IllegalArgumentException("null input: name");
		}

		ClassLoader classLoader = (loader == null) ? ClassLoader.getSystemClassLoader() : loader;

		String fileName = name.startsWith("/") ? name.substring(1) : name;

		if (fileName.endsWith(SUFFIX))
		{
			fileName = fileName.substring(0, fileName.length() - SUFFIX.length());
		}

		if (fileName.endsWith(SUFFIX_PROPERTIES))
		{
			fileName = fileName.substring(0, fileName.length() - SUFFIX_PROPERTIES.length());
			isConfSuffix = false;
		}

		Properties result = null;

		InputStream in = null;
		try
		{
			fileName = fileName.replace('.', '/');

			if (!fileName.endsWith(SUFFIX) && isConfSuffix)
			{
				fileName = fileName.concat(SUFFIX);
			}
			else if (!fileName.endsWith(SUFFIX_PROPERTIES) && !isConfSuffix)
			{
				fileName = fileName.concat(SUFFIX_PROPERTIES);
			}

			in = classLoader.getResourceAsStream(fileName);

			if (in != null)
			{
				result = new Properties();
				result.load(in);
			}
		}
		catch (Exception e)
		{
			result = null;
		}
		finally
		{
			if (in != null)
				try
				{
					in.close();
				}
				catch (Throwable ignore)
				{

				}
		}

		if (result == null)
		{
			throw new IllegalArgumentException("Could not load [" + fileName + "].");
		}

		return result;
	}

	public static Properties loadProperties(final String name)
	{
		return loadProperties(name, Thread.currentThread().getContextClassLoader());
	}

	private PropertyLoader()
	{
		super();
	}

}
