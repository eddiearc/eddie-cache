package pro.eddiecache.core;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.ICacheListener;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;
import pro.eddiecache.utils.threadpool.ThreadPoolManager;

public class PooledCacheEventQueue<K, V> extends AbstractCacheEventQueue<K, V>
{
	private static final Log log = LogFactory.getLog(PooledCacheEventQueue.class);

	private static final QueueType queueType = QueueType.POOLED;

	private ThreadPoolExecutor pool = null;

	public PooledCacheEventQueue(ICacheListener<K, V> listener, long listenerId, String cacheName, int maxFailure,
                                 int waitBeforeRetry, String threadPoolName)
	{
		initialize(listener, listenerId, cacheName, maxFailure, waitBeforeRetry, threadPoolName);
	}

	protected void initialize(ICacheListener<K, V> listener, long listenerId, String cacheName, int maxFailure,
			int waitBeforeRetry, String threadPoolName)
	{
		super.initialize(listener, listenerId, cacheName, maxFailure, waitBeforeRetry);

		pool = ThreadPoolManager.getInstance().getPool((threadPoolName == null) ? "cache_event_queue" : threadPoolName);
	}

	@Override
	public QueueType getQueueType()
	{
		return queueType;
	}

	@Override
	public synchronized void destroy()
	{
		if (isAlive())
		{
			if (log.isInfoEnabled())
			{
				log.info("Cache event queue destroyed: " + this);
			}
			setAlive(false);
			pool.shutdownNow();
		}
	}

	@Override
	protected void put(AbstractCacheEvent event)
	{
		pool.execute(event);
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("Pooled Cache Event Queue");

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();

		elems.add(new StatElement<Boolean>("Working", super.isWorking()));
		elems.add(new StatElement<Boolean>("Alive", this.isAlive()));
		elems.add(new StatElement<Boolean>("Empty", this.isEmpty()));

		if (pool.getQueue() != null)
		{
			BlockingQueue<Runnable> bb = pool.getQueue();
			elems.add(new StatElement<Integer>("Queue Size", bb.size()));
			elems.add(new StatElement<Integer>("Queue Capacity", bb.remainingCapacity()));
		}

		elems.add(new StatElement<Integer>("Pool Size", pool.getPoolSize()));
		elems.add(new StatElement<Integer>("Maximum Pool Size", pool.getMaximumPoolSize()));

		stats.setStatElements(elems);

		return stats;
	}

	@Override
	public boolean isEmpty()
	{
		if (pool.getQueue() == null)
		{
			return true;
		}
		else
		{
			return pool.getQueue().size() == 0;
		}
	}

	@Override
	public int size()
	{
		if (pool.getQueue() == null)
		{
			return 0;
		}
		else
		{
			return pool.getQueue().size();
		}
	}
}
