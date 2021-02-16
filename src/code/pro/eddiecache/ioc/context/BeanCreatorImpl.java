package pro.eddiecache.ioc.context;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import pro.eddiecache.ioc.context.exception.BeanCreateException;

public class BeanCreatorImpl implements IBeanCreator
{

	@Override
	public Object createBeanUseDefaultConstruct(String className)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			return clazz.newInstance();
		}
		catch (ClassNotFoundException e)
		{
			throw new BeanCreateException("class not found " + e.getMessage());
		}
		catch (Exception e)
		{
			throw new BeanCreateException(e.getMessage());
		}
	}

	private Constructor<?> getConstructor(Class<?> clazz, Class<?>[] argsClass)
	{
		try
		{
			Constructor<?> constructor = clazz.getConstructor(argsClass);
			return constructor;
		}
		catch (NoSuchMethodException e)
		{
			return null;
		}
	}

	private Constructor<?> findConstructor(Class<?> clazz, Class<?>[] argsClass) throws NoSuchMethodException
	{
		Constructor<?> constructor = getConstructor(clazz, argsClass);
		if (constructor == null)
		{
			Constructor<?>[] constructors = clazz.getConstructors();
			for (Constructor<?> c : constructors)
			{
				Class<?>[] constructorArgsCLass = c.getParameterTypes();
				if (constructorArgsCLass.length == argsClass.length)
				{
					if (isSameArgs(argsClass, constructorArgsCLass))
					{
						return c;
					}
				}
			}
		}
		else
		{
			return constructor;
		}
		throw new NoSuchMethodException("could not find any constructor");
	}

	private boolean isSameArgs(Class<?>[] argsClass, Class<?>[] constructorArgsCLass)
	{
		for (int i = 0; i < argsClass.length; i++)
		{
			try
			{
				argsClass[i].asSubclass(constructorArgsCLass[i]);
				if (i == (argsClass.length - 1))
				{
					return true;
				}
			}
			catch (Exception e)
			{
				break;
			}
		}
		return false;
	}

	@Override
	public Object createBeanUseDefineConstruce(String className, List<Object> args)
	{
		Class<?>[] argsClass = getArgsClasses(args);
		try
		{
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = findConstructor(clazz, argsClass);
			return constructor.newInstance(args.toArray());
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			throw new BeanCreateException("class not found " + e.getMessage());
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
			throw new BeanCreateException("no such constructor " + e.getMessage());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new BeanCreateException(e.getMessage());
		}
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

	private Class<?>[] getArgsClasses(List<Object> args)
	{
		List<Class<?>> result = new ArrayList<Class<?>>();
		for (Object arg : args)
		{
			result.add(getClass(arg));
		}
		Class<?>[] a = new Class[result.size()];
		return result.toArray(a);
	}

}
