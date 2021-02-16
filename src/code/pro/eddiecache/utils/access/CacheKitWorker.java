package pro.eddiecache.utils.access;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.CacheKit;
import pro.eddiecache.access.CacheKitAccess;
import pro.eddiecache.access.GroupCacheKitAccess;
import pro.eddiecache.access.exception.CacheException;

public class CacheKitWorker<K, V>
{
	private static final Log logger = LogFactory.getLog(CacheKitWorker.class);

	private CacheKitAccess<K, V> cache;

	private GroupCacheKitAccess<K, V> groupCache;

	private volatile ConcurrentMap<String, CacheKitWorkerHelper<V>> map = new ConcurrentHashMap<String, CacheKitWorkerHelper<V>>();

	private final String cacheName;

	public CacheKitWorker(final String cacheName)
	{
		this.cacheName = cacheName;
		try
		{
			this.cache = CacheKit.getInstance(cacheName);
			this.groupCache = CacheKit.getGroupCacheInstance(cacheName);
		}
		catch (CacheException e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

	public String getCacheName()
	{
		return cacheName;
	}

	public V getResult(K key, CacheKitWorkerHelper<V> worker) throws Exception
	{
		return run(key, null, worker);
	}

	public V getResult(K key, String group, CacheKitWorkerHelper<V> worker) throws Exception
	{
		return run(key, group, worker);
	}

	private V run(K key, String group, CacheKitWorkerHelper<V> helper) throws Exception
	{
		V result = null;

		CacheKitWorkerHelper<V> myHelper = map.putIfAbsent(getCacheName() + key, helper);

		if (myHelper != null)
		{
			synchronized (myHelper)
			{

				while (!myHelper.isFinished())
				{
					try
					{
						myHelper.wait();
					}
					catch (InterruptedException e)
					{

					}
				}

			}
		}
		try
		{
			if (logger.isDebugEnabled())
			{
				logger.debug(getCacheName() + " is doing the work.");
			}

			if (group != null)
			{
				result = groupCache.getFromGroup(key, group);
			}
			else
			{
				result = cache.get(key);
			}
			if (result == null)
			{
				result = helper.doWork();

				if (group != null)
				{
					groupCache.putInGroup(key, group, result);
				}
				else
				{
					cache.put(key, result);
				}
			}
			return result;
		}
		finally
		{
			if (myHelper == null)
			{
				map.remove(getCacheName() + key);
			}
			synchronized (helper)
			{
				helper.setFinished(true);
				helper.notifyAll();
			}
		}
	}
}
