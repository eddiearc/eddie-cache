package pro.eddiecache.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

public class IOClassLoaderWarpper extends ObjectInputStream
{
	private final ClassLoader classLoader;

	public IOClassLoaderWarpper(final InputStream in, final ClassLoader classLoader) throws IOException
	{
		super(in);
		this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
	}

	@Override
	protected Class<?> resolveClass(final ObjectStreamClass cls) throws ClassNotFoundException
	{
		return Class.forName(BlacklistClassResolver.DEFAULT.check(cls.getName()), false, classLoader);
	}

	@Override
	protected Class<?> resolveProxyClass(final String[] interfaces) throws IOException, ClassNotFoundException
	{
		final Class<?>[] cinterfaces = new Class[interfaces.length];
		for (int i = 0; i < interfaces.length; i++)
		{
			cinterfaces[i] = Class.forName(interfaces[i], false, classLoader);
		}

		try
		{
			return Proxy.getProxyClass(classLoader, cinterfaces);
		}
		catch (IllegalArgumentException e)
		{
			throw new ClassNotFoundException(null, e);
		}
	}

	private static class BlacklistClassResolver
	{
		private static final BlacklistClassResolver DEFAULT = new BlacklistClassResolver(
				toArray(System.getProperty("cachekit.serialization.class.blacklist")),
				toArray(System.getProperty("cachekit.serialization.class.whitelist")));

		private final String[] blacklist;
		private final String[] whitelist;

		protected BlacklistClassResolver(final String[] blacklist, final String[] whitelist)
		{
			this.whitelist = whitelist;
			this.blacklist = blacklist;
		}

		protected boolean isBlacklisted(final String name)
		{
			return (whitelist != null && !contains(whitelist, name)) || contains(blacklist, name);
		}

		public final String check(final String name)
		{
			if (isBlacklisted(name))
			{
				throw new SecurityException(name + " is not whitelisted as deserialisable.");
			}
			return name;
		}

		private static String[] toArray(final String property)
		{
			return property == null ? null : property.split(" *, *");
		}

		private static boolean contains(final String[] list, String name)
		{
			if (list != null)
			{
				for (final String white : list)
				{
					if (name.startsWith(white))
					{
						return true;
					}
				}
			}
			return false;
		}
	}
}
