package pro.eddiecache.ioc.xml;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import pro.eddiecache.ioc.xml.autowire.ByNameAutowire;
import pro.eddiecache.ioc.xml.autowire.IAutowire;
import pro.eddiecache.ioc.xml.autowire.NoAutowire;
import pro.eddiecache.ioc.xml.construct.ParamElement;
import pro.eddiecache.ioc.xml.construct.RefElement;
import pro.eddiecache.ioc.xml.construct.ValueElement;
import pro.eddiecache.ioc.xml.property.PropertyElement;

public class ElementReaderImpl implements IElementReader
{

	@Override
	public String getAttribute(Element element, String name)
	{
		String value = element.attributeValue(name);
		return value;
	}

	@Override
	public List<Element> getConstructorElements(Element element)
	{
		List<Element> children = element.elements();
		List<Element> result = new ArrayList<Element>();
		for (Element e : children)
		{
			if ("constructor-arg".equals(e.getName()))
			{
				result.add(e);
			}
		}
		return result;
	}

	@Override
	public List<Element> getPropertyElements(Element element)
	{
		List<Element> children = element.elements();
		List<Element> result = new ArrayList<Element>();
		for (Element e : children)
		{
			if ("property".equals(e.getName()))
			{
				result.add(e);
			}
		}
		return result;
	}

	@Override
	public IAutowire getAutowire(Element element)
	{
		String value = getAttribute(element, "autowire");
		String parentValue = getAttribute(element.getParent(), "default-autowire");
		if ("no".equals(parentValue))
		{
			if ("byName".equals(value))
			{
				return new ByNameAutowire(value);
			}
			return new NoAutowire(value);
		}
		else if ("byName".equals(parentValue))
		{
			if ("no".equals(value))
			{
				return new NoAutowire(value);
			}
			return new ByNameAutowire(value);
		}
		return new NoAutowire(value);
	}

	@Override
	public boolean isLazy(Element element)
	{
		String lazy = getAttribute(element, "lazy-init");
		Element parent = element.getParent();
		Boolean parentLazy = new Boolean(getAttribute(parent, "default-lazy-init"));
		if (parentLazy)
		{
			if ("false".equals(lazy))
			{
				return false;
			}
			return true;
		}
		else
		{
			if ("true".equals(lazy))
			{
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean isSingleton(Element element)
	{
		Boolean singleton = new Boolean(getAttribute(element, "singleton"));
		return singleton;
	}

	@Override
	public List<ParamElement> getConstructorValue(Element element)
	{
		List<Element> cons = getConstructorElements(element);
		List<ParamElement> result = new ArrayList<ParamElement>();
		for (Element e : cons)
		{
			List<Element> els = e.elements();
			ParamElement paramElement = getDataElement(els.get(0));
			result.add(paramElement);
		}
		return result;
	}

	@Override
	public List<PropertyElement> getPropertyValue(Element element)
	{
		List<Element> properties = getPropertyElements(element);
		List<PropertyElement> result = new ArrayList<PropertyElement>();
		for (Element e : properties)
		{
			List<Element> els = e.elements();
			ParamElement paramElement = getDataElement(els.get(0));
			String propertyNameAtt = getAttribute(e, "name");
			PropertyElement pe = new PropertyElement(propertyNameAtt, paramElement);
			result.add(pe);
		}
		return result;
	}

	private ParamElement getDataElement(Element dataElement)
	{
		String name = dataElement.getName();
		if ("value".equals(name))
		{
			String classTypeName = dataElement.attributeValue("type");
			String data = dataElement.getText();
			return new ValueElement(getValue(classTypeName, data));
		}
		else if ("ref".equals(name))
		{
			return new RefElement(getAttribute(dataElement, "bean"));
		}
		return null;
	}

	private Object getValue(String className, String data)
	{
		if (isType(className, "Integer"))
		{
			return Integer.parseInt(data);
		}
		else if (isType(className, "Boolean"))
		{
			return Boolean.valueOf(data);
		}
		else if (isType(className, "Long"))
		{
			return Long.valueOf(data);
		}
		else if (isType(className, "Short"))
		{
			return Short.valueOf(data);
		}
		else if (isType(className, "Double"))
		{
			return Double.valueOf(data);
		}
		else if (isType(className, "Float"))
		{
			return Float.valueOf(data);
		}
		else if (isType(className, "Character"))
		{
			return data.charAt(0);
		}
		else if (isType(className, "Byte"))
		{
			return Byte.valueOf(data);
		}
		else
		{
			return data;
		}
	}

	private boolean isType(String className, String type)
	{
		if (className.indexOf(type) != -1)
		{
			return true;
		}
		return false;
	}
}
