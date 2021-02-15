package pro.eddiecache.core;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import pro.eddiecache.core.model.ICache;
import pro.eddiecache.core.model.ICacheEventQueue;

public class CacheListeners<K, V>
{
	public final ICache<K, V> cache;

	public final ConcurrentMap<Long, ICacheEventQueue<K, V>> eventQueueMap = new ConcurrentHashMap<Long, ICacheEventQueue<K, V>>();

	public CacheListeners(ICache<K, V> cache)
	{
		if (cache == null)
		{
			throw new IllegalArgumentException("cache must not be null");
		}
		this.cache = cache;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n CacheListeners");
		if (cache != null)
		{
			sb.append("\n CacheName = " + cache.getCacheName());
		}
		if (eventQueueMap != null)
		{
			sb.append("\n Event Queue Map ");
			sb.append("\n size = " + eventQueueMap.size());
			Iterator<Map.Entry<Long, ICacheEventQueue<K, V>>> it = eventQueueMap.entrySet().iterator();
			while (it.hasNext())
			{
				sb.append("\n Entry: " + it.next());
			}
		}
		else
		{
			sb.append("\n No Listeners. ");
		}
		return sb.toString();
	}
}
