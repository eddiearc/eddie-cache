package pro.eddiecache;

import java.util.Properties;

import pro.eddiecache.access.CacheKitAccess;
import pro.eddiecache.access.GroupCacheKitAccess;
import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.control.ContextCacheManager;
import pro.eddiecache.core.control.group.GroupAttrName;
import pro.eddiecache.core.model.IContextCacheAttributes;
import pro.eddiecache.core.model.IElementAttributes;

public abstract class CacheKit
{
	private static String configFilename = null;

	private static Properties configProps = null;

	private static ContextCacheManager cacheMgr;

	/**
	 * 指定对应的配置文件
	 *
	 * @param configFilename 配置文件路径与名称
	 */
	public static void setConfigFilename(String configFilename)
	{
		CacheKit.configFilename = configFilename;
	}

	public static void setConfigProperties(Properties configProps)
	{
		CacheKit.configProps = configProps;
	}

	public static void shutdown()
	{
		synchronized (CacheKit.class)
		{
			if (cacheMgr != null && cacheMgr.isInitialized())
			{
				cacheMgr.shutDown();
			}

			cacheMgr = null;
		}
	}

	private static ContextCacheManager getCacheManager() throws CacheException
	{
		synchronized (CacheKit.class)
		{
			if (cacheMgr == null || !cacheMgr.isInitialized())
			{
				if (configProps != null)
				{
					cacheMgr = ContextCacheManager.getUnconfiguredInstance();
					cacheMgr.configure(configProps);
				}
				else if (configFilename != null)
				{
					cacheMgr = ContextCacheManager.getUnconfiguredInstance();
					cacheMgr.configure(configFilename);
				}
				else
				{
					cacheMgr = ContextCacheManager.getInstance();
				}
			}

			return cacheMgr;
		}
	}

	public static <K, V> CacheKitAccess<K, V> getInstance(String cacheName) throws CacheException
	{
		ContextCache<K, V> cache = getCacheManager().getCache(cacheName);
		return new CacheKitAccess<K, V>(cache);
	}

	public static <K, V> CacheKitAccess<K, V> getInstance(String cacheName, IContextCacheAttributes icca)
			throws CacheException
	{
		ContextCache<K, V> cache = getCacheManager().getCache(cacheName, icca);
		return new CacheKitAccess<K, V>(cache);
	}

	public static <K, V> CacheKitAccess<K, V> getInstance(String cacheName, IContextCacheAttributes icca,
			IElementAttributes eattr) throws CacheException
	{
		ContextCache<K, V> cache = getCacheManager().getCache(cacheName, icca, eattr);
		return new CacheKitAccess<K, V>(cache);
	}

	public static <K, V> GroupCacheKitAccess<K, V> getGroupCacheInstance(String cacheName) throws CacheException
	{
		ContextCache<GroupAttrName<K>, V> cache = getCacheManager().getCache(cacheName);
		return new GroupCacheKitAccess<K, V>(cache);
	}

	public static <K, V> GroupCacheKitAccess<K, V> getGroupCacheInstance(String cacheName,
			IContextCacheAttributes icca) throws CacheException
	{
		ContextCache<GroupAttrName<K>, V> cache = getCacheManager().getCache(cacheName, icca);
		return new GroupCacheKitAccess<K, V>(cache);
	}

	public static <K, V> GroupCacheKitAccess<K, V> getGroupCacheInstance(String cacheName,
			IContextCacheAttributes icca, IElementAttributes eattr) throws CacheException
	{
		ContextCache<GroupAttrName<K>, V> cache = getCacheManager().getCache(cacheName, icca, eattr);
		return new GroupCacheKitAccess<K, V>(cache);
	}
}
