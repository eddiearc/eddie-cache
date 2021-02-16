package pro.eddiecache.ioc.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.eddiecache.ioc.context.exception.BeanCreateException;
import pro.eddiecache.ioc.context.exception.PropertyException;

@SuppressWarnings("unchecked")
public class PropertyHandlerImpl implements IPropertyHandler
{
	@Override
	public Object setProperties(Object obj, Map<String, Object> properties)
	{
		Class<?> clazz = obj.getClass();
		try
		{
			for (String key : properties.keySet())
			{
				String setterName = getSetterMethodName(key);
				Class<?> argClass = getClass(properties.get(key));
				Method setterMethod = getSetterMethod(clazz, setterName, argClass);
				setterMethod.invoke(obj, properties.get(key));
			}
			return obj;
		}
		catch (NoSuchMethodException e)
		{
			throw new PropertyException("setter method not found " + e.getMessage());
		}
		catch (IllegalArgumentException e)
		{
			throw new PropertyException("wrong argument " + e.getMessage());
		}
		catch (Exception e)
		{
			throw new PropertyException(e.getMessage());
		}
	}

	private Method findMethod(Class<?> argClass, List<Method> methods)
	{
		for (Method m : methods)
		{
			if (isMethodArgs(m, argClass))
			{
				return m;
			}
		}
		return null;
	}

	private boolean isMethodArgs(Method m, Class<?> argClass)
	{
		Class<?>[] c = m.getParameterTypes();
		if (c.length == 1)
		{
			try
			{
				argClass.asSubclass(c[0]);
				return true;
			}
			catch (ClassCastException e)
			{
				return false;
			}
		}
		return false;
	}

	private Method getMethod(Class<?> objClass, String methodName, Class<?> argClass)
	{
		try
		{
			Method method = objClass.getMethod(methodName, argClass);
			return method;
		}
		catch (NoSuchMethodException e)
		{
			return null;
		}
	}

	private Method getSetterMethod(Class<?> objClass, String methodName, Class<?> argClass) throws NoSuchMethodException
	{

		Method argClassMethod = getMethod(objClass, methodName, argClass);

		if (argClassMethod == null)
		{

			List<Method> methods = getMethods(objClass, methodName);
			Method method = findMethod(argClass, methods);
			if (method == null)
			{

				throw new NoSuchMethodException(methodName);
			}
			return method;
		}
		else
		{
			return argClassMethod;
		}
	}

	private List<Method> getMethods(Class<?> objClass, String methodName)
	{
		List<Method> result = new ArrayList<Method>();
		for (Method m : objClass.getMethods())
		{
			if (m.getName().equals(methodName))
			{

				Class<?>[] c = m.getParameterTypes();
				if (c.length == 1)
				{
					result.add(m);
				}
			}
		}
		return result;
	}

	private List<Method> getSetterMethodsList(Object obj)
	{
		Class<?> clazz = obj.getClass();
		Method[] methods = clazz.getMethods();
		List<Method> result = new ArrayList<Method>();
		for (Method m : methods)
		{
			if (m.getName().startsWith("set"))
			{
				result.add(m);
			}
		}
		return result;
	}

	public Map<String, Method> getSetterMethodsMap(Object obj)
	{
		List<Method> methods = getSetterMethodsList(obj);
		Map<String, Method> result = new HashMap<String, Method>();
		for (Method m : methods)
		{
			String propertyName = getMethodNameWithOutSet(m.getName());
			result.put(propertyName, m);
		}
		return result;
	}

	public void executeMethod(Object object, Object argBean, Method method)
	{
		try
		{
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 1)
			{
				if (isMethodArgs(method, parameterTypes[0]))
				{
					method.invoke(object, argBean);
				}
			}
		}
		catch (Exception e)
		{
			throw new BeanCreateException("autowire exception " + e.getMessage());
		}
	}

	private String getMethodNameWithOutSet(String methodName)
	{
		String propertyName = methodName.replaceFirst("set", "");
		String firstWord = propertyName.substring(0, 1);
		String lowerFirstWord = firstWord.toLowerCase();
		return propertyName.replaceFirst(firstWord, lowerFirstWord);
	}

	private Class<?> getClass(Object obj)
	{
		if (obj instanceof Integer)
		{
			return Integer.TYPE;
		}
		else if (obj instanceof Boolean)
		{
			return Boolean.TYPE;
		}
		else if (obj instanceof Long)
		{
			return Long.TYPE;
		}
		else if (obj instanceof Short)
		{
			return Short.TYPE;
		}
		else if (obj instanceof Double)
		{
			return Double.TYPE;
		}
		else if (obj instanceof Float)
		{
			return Float.TYPE;
		}
		else if (obj instanceof Character)
		{
			return Character.TYPE;
		}
		else if (obj instanceof Byte)
		{
			return Byte.TYPE;
		}
		return obj.getClass();
	}

	private String getSetterMethodName(String propertyName)
	{
		return "set" + upperCaseFirstWord(propertyName);
	}

	private String upperCaseFirstWord(String s)
	{
		String firstWord = s.substring(0, 1);
		String upperCaseWord = firstWord.toUpperCase();
		return s.replaceFirst(firstWord, upperCaseWord);
	}

}
