package pro.eddiecache.core;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheEventQueue;
import pro.eddiecache.core.model.ICacheListener;

/**
 * @author eddie
 */
public abstract class AbstractCacheEventQueue<K, V> implements ICacheEventQueue<K, V>
{
	private static final Log log = LogFactory.getLog(AbstractCacheEventQueue.class);

	protected static final int DEFAULT_WAIT_TO_DIE_MILLIS = 10000;

	private int waitToDieMillis = DEFAULT_WAIT_TO_DIE_MILLIS;

	private ICacheListener<K, V> listener;

	private long listenerId;

	private String cacheName;

	private int maxFailure;

	private int waitBeforeRetry;

	private final AtomicBoolean alive = new AtomicBoolean(false);

	private final AtomicBoolean working = new AtomicBoolean(true);

	public int getWaitToDieMillis()
	{
		return waitToDieMillis;
	}

	public void setWaitToDieMillis(int wtdm)
	{
		waitToDieMillis = wtdm;
	}

	@Override
	public String toString()
	{
		return "CacheEventQueue [listenerId=" + listenerId + ", cacheName=" + cacheName + "]";
	}

	@Override
	public boolean isAlive()
	{
		return alive.get();
	}

	public void setAlive(boolean aState)
	{
		alive.set(aState);
	}

	@Override
	public long getListenerId()
	{
		return listenerId;
	}

	protected String getCacheName()
	{
		return cacheName;
	}

	protected void initialize(ICacheListener<K, V> listener, long listenerId, String cacheName, int maxFailure,
			int waitBeforeRetry)
	{
		if (listener == null)
		{
			throw new IllegalArgumentException("Listener must not be null");
		}

		this.listener = listener;
		this.listenerId = listenerId;
		this.cacheName = cacheName;
		this.maxFailure = maxFailure <= 0 ? 3 : maxFailure;
		this.waitBeforeRetry = waitBeforeRetry <= 0 ? 500 : waitBeforeRetry;

		if (log.isDebugEnabled())
		{
			log.debug("Initialize CacheEventQueue: " + this);
		}
	}

	@Override
	public synchronized void addPutEvent(ICacheElement<K, V> ce) throws IOException
	{
		if (isWorking())
		{
			put(new PutEvent(ce));
		}
		else if (log.isWarnEnabled())
		{
			log.warn("Fail to put event for [" + this + "] because it not work.");
		}
	}

	@Override
	public synchronized void addRemoveEvent(K key) throws IOException
	{
		if (isWorking())
		{
			put(new RemoveEvent(key));
		}
		else if (log.isWarnEnabled())
		{
			log.warn("Fail to remove event for [" + this + "] because it not work.");
		}
	}

	@Override
	public synchronized void addRemoveAllEvent() throws IOException
	{
		if (isWorking())
		{
			put(new RemoveAllEvent());
		}
		else if (log.isWarnEnabled())
		{
			log.warn("fail to remove all event for [" + this + "] because it not work.");
		}
	}

	@Override
	public synchronized void addDisposeEvent() throws IOException
	{
		if (isWorking())
		{
			put(new DisposeEvent());
		}
		else if (log.isWarnEnabled())
		{
			log.warn("fail to dispose for [" + this + "] because it not work.");
		}
	}

	protected abstract void put(AbstractCacheEvent event);

	// /////////////////////////// 内部类 /////////////////////////////
	protected abstract class AbstractCacheEvent implements Runnable
	{
		int failures = 0;

		@Override
		public void run()
		{
			try
			{
				doRun();
			}
			catch (IOException e)
			{
				if (++failures >= maxFailure)
				{//多次重试，最终失败
					if (log.isWarnEnabled())
					{
						log.warn("error occur while running event from Queue: " + this);
					}
					setWorking(false);
					setAlive(false);
					return;
				}

				try
				{//开启重试
					if (log.isInfoEnabled())
					{
						log.info("error occur while running event from Queue: " + this + ". Retrying...");
					}
					Thread.sleep(waitBeforeRetry);
					run();
				}
				catch (InterruptedException ie)
				{
					if (log.isErrorEnabled())
					{
						log.warn("interrupted while sleeping for retry on event " + this + ".");
					}
					setWorking(false);
					setAlive(false);
					return;
				}
			}
		}

		protected abstract void doRun() throws IOException;
	}

	protected class PutEvent extends AbstractCacheEvent
	{
		private final ICacheElement<K, V> ice;

		PutEvent(ICacheElement<K, V> ice) throws IOException
		{
			this.ice = ice;
		}

		@Override
		protected void doRun() throws IOException
		{
			listener.handlePut(ice);
		}

		@Override
		public String toString()
		{
			return "PutEvent for key: " + ice.getKey() + " value: " +
					ice.getVal();
		}

	}

	protected class RemoveEvent extends AbstractCacheEvent
	{
		private final K key;

		RemoveEvent(K key) throws IOException
		{
			this.key = key;
		}

		@Override
		protected void doRun() throws IOException
		{
			listener.handleRemove(cacheName, key);
		}

		@Override
		public String toString()
		{
			return new StringBuilder("RemoveEvent for ").append(key).toString();
		}

	}

	protected class RemoveAllEvent extends AbstractCacheEvent
	{
		@Override
		protected void doRun() throws IOException
		{
			listener.handleRemoveAll(cacheName);
		}

		@Override
		public String toString()
		{
			return "RemoveAllEvent";
		}
	}

	protected class DisposeEvent extends AbstractCacheEvent
	{
		@Override
		protected void doRun() throws IOException
		{
			listener.handleDispose(cacheName);
		}

		@Override
		public String toString()
		{
			return "DisposeEvent";
		}
	}

	@Override
	public boolean isWorking()
	{
		return working.get();
	}

	public void setWorking(boolean b)
	{
		working.set(b);
	}
}
