package pro.eddiecache.ioc.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import pro.eddiecache.ioc.xml.exception.DocumentException;

public class XmlDocumentHolder implements IDocumentHolder
{

	private Map<String, Document> docs = new HashMap<String, Document>();

	@Override
	public Document getDocument(String filePath)
	{
		Document doc = docs.get(filePath);
		if (doc == null)
		{
			docs.put(filePath, readDocument(filePath));
		}
		return docs.get(filePath);
	}

	private Document readDocument(String filePath)
	{
		try
		{
			SAXReader reader = new SAXReader(true);
			reader.setEntityResolver(new IoCEntityResolver());
			File xmlFile = new File(filePath);
			Document doc = reader.read(xmlFile);
			return doc;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new DocumentException(e.getMessage());
		}
	}

}
