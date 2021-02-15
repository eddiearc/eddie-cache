package pro.eddiecache.ioc.context;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import pro.eddiecache.ioc.context.exception.BeanCreateException;
import pro.eddiecache.ioc.xml.ElementContainerImpl;
import pro.eddiecache.ioc.xml.ElementReaderImpl;
import pro.eddiecache.ioc.xml.IDocumentHolder;
import pro.eddiecache.ioc.xml.IElementContainer;
import pro.eddiecache.ioc.xml.IElementReader;
import pro.eddiecache.ioc.xml.XmlDocumentHolder;
import pro.eddiecache.ioc.xml.autowire.ByNameAutowire;
import pro.eddiecache.ioc.xml.autowire.IAutowire;
import pro.eddiecache.ioc.xml.autowire.NoAutowire;
import pro.eddiecache.ioc.xml.construct.ParamElement;
import pro.eddiecache.ioc.xml.construct.RefElement;
import pro.eddiecache.ioc.xml.construct.ValueElement;
import pro.eddiecache.ioc.xml.property.PropertyElement;

public abstract class AbstractAppContext implements IAppContext
{

	protected IElementContainer elementContainer = new ElementContainerImpl();

	protected IDocumentHolder documentHolder = new XmlDocumentHolder();

	protected Map<String, Object> beans = new HashMap<String, Object>();

	protected IPropertyHandler propertyHandler = new PropertyHandlerImpl();

	protected IBeanCreator beanCreator = new BeanCreatorImpl();

	protected IElementReader elementReader = new ElementReaderImpl();

	protected void setUpElements(String[] xmlPaths)
	{
		try
		{
			URL classPathUrl = AbstractAppContext.class.getClassLoader().getResource(".");
			String classPath = java.net.URLDecoder.decode(classPathUrl.getPath(), "utf-8");
			for (String path : xmlPaths)
			{
				Document doc = documentHolder.getDocument(classPath + path);
				elementContainer.addElements(doc);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	protected void createBeans()
	{
		Collection<Element> elements = elementContainer.getElements();
		for (Element e : elements)
		{
			boolean lazy = elementReader.isLazy(e);
			if (!lazy)
			{
				String id = e.attributeValue("id");
				Object bean = this.getBean(id);
				if (bean == null)
				{
					handleSingleton(id);
				}
			}
		}
	}

	protected Object handleSingleton(String id)
	{
		Object bean = createBean(id);
		if (isSingleton(id))
		{
			beans.put(id, bean);
		}
		return bean;
	}

	protected Object createBean(String id)
	{
		Element e = elementContainer.getElement(id);
		if (e == null)
		{
			throw new BeanCreateException("element not found " + id);
		}
		Object result = instance(e);

		IAutowire autowire = elementReader.getAutowire(e);
		if (autowire instanceof ByNameAutowire)
		{
			autowireByName(result);
		}
		else if (autowire instanceof NoAutowire)
		{
			setterInject(result, e);
		}
		return result;
	}

	protected Object instance(Element e)
	{
		String className = elementReader.getAttribute(e, "class");

		List<Element> constructorElements = elementReader.getConstructorElements(e);

		if (constructorElements.size() == 0)
		{
			return beanCreator.createBeanUseDefaultConstruct(className);
		}
		else
		{
			List<Object> args = getConstructArgs(e);
			return beanCreator.createBeanUseDefineConstruce(className, args);
		}
	}

	protected void setterInject(Object obj, Element e)
	{
		List<PropertyElement> properties = elementReader.getPropertyValue(e);
		Map<String, Object> propertiesMap = getPropertyArgs(properties);
		propertyHandler.setProperties(obj, propertiesMap);
	}

	protected Map<String, Object> getPropertyArgs(List<PropertyElement> properties)
	{
		Map<String, Object> result = new HashMap<String, Object>();
		for (PropertyElement pro : properties)
		{
			ParamElement de = pro.getParamElement();
			if (de instanceof RefElement)
			{
				result.put(pro.getName(), getBean((String) de.getValue()));
			}
			else if (de instanceof ValueElement)
			{
				result.put(pro.getName(), de.getValue());
			}
		}
		return result;
	}

	protected List<Object> getConstructArgs(Element e)
	{
		List<ParamElement> params = elementReader.getConstructorValue(e);
		List<Object> result = new ArrayList<Object>();
		for (ParamElement param : params)
		{
			if (param instanceof ValueElement)
			{
				param = (ValueElement) param;
				result.add(param.getValue());
			}
			else if (param instanceof RefElement)
			{
				param = (RefElement) param;
				String refId = (String) param.getValue();
				result.add(getBean(refId));
			}
		}
		return result;
	}

	protected void autowireByName(Object obj)
	{
		Map<String, Method> methods = propertyHandler.getSetterMethodsMap(obj);
		for (String s : methods.keySet())
		{
			Element e = elementContainer.getElement(s);
			if (e == null)
			{
				continue;
			}
			Object bean = getBean(s);
			Method method = methods.get(s);
			propertyHandler.executeMethod(obj, bean, method);
		}
	}

	@Override
	public boolean containsBean(String id)
	{
		Element e = elementContainer.getElement(id);
		return (e == null) ? false : true;
	}

	@Override
	public Object getBean(String id)
	{
		Object bean = beans.get(id);
		if (bean == null)
		{
			bean = handleSingleton(id);
		}
		return bean;
	}

	public boolean isSingleton(String id)
	{
		Element e = elementContainer.getElement(id);
		return elementReader.isSingleton(e);
	}

	public Object getBeanIgnoreCreate(String id)
	{
		return beans.get(id);
	}
}
