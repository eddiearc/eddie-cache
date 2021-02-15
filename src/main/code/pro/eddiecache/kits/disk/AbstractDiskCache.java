package pro.eddiecache.kits.disk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheEventQueueFactory;
import pro.eddiecache.core.CacheInfo;
import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheEventQueue;
import pro.eddiecache.core.model.ICacheListener;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;
import pro.eddiecache.kits.AbstractKitCacheEvent;
import pro.eddiecache.utils.struct.LRUMap;

public abstract class AbstractDiskCache<K, V> extends AbstractKitCacheEvent<K, V>
{
	private static final Log log = LogFactory.getLog(AbstractDiskCache.class);

	private IDiskCacheAttributes diskCacheAttributes = null;

	private Map<K, PurgatoryElement<K, V>> purgatory;

	private ICacheEventQueue<K, V> cacheEventQueue;

	private boolean alive = false;

	private String cacheName;

	private int purgHits = 0;

	private final ReentrantReadWriteLock removeAllLock = new ReentrantReadWriteLock();

	protected AbstractDiskCache(IDiskCacheAttributes attr)
	{
		this.diskCacheAttributes = attr;
		this.cacheName = attr.getCacheName();

		CacheEventQueueFactory<K, V> factory = new CacheEventQueueFactory<K, V>();
		this.cacheEventQueue = factory.createCacheEventQueue(new DiskCacheListener(), CacheInfo.listenerId, cacheName,
				diskCacheAttributes.getEventQueuePoolName(), diskCacheAttributes.getEventQueueType());

		initPurgatory();
	}

	public boolean isAlive()
	{
		return alive;
	}

	public void setAlive(boolean alive)
	{
		this.alive = alive;
	}

	private void initPurgatory()
	{
		removeAllLock.writeLock().lock();

		try
		{
			synchronized (this)
			{
				if (diskCacheAttributes.getMaxPurgatorySize() >= 0)
				{
					purgatory = new LRUMap<K, PurgatoryElement<K, V>>(diskCacheAttributes.getMaxPurgatorySize());
				}
				else
				{
					purgatory = new HashMap<K, PurgatoryElement<K, V>>();
				}
			}
		}
		finally
		{
			removeAllLock.writeLock().unlock();
		}
	}

	@Override
	public final void update(ICacheElement<K, V> cacheElement) throws IOException
	{
		try
		{
			PurgatoryElement<K, V> pe = new PurgatoryElement<K, V>(cacheElement);

			pe.setSpoolable(true);

			synchronized (purgatory)
			{
				purgatory.put(pe.getKey(), pe);
			}

			cacheEventQueue.addPutEvent(pe);
		}
		catch (IOException ex)
		{
			log.error("Error occur in adding put event to queue.", ex);

			cacheEventQueue.destroy();
		}
	}

	@Override
	public final ICacheElement<K, V> get(K key)
	{

		if (!alive)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Get was called, but the disk cache is not alive.");
			}
			return null;
		}

		PurgatoryElement<K, V> pe = null;
		synchronized (purgatory)
		{
			pe = purgatory.get(key);
		}

		if (pe != null)
		{
			purgHits++;

			if (log.isDebugEnabled())
			{
				if (purgHits % 100 == 0)
				{
					log.debug("Purgatory hits = " + purgHits);
				}
			}

			if (log.isDebugEnabled())
			{
				log.debug("Get element in purgatory, cacheName: " + cacheName + ", key: " + key);
			}

			return pe.getCacheElement();
		}

		try
		{
			return doGet(key);
		}
		catch (Exception e)
		{
			log.error(e);

			cacheEventQueue.destroy();
		}

		return null;
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMatching(String pattern) throws IOException
	{
		Set<K> keyArray = null;

		synchronized (purgatory)
		{
			keyArray = new HashSet<K>(purgatory.keySet());
		}

		Set<K> matchingKeys = getKeyMatcher().getMatchingKeysFromArray(pattern, keyArray);

		Map<K, ICacheElement<K, V>> result = processGetMultiple(matchingKeys);

		Map<K, ICacheElement<K, V>> diskMatches = doGetMatching(pattern);

		result.putAll(diskMatches);

		return result;
	}

	@Override
	public Map<K, ICacheElement<K, V>> processGetMultiple(Set<K> keys)
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

	@Override
	public abstract Set<K> getKeySet() throws IOException;

	@Override
	public final boolean remove(K key) throws IOException
	{
		PurgatoryElement<K, V> pe = null;

		synchronized (purgatory)
		{
			pe = purgatory.get(key);
		}

		if (pe != null)
		{
			synchronized (pe.getCacheElement())
			{
				synchronized (purgatory)
				{
					purgatory.remove(key);
				}

				pe.setSpoolable(false);

				doRemove(key);
			}
		}
		else
		{
			doRemove(key);
		}

		return false;
	}

	@Override
	public final void removeAll() throws IOException
	{
		if (this.diskCacheAttributes.isAllowRemoveAll())
		{
			initPurgatory();

			doRemoveAll();
		}
		else
		{
			if (log.isInfoEnabled())
			{
				log.info("allowRemoveAll is set to false.");
			}
		}
	}

	@Override
	public final void dispose() throws IOException
	{
		Runnable runner = new Runnable() {
			@Override
			public void run()
			{
				boolean keepGoing = true;
				// long total = 0;
				long interval = 100;
				while (keepGoing)
				{
					keepGoing = !cacheEventQueue.isEmpty();
					try
					{
						Thread.sleep(interval);
						// total += interval;
						// log.info( "total = " + total );
					}
					catch (InterruptedException e)
					{
						break;
					}
				}

			}
		};
		Thread thread = new Thread(runner);
		thread.start();
		try
		{
			thread.join(this.diskCacheAttributes.getShutdownSpoolTimeLimit() * 1000L);
		}
		catch (InterruptedException ex)
		{
			log.error("Shutdown spool process was interrupted.", ex);
		}

		cacheEventQueue.destroy();

		doDispose();

		alive = false;
	}

	@Override
	public String getCacheName()
	{
		return cacheName;
	}

	@Override
	public String getStats()
	{
		return getStatistics().toString();
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("Abstract Disk Cache");

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();

		elems.add(new StatElement<Integer>("Purgatory Hits", Integer.valueOf(purgHits)));
		elems.add(new StatElement<Integer>("Purgatory Size", Integer.valueOf(purgatory.size())));

		IStats eqStats = this.cacheEventQueue.getStatistics();
		elems.addAll(eqStats.getStatElements());

		stats.setStatElements(elems);

		return stats;
	}

	@Override
	public CacheStatus getStatus()
	{
		return (alive ? CacheStatus.ALIVE : CacheStatus.DISPOSED);
	}

	@Override
	public abstract int getSize();

	@Override
	public CacheType getCacheType()
	{
		return CacheType.DISK_CACHE;
	}

	protected class DiskCacheListener implements ICacheListener<K, V>
	{
		private long listenerId = 0;

		@Override
		public long getListenerId() throws IOException
		{
			return this.listenerId;
		}

		@Override
		public void setListenerId(long id) throws IOException
		{
			this.listenerId = id;
		}

		@Override
		public void handlePut(ICacheElement<K, V> element) throws IOException
		{
			if (alive)
			{
				if (element instanceof PurgatoryElement)
				{
					PurgatoryElement<K, V> pe = (PurgatoryElement<K, V>) element;

					synchronized (pe.getCacheElement())
					{
						removeAllLock.readLock().lock();

						try
						{
							synchronized (purgatory)
							{
								if (!purgatory.containsKey(pe.getKey()))
								{
									return;
								}

								element = pe.getCacheElement();
							}

							if (pe.isSpoolable())
							{
								doUpdate(element);
							}
						}
						finally
						{
							removeAllLock.readLock().unlock();
						}

						synchronized (purgatory)
						{
							purgatory.remove(element.getKey());
						}
					}
				}
				else
				{
					doUpdate(element);
				}
			}
			else
			{
				synchronized (purgatory)
				{
					purgatory.remove(element.getKey());
				}
			}
		}

		@Override
		public void handleRemove(String cacheName, K key) throws IOException
		{
			if (alive)
			{
				if (doRemove(key))
				{
					log.debug("Element removed, key: " + key);
				}
			}
		}

		@Override
		public void handleRemoveAll(String cacheName) throws IOException
		{
			if (alive)
			{
				doRemoveAll();
			}
		}

		@Override
		public void handleDispose(String cacheName) throws IOException
		{
			if (alive)
			{
				doDispose();
			}
		}
	}

	protected final ICacheElement<K, V> doGet(K key) throws IOException
	{
		return super.getWithEventLogger(key);
	}

	protected final Map<K, ICacheElement<K, V>> doGetMatching(String pattern) throws IOException
	{
		return super.getMatchingWithEventLogger(pattern);
	}

	protected final void doUpdate(ICacheElement<K, V> cacheElement) throws IOException
	{
		super.updateWithEventLogger(cacheElement);
	}

	protected final boolean doRemove(K key) throws IOException
	{
		return super.removeWithEventLogger(key);
	}

	protected final void doRemoveAll() throws IOException
	{
		super.removeAllWithEventLogger();
	}

	protected final void doDispose() throws IOException
	{
		super.disposeWithEventLogger();
	}

	@Override
	public String getEventLoggerExtraInfo()
	{
		return getDiskLocation();
	}

	protected abstract String getDiskLocation();
}
