package pro.eddiecache.xml;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XPathParser
{
	private final Document document;
	private boolean validation;
	private EntityResolver entityResolver;
	private Properties variables;
	private XPath xpath;

	public XPathParser(String xml)
	{
		commonConstructor(false, null, null);
		this.document = createDocument(new InputSource(new StringReader(xml)));
	}

	public XPathParser(Reader reader)
	{
		commonConstructor(false, null, null);
		this.document = createDocument(new InputSource(reader));
	}

	public XPathParser(InputStream inputStream)
	{
		commonConstructor(false, null, null);
		this.document = createDocument(new InputSource(inputStream));
	}

	public XNode evalNode(String expression)
	{
		return evalNode(document, expression);
	}

	public XNode evalNode(Object root, String expression)
	{
		Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
		if (node == null)
		{
			return null;
		}
		return new XNode(this, node, variables);
	}

	private Object evaluate(String expression, Object root, QName returnType)
	{
		try
		{
			return xpath.evaluate(expression, root, returnType);
		}
		catch (Exception e)
		{
			throw new XmlParserException("Error evaluating XPath.  Cause: " + e, e);
		}
	}

	private Document createDocument(InputSource inputSource)
	{

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(validation);

			factory.setNamespaceAware(false);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(false);
			factory.setCoalescing(false);
			factory.setExpandEntityReferences(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(entityResolver);
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void error(SAXParseException exception) throws SAXException
				{
					throw exception;
				}

				@Override
				public void fatalError(SAXParseException exception) throws SAXException
				{
					throw exception;
				}

				@Override
				public void warning(SAXParseException exception) throws SAXException
				{
				}
			});
			return builder.parse(inputSource);
		}
		catch (Exception e)
		{
			throw new XmlParserException("Error creating document instance.  Cause: " + e, e);
		}
	}

	private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver)
	{
		this.validation = validation;
		this.entityResolver = entityResolver;
		this.variables = variables;
		XPathFactory factory = XPathFactory.newInstance();
		this.xpath = factory.newXPath();
	}

}
