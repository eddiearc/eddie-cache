package pro.eddiecache.xml;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XmlParser
{

	private static final Log log = LogFactory.getLog(XmlParser.class);

	/**
	 * 通过xml文件获取对应的Properties对象
	 *
	 * @param xmlFile 放置xml文件的位置
	 */
	public static Properties getPropertiesFromXml(String xmlFile)
	{
		InputStream is = XmlParser.class.getResourceAsStream(xmlFile);

		XPathParser parser = new XPathParser(is);

		XNode root = parser.evalNode("/configuration");

		XNode caches = root.evalNode("caches");
		Properties prop = new Properties();
		for (XNode cache : caches.getChildren())
		{

			String id = cache.getStringAttribute("id");

			String prefix = "cachekit." + id;

			XNode cacheKit = cache.evalNode("kit");

			String kitStr = cacheKit.getStringBody();

			if (kitStr == null || kitStr.isEmpty())
			{
				prop.setProperty(prefix, "");
			}
			else
			{
				prop.setProperty(prefix, kitStr);
			}

			XNode cacheattributes = cache.evalNode("cacheattributes");

			String cls = cacheattributes.getStringAttribute("class");

			prop.setProperty(prefix + ".cacheattributes", cls);

			Properties properties = cacheattributes.getChildrenAsProperties();
			addPrefix(properties, prefix + ".cacheattributes", prop);

			XNode elementattributes = cache.evalNode("elementattributes");
			cls = elementattributes.getStringAttribute("class");
			prop.setProperty(prefix + ".elementattributes", cls);

			Properties elementProp = elementattributes.getChildrenAsProperties();
			addPrefix(elementProp, prefix + ".elementattributes", prop);
		}

		XNode kits = root.evalNode("kits");

		for (XNode kit : kits.getChildren())
		{
			String id = kit.getStringAttribute("id");
			String fact = kit.getStringAttribute("class");

			XNode attributes = kit.evalNode("attributes");
			String cls = attributes.getStringAttribute("class");

			prop.setProperty("cachekit.kit." + id, fact);
			prop.setProperty("cachekit.kit." + id + ".attributes", cls);

			Properties properties = attributes.getChildrenAsProperties();
			addPrefix(properties, "cachekit.kit." + id + ".attributes", prop);

		}

		xml2PropertyLogger(prop);
		return prop;
	}

	/**
	 * 将originalPro的属性内容加上前缀prefix，再添加到currentPro中
	 */
	private static void addPrefix(Properties originalPro, String prefix, Properties currentPro)
	{
		Iterator<?> it = originalPro.entrySet().iterator();
		while (it.hasNext())
		{
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			currentPro.setProperty(prefix + "." + key, value);
		}
	}

	/**
	 * log记录
	 */
	private static void xml2PropertyLogger(Properties properties)
	{
		Iterator<?> it = properties.entrySet().iterator();
		while (it.hasNext())
		{
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			log.info(key + "=" + value);
		}
	}
}
