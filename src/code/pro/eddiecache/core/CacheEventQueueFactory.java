package pro.eddiecache.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.ICacheEventQueue;
import pro.eddiecache.core.model.ICacheListener;

/**
 * @author eddie
 */
public class CacheEventQueueFactory<K, V>
{
	private static final Log log = LogFactory.getLog(CacheEventQueueFactory.class);

	public ICacheEventQueue<K, V> createCacheEventQueue(ICacheListener<K, V> listener, long listenerId,
                                                        String cacheName, String threadPoolName, ICacheEventQueue.QueueType poolType)
	{
		return createCacheEventQueue(listener, listenerId, cacheName, 10, 500, threadPoolName, poolType);
	}

	public ICacheEventQueue<K, V> createCacheEventQueue(ICacheListener<K, V> listener, long listenerId,
			String cacheName, int maxFailure, int waitBeforeRetry, String threadPoolName,
			ICacheEventQueue.QueueType poolType)
	{
		if (log.isDebugEnabled())
		{
			log.debug("ThreadPoolName = [" + threadPoolName + "] poolType = " + poolType + " ");
		}

		ICacheEventQueue<K, V> eventQueue = null;
		if (poolType == null || ICacheEventQueue.QueueType.SINGLE == poolType)
		{
			eventQueue = new CacheEventQueue<K, V>(listener, listenerId, cacheName, maxFailure, waitBeforeRetry);
		}
		else if (ICacheEventQueue.QueueType.POOLED == poolType)
		{
			eventQueue = new PooledCacheEventQueue<K, V>(listener, listenerId, cacheName, maxFailure, waitBeforeRetry,
					threadPoolName);
		}

		return eventQueue;
	}
}
