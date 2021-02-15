package pro.eddiecache.core;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.ICacheListener;
import pro.eddiecache.core.model.ICacheObserver;

public class CacheWatchRepairable implements ICacheObserver
{
	private static final Log log = LogFactory.getLog(CacheWatchRepairable.class);

	private ICacheObserver cacheWatch;

	private final ConcurrentMap<String, Set<ICacheListener<?, ?>>> cacheMap = new ConcurrentHashMap<String, Set<ICacheListener<?, ?>>>();

	public void setCacheWatch(ICacheObserver cacheWatch)
	{
		this.cacheWatch = cacheWatch;
		for (Map.Entry<String, Set<ICacheListener<?, ?>>> entry : cacheMap.entrySet())
		{
			String cacheName = entry.getKey();
			for (ICacheListener<?, ?> listener : entry.getValue())
			{
				try
				{
					if (log.isInfoEnabled())
					{
						log.info("Add listener to cache watch. ICacheListener = " + listener + " | ICacheObserver = "
								+ cacheWatch);
					}
					cacheWatch.addCacheListener(cacheName, listener);
				}
				catch (IOException ex)
				{
					log.error("Error occur in adding listener. ICacheListener = " + listener + " | ICacheObserver = "
							+ cacheWatch, ex);
				}
			}
		}
	}

	@Override
	public <K, V> void addCacheListener(String cacheName, ICacheListener<K, V> listener) throws IOException
	{

		Set<ICacheListener<?, ?>> listenerSet = cacheMap.get(cacheName);
		if (listenerSet == null)
		{
			Set<ICacheListener<?, ?>> newListenerSet = new CopyOnWriteArraySet<ICacheListener<?, ?>>();
			listenerSet = cacheMap.putIfAbsent(cacheName, newListenerSet);

			if (listenerSet == null)
			{
				listenerSet = newListenerSet;
			}
		}

		listenerSet.add(listener);

		if (log.isInfoEnabled())
		{
			log.info("Add listener to cache watch. ICacheListener = " + listener + " , ICacheObserver = " + cacheWatch
					+ " , cacheName = " + cacheName);
		}
		cacheWatch.addCacheListener(cacheName, listener);
	}

	@Override
	public <K, V> void addCacheListener(ICacheListener<K, V> listener) throws IOException
	{
		for (Set<ICacheListener<?, ?>> listenerSet : cacheMap.values())
		{
			listenerSet.add(listener);
		}

		if (log.isInfoEnabled())
		{
			log.info("Add listener to cache watch. ICacheListener = " + listener + " | ICacheObserver = " + cacheWatch);
		}
		cacheWatch.addCacheListener(listener);
	}

	@Override
	public <K, V> void removeCacheListener(String cacheName, ICacheListener<K, V> listener) throws IOException
	{
		if (log.isInfoEnabled())
		{
			log.info("Remove cache listener, cacheName [" + cacheName + "]");
		}
		Set<ICacheListener<?, ?>> listenerSet = cacheMap.get(cacheName);
		if (listenerSet != null)
		{
			listenerSet.remove(listener);
		}
		cacheWatch.removeCacheListener(cacheName, listener);
	}

	@Override
	public <K, V> void removeCacheListener(ICacheListener<K, V> listener) throws IOException
	{
		if (log.isInfoEnabled())
		{
			log.info("Remove cache listener, ICacheListener [" + listener + "]");
		}

		for (Set<ICacheListener<?, ?>> listenerSet : cacheMap.values())
		{
			if (log.isDebugEnabled())
			{
				log.debug("Remove [" + listener + "] the listenerSet = " + listenerSet);
			}
			listenerSet.remove(listener);
		}
		cacheWatch.removeCacheListener(listener);
	}
}
