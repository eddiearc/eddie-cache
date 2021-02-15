package pro.eddiecache.ioc.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IoCEntityResolver implements EntityResolver
{

	private static final String DTD_URL = "http://www.cachekit.com/beans.dtd";
	private static final String DTD_FILE = "/pro/eddiecache/ioc/beans/beans.dtd";

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
	{

		if (DTD_URL.equals(systemId))
		{
			InputStream stream = IoCEntityResolver.class.getResourceAsStream(DTD_FILE);
			return new InputSource(stream);
		}
		else
		{
			return null;
		}
	}

}
