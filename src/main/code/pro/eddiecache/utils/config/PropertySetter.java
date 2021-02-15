package pro.eddiecache.utils.config;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertySetter
{
	private static final Log log = LogFactory.getLog(PropertySetter.class);

	private final Object obj;

	private PropertyDescriptor[] props;

	public PropertySetter(Object obj)
	{
		this.obj = obj;
	}

	protected void introspect()
	{
		try
		{
			BeanInfo bi = Introspector.getBeanInfo(obj.getClass());
			props = bi.getPropertyDescriptors();
		}
		catch (IntrospectionException ex)
		{
			log.error("Fail to introspect " + obj + ": " + ex.getMessage());
			props = new PropertyDescriptor[0];
		}
	}

	public static void setProperties(Object obj, Properties properties, String prefix)
	{
		new PropertySetter(obj).setProperties(properties, prefix);
	}

	public void setProperties(Properties properties, String prefix)
	{
		int len = prefix.length();

		for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();)
		{
			String key = (String) e.nextElement();

			if (key.startsWith(prefix))
			{

				if (key.indexOf('.', len + 1) > 0)
				{
					continue;
				}

				String value = OptionConverter.findAndSubst(key, properties);
				key = key.substring(len);

				setProperty(key, value);
			}
		}

	}

	public void setProperty(String name, String value)
	{
		if (value == null)
		{
			return;
		}

		name = Introspector.decapitalize(name);

		PropertyDescriptor prop = getPropertyDescriptor(name);

		if (prop == null)
		{
			log.warn("No such property [" + name + "] in " + obj.getClass().getName() + ".");
		}
		else
		{
			try
			{
				setProperty(prop, name, value);
			}
			catch (PropertySetterException ex)
			{
				log.warn("Fail to set property " + name + " to value \"" + value + "\". " + ex.getMessage());
			}
		}
	}

	public void setProperty(PropertyDescriptor prop, String name, String value) throws PropertySetterException
	{
		Method setter = prop.getWriteMethod();
		if (setter == null)
		{
			throw new PropertySetterException("No setter for property");
		}
		Class<?>[] paramTypes = setter.getParameterTypes();

		if (paramTypes.length != 1)
		{
			throw new PropertySetterException("#params for setter != 1");
		}

		Object arg;
		try
		{
			arg = convertArg(value, paramTypes[0]);
		}
		catch (Throwable t)
		{
			throw new PropertySetterException("Conversion to type [" + paramTypes[0] + "] failed. Reason: " + t);
		}
		if (arg == null)
		{
			throw new PropertySetterException("Conversion to type [" + paramTypes[0] + "] failed.");
		}
		log.debug("Setting property [" + name + "] to [" + arg + "].");
		try
		{
			setter.invoke(obj, new Object[]
			{ arg });
		}
		catch (Exception ex)
		{
			throw new PropertySetterException(ex);
		}
	}

	protected Object convertArg(String value, Class<?> type)
	{
		if (value == null)
		{
			return null;
		}

		String val = value.trim();
		if (String.class.isAssignableFrom(type))
		{
			return value;
		}
		else if (Integer.TYPE.isAssignableFrom(type))
		{
			return Integer.valueOf(val);
		}
		else if (Long.TYPE.isAssignableFrom(type))
		{
			return Long.valueOf(val);
		}
		else if (Boolean.TYPE.isAssignableFrom(type))
		{
			if ("true".equalsIgnoreCase(val))
			{
				return Boolean.TRUE;
			}
			else if ("false".equalsIgnoreCase(val))
			{
				return Boolean.FALSE;
			}
		}
		else if (type.isEnum())
		{
			@SuppressWarnings("unchecked")
			Enum<?> en = Enum.valueOf(type.asSubclass(Enum.class), val);
			return en;
		}
		else if (File.class.isAssignableFrom(type))
		{
			return new File(val);
		}

		return null;
	}

	protected PropertyDescriptor getPropertyDescriptor(String name)
	{
		if (props == null)
		{
			introspect();
		}

		for (int i = 0; i < props.length; i++)
		{
			if (name.equals(props[i].getName()))
			{
				return props[i];
			}
		}
		return null;
	}
}
