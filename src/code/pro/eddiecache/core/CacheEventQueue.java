package pro.eddiecache.core;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.ICacheListener;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;

public class CacheEventQueue<K, V> extends AbstractCacheEventQueue<K, V>
{
	private static final Log log = LogFactory.getLog(CacheEventQueue.class);

	private static final QueueType queueType = QueueType.SINGLE;

	private Thread processorThread;

	private LinkedBlockingQueue<AbstractCacheEvent> queue = new LinkedBlockingQueue<AbstractCacheEvent>();

	public CacheEventQueue(ICacheListener<K, V> listener, long listenerId, String cacheName)
	{
		this(listener, listenerId, cacheName, 10, 500);
	}

	public CacheEventQueue(ICacheListener<K, V> listener, long listenerId, String cacheName, int maxFailure,
			int waitBeforeRetry)
	{
		initialize(listener, listenerId, cacheName, maxFailure, waitBeforeRetry);
	}

	@Override
	public QueueType getQueueType()
	{
		return queueType;
	}

	protected void stopProcessing()
	{
		setAlive(false);
		processorThread = null;
	}

	@Override
	public void destroy()
	{
		if (isAlive())
		{
			setAlive(false);

			if (log.isInfoEnabled())
			{
				log.info("Destroy queue, stats =  " + getStatistics());
			}

			if (processorThread != null)
			{
				processorThread.interrupt();
				processorThread = null;
			}

			if (log.isInfoEnabled())
			{
				log.info("Cache event queue destroyed: " + this);
			}
		}
		else
		{
			if (log.isInfoEnabled())
			{
				log.info("Destroy was called after queue was destroyed. Doing nothing. Stats =  " + getStatistics());
			}
		}
	}

	@Override
	protected void put(AbstractCacheEvent event)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Event entering Queue for " + getCacheName() + ": " + event);
		}

		queue.offer(event);

		if (isWorking())
		{
			if (!isAlive())
			{
				setAlive(true);
				processorThread = new QProcessor();
				processorThread.start();
				if (log.isInfoEnabled())
				{
					log.info("Cache event queue created: " + this);
				}
			}
		}
	}

	// /////////////////////////// 内部类 /////////////////////////////

	protected class QProcessor extends Thread
	{
		QProcessor()
		{
			super("CacheEventQueue.QProcessor-" + getCacheName());
			setDaemon(true);
		}

		@Override
		public void run()
		{

			while (CacheEventQueue.this.isAlive())
			{
				AbstractCacheEvent event = null;

				try
				{
					event = queue.poll(getWaitToDieMillis(), TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{

				}

				if (log.isDebugEnabled())
				{
					log.debug("Event from queue = " + event);
				}

				if (event == null)
				{
					stopProcessing();
				}

				if (event != null && isWorking() && CacheEventQueue.this.isAlive())
				{
					event.run();
				}
			}

		}
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("Cache Event Queue");

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();

		elems.add(new StatElement<Boolean>("Working", Boolean.valueOf(this.isWorking())));
		elems.add(new StatElement<Boolean>("Alive", Boolean.valueOf(this.isAlive())));
		elems.add(new StatElement<Boolean>("Empty", Boolean.valueOf(this.isEmpty())));
		elems.add(new StatElement<Integer>("Size", Integer.valueOf(this.size())));

		stats.setStatElements(elems);

		return stats;
	}

	@Override
	public boolean isEmpty()
	{
		return queue.isEmpty();
	}

	@Override
	public int size()
	{
		return queue.size();
	}
}
