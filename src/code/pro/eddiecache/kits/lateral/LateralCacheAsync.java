package pro.eddiecache.kits.lateral;

import java.io.IOException;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheEventQueueFactory;
import pro.eddiecache.core.CacheInfo;
import pro.eddiecache.core.CacheKitWrapper;
import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheEventQueue;
import pro.eddiecache.core.model.ICacheServiceRemote;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;
import pro.eddiecache.kits.AbstractKitCache;
import pro.eddiecache.kits.KitCacheAttributes;

/**
 * @author eddie
 *
 * LateralCache异步处理类
 */
public class LateralCacheAsync<K, V> extends AbstractKitCache<K, V>
{
	private static final Log log = LogFactory.getLog(LateralCacheAsync.class);

	private final LateralCache<K, V> cache;

	private ICacheEventQueue<K, V> eventQueue;

	private int getCount = 0;

	private int removeCount = 0;

	private int putCount = 0;

	public LateralCacheAsync(LateralCache<K, V> cache)
	{
		this.cache = cache;

		if (log.isDebugEnabled())
		{
			log.debug("Construct LateralCacheAsync, LateralCache = [" + cache + "]");
		}

		CacheEventQueueFactory<K, V> factory = new CacheEventQueueFactory<K, V>();
		this.eventQueue = factory.createCacheEventQueue(new CacheKitWrapper<K, V>(cache), CacheInfo.listenerId,
				cache.getCacheName(), cache.getKitCacheAttributes().getEventQueuePoolName(),
				cache.getKitCacheAttributes().getEventQueueType());

		if (cache.getStatus() == CacheStatus.ERROR)
		{
			eventQueue.destroy();
		}
	}

	/**
	 * 更新缓存值，通过事件异步处理
	 *
	 * @param ce 被更新的对象（K-V）
	 */
	@Override
	public void update(ICacheElement<K, V> ce) throws IOException
	{
		putCount++;
		try
		{
			eventQueue.addPutEvent(ce);
		}
		catch (IOException ex)
		{
			log.error(ex);
			eventQueue.destroy();
		}
	}

	/**
	 * 通过key得到对应的对象，直接进行读取（无需异步）
	 *
	 * @param key 键
	 */
	@Override
	public ICacheElement<K, V> get(K key)
	{
		getCount++;
		if (this.getStatus() != CacheStatus.ERROR)
		{
			try
			{
				return cache.get(key);
			}
			catch (UnmarshalException ue)
			{
				log.debug("Retry to get key");
				try
				{
					return cache.get(key);
				}
				catch (IOException ex)
				{
					log.error("Failed in retrying the get for the second time.");
					eventQueue.destroy();
				}
			}
			catch (IOException ex)
			{
				eventQueue.destroy();
			}
		}
		return null;
	}

	/**
	 * 批量获取K-V信息
	 *
	 * @param keys 批量的key值
	 */
	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys)
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		if (keys != null && !keys.isEmpty())
		{
			for (K key : keys)
			{
				ICacheElement<K, V> element = get(key);

				if (element != null)
				{
					elements.put(key, element);
				}
			}
		}

		return elements;
	}

	/**
	 * 根据字符串正则匹配获取对应的K-V信息
	 *
	 * @param pattern 正则匹配
	 */
	@Override
	public Map<K, ICacheElement<K, V>> getMatching(String pattern)
	{
		getCount++;
		if (this.getStatus() != CacheStatus.ERROR)
		{
			try
			{
				return cache.getMatching(pattern);
			}
			catch (UnmarshalException ue)
			{
				log.debug("Retry to get match");
				try
				{
					return cache.getMatching(pattern);
				}
				catch (IOException ex)
				{
					log.error("Failed in retrying the get match for the second time.");
					eventQueue.destroy();
				}
			}
			catch (IOException ex)
			{
				eventQueue.destroy();
			}
		}
		return Collections.emptyMap();
	}

	@Override
	public Set<K> getKeySet() throws IOException
	{
		try
		{
			return cache.getKeySet();
		}
		catch (IOException ex)
		{
			log.error(ex);
			eventQueue.destroy();
		}
		return Collections.emptySet();
	}

	@Override
	public boolean remove(K key)
	{
		removeCount++;
		try
		{
			eventQueue.addRemoveEvent(key);
		}
		catch (IOException ex)
		{
			log.error(ex);
			eventQueue.destroy();
		}
		return false;
	}

	@Override
	public void removeAll()
	{
		try
		{
			eventQueue.addRemoveAllEvent();
		}
		catch (IOException ex)
		{
			log.error(ex);
			eventQueue.destroy();
		}
	}

	@Override
	public void dispose()
	{
		try
		{
			eventQueue.addDisposeEvent();
		}
		catch (IOException ex)
		{
			log.error(ex);
			eventQueue.destroy();
		}
	}

	@Override
	public int getSize()
	{
		return cache.getSize();
	}

	@Override
	public CacheType getCacheType()
	{
		return cache.getCacheType();
	}

	@Override
	public CacheStatus getStatus()
	{
		return eventQueue.isWorking() ? cache.getStatus() : CacheStatus.ERROR;
	}

	@Override
	public String getCacheName()
	{
		return cache.getCacheName();
	}

	public void fixCache(ICacheServiceRemote<K, V> lateral)
	{
		cache.fixCache(lateral);
		resetEventQueue();
	}

	public void resetEventQueue()
	{
		if (eventQueue.isWorking())
		{
			eventQueue.destroy();
		}
		CacheEventQueueFactory<K, V> factory = new CacheEventQueueFactory<K, V>();
		this.eventQueue = factory.createCacheEventQueue(new CacheKitWrapper<K, V>(cache), CacheInfo.listenerId,
				cache.getCacheName(), cache.getKitCacheAttributes().getEventQueuePoolName(),
				cache.getKitCacheAttributes().getEventQueueType());
	}

	@Override
	public KitCacheAttributes getKitCacheAttributes()
	{
		return cache.getKitCacheAttributes();
	}

	@Override
	public String getStats()
	{
		return getStatistics().toString();
	}

	@Override
	public String getEventLoggerExtraInfo()
	{
		return "Lateral Cache Async";
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("Lateral Cache Async");

		IStats eqStats = this.eventQueue.getStatistics();
		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>(eqStats.getStatElements());

		elems.add(new StatElement<Integer>("Get Count", this.getCount));
		elems.add(new StatElement<Integer>("Remove Count", this.removeCount));
		elems.add(new StatElement<Integer>("Put Count", this.putCount));
		elems.add(new StatElement<KitCacheAttributes>("Attributes", cache.getKitCacheAttributes()));

		stats.setStatElements(elems);

		return stats;
	}

	@Override
	public String toString()
	{
		return " LateralCacheAsync " +
				" Status = " + this.getStatus() +
				" cache = [" + cache.toString() + "]";
	}
}
