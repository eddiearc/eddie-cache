package pro.eddiecache.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XNode
{

	private final Node node;
	private final String name;
	private final String body;
	private final Properties attributes;
	private final Properties variables;
	private final XPathParser xpathParser;

	public XNode(XPathParser xpathParser, Node node, Properties variables)
	{
		this.xpathParser = xpathParser;
		this.node = node;
		this.name = node.getNodeName();
		this.variables = variables;
		this.attributes = parseAttributes(node);
		this.body = parseBody(node);
	}

	public XNode newXNode(Node node)
	{
		return new XNode(xpathParser, node, variables);
	}

	public XNode getParent()
	{
		Node parent = node.getParentNode();
		if (parent == null || !(parent instanceof Element))
		{
			return null;
		}
		else
		{
			return new XNode(xpathParser, parent, variables);
		}
	}

	public String getPath()
	{
		StringBuilder builder = new StringBuilder();
		Node current = node;
		while (current != null && current instanceof Element)
		{
			if (current != node)
			{
				builder.insert(0, "/");
			}
			builder.insert(0, current.getNodeName());
			current = current.getParentNode();
		}
		return builder.toString();
	}

	public String getValueBasedIdentifier()
	{
		StringBuilder builder = new StringBuilder();
		XNode current = this;
		while (current != null)
		{
			if (current != this)
			{
				builder.insert(0, "_");
			}
			String value = current.getStringAttribute("id",
					current.getStringAttribute("value", current.getStringAttribute("property", null)));
			if (value != null)
			{
				value = value.replace('.', '_');
				builder.insert(0, "]");
				builder.insert(0, value);
				builder.insert(0, "[");
			}
			builder.insert(0, current.getName());
			current = current.getParent();
		}
		return builder.toString();
	}

	public XNode evalNode(String expression)
	{
		return xpathParser.evalNode(node, expression);
	}

	public Node getNode()
	{
		return node;
	}

	public String getName()
	{
		return name;
	}

	public String getStringBody()
	{
		return getStringBody(null);
	}

	public String getStringBody(String def)
	{
		if (body == null)
		{
			return def;
		}
		else
		{
			return body;
		}
	}

	public <T extends Enum<T>> T getEnumAttribute(Class<T> enumType, String name)
	{
		return getEnumAttribute(enumType, name, null);
	}

	public <T extends Enum<T>> T getEnumAttribute(Class<T> enumType, String name, T def)
	{
		String value = getStringAttribute(name);
		if (value == null)
		{
			return def;
		}
		else
		{
			return Enum.valueOf(enumType, value);
		}
	}

	public String getStringAttribute(String name)
	{
		return getStringAttribute(name, null);
	}

	public String getStringAttribute(String name, String def)
	{
		String value = attributes.getProperty(name);
		if (value == null)
		{
			return def;
		}
		else
		{
			return value;
		}
	}

	public List<XNode> getChildren()
	{
		List<XNode> children = new ArrayList<XNode>();
		NodeList nodeList = node.getChildNodes();
		if (nodeList != null)
		{
			for (int i = 0, n = nodeList.getLength(); i < n; i++)
			{
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					children.add(new XNode(xpathParser, node, variables));
				}
			}
		}
		return children;
	}

	public Properties getChildrenAsProperties()
	{
		Properties properties = new Properties();
		for (XNode child : getChildren())
		{
			String name = child.getStringAttribute("name");
			String value = child.getStringAttribute("value");
			if (name != null && value != null)
			{
				properties.setProperty(name, value);
			}
		}
		return properties;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<");
		builder.append(name);
		for (Map.Entry<Object, Object> entry : attributes.entrySet())
		{
			builder.append(" ");
			builder.append(entry.getKey());
			builder.append("=\"");
			builder.append(entry.getValue());
			builder.append("\"");
		}
		List<XNode> children = getChildren();
		if (!children.isEmpty())
		{
			builder.append(">\n");
			for (XNode node : children)
			{
				builder.append(node.toString());
			}
			builder.append("</");
			builder.append(name);
			builder.append(">");
		}
		else if (body != null)
		{
			builder.append(">");
			builder.append(body);
			builder.append("</");
			builder.append(name);
			builder.append(">");
		}
		else
		{
			builder.append("/>");
		}
		builder.append("\n");
		return builder.toString();
	}

	private Properties parseAttributes(Node n)
	{
		Properties attributes = new Properties();
		NamedNodeMap attributeNodes = n.getAttributes();
		if (attributeNodes != null)
		{
			for (int i = 0; i < attributeNodes.getLength(); i++)
			{
				Node attribute = attributeNodes.item(i);
				//String value = PropertyParser.parse(attribute.getNodeValue(), variables);
				String value = attribute.getNodeValue();
				attributes.put(attribute.getNodeName(), value);
			}
		}
		return attributes;
	}

	private String parseBody(Node node)
	{
		String data = getBodyData(node);
		if (data == null)
		{
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				Node child = children.item(i);
				data = getBodyData(child);
				if (data != null)
				{
					break;
				}
			}
		}
		return data;
	}

	private String getBodyData(Node child)
	{
		if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE)
		{
			String data = ((CharacterData) child).getData();
			//data = PropertyParser.parse(data, variables);
			return data;
		}
		return null;
	}

}