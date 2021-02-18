package pro.eddiecache.core.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.access.exception.ObjectNotFoundException;
import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.control.event.ElementEvent;
import pro.eddiecache.core.control.event.ElementEventType;
import pro.eddiecache.core.control.event.IElementEvent;
import pro.eddiecache.core.control.event.IElementEventHandler;
import pro.eddiecache.core.control.event.IElementEventQueue;
import pro.eddiecache.core.match.IKeyMatcher;
import pro.eddiecache.core.match.KeyMatcher;
import pro.eddiecache.core.memory.IMemoryCache;
import pro.eddiecache.core.memory.lru.LRUMemoryCache;
import pro.eddiecache.core.memory.shrinking.ShrinkerThread;
import pro.eddiecache.core.model.ICache;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IContextCacheAttributes;
import pro.eddiecache.core.model.IElementAttributes;
import pro.eddiecache.core.model.IRequireScheduler;
import pro.eddiecache.core.stats.CacheStats;
import pro.eddiecache.core.stats.ICacheStats;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.kits.KitCache;

public class ContextCache<K, V> implements ICache<K, V>, IRequireScheduler
{
	private static final Log log = LogFactory.getLog(ContextCache.class);

	private IElementEventQueue elementEventQueue;

	@SuppressWarnings("unchecked")
	private KitCache<K, V>[] kitCaches = new KitCache[0];

	private AtomicBoolean alive;

	private IElementAttributes attr;

	private IContextCacheAttributes cacheAttr;

	private AtomicInteger updateCount;

	private AtomicInteger removeCount;

	private AtomicInteger hitCountMemCache;

	private AtomicInteger hitCountKitCache;

	private AtomicInteger missCountNotFound;

	private AtomicInteger missCountExpired;

	private ContextCacheManager cacheManager = null;

	private IMemoryCache<K, V> memCache;

	private IKeyMatcher<K> keyMatcher = new KeyMatcher<K>();

	private ScheduledFuture<?> future;

	public ContextCache(IContextCacheAttributes cattr, IElementAttributes attr)
	{
		this.attr = attr;
		this.cacheAttr = cattr;
		this.alive = new AtomicBoolean(true);
		this.updateCount = new AtomicInteger(0);
		this.removeCount = new AtomicInteger(0);
		this.hitCountMemCache = new AtomicInteger(0);
		this.hitCountKitCache = new AtomicInteger(0);
		this.missCountNotFound = new AtomicInteger(0);
		this.missCountExpired = new AtomicInteger(0);

		createMemoryCache(cattr);

		if (log.isInfoEnabled())
		{
			log.info("Create cache with name [" + cacheAttr.getCacheName() + "] and cache attributes " + cattr);
		}
	}

	public void setElementEventQueue(IElementEventQueue queue)
	{
		this.elementEventQueue = queue;
	}

	public void setContextCacheManager(ContextCacheManager manager)
	{
		this.cacheManager = manager;
	}

	@Override
	public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutor)
	{
		if (cacheAttr.isUseMemoryShrinker())
		{
			future = scheduledExecutor.scheduleAtFixedRate(new ShrinkerThread<K, V>(this), 0,
					cacheAttr.getShrinkerIntervalSeconds(), TimeUnit.SECONDS);
		}
	}

	public void setKitCaches(KitCache<K, V>[] kitCaches)
	{
		this.kitCaches = kitCaches;
	}

	public KitCache<K, V>[] getKitCaches()
	{
		return this.kitCaches;
	}

	@Override
	public void update(ICacheElement<K, V> ce) throws IOException
	{
		update(ce, false);
	}

	public void localUpdate(ICacheElement<K, V> ce) throws IOException
	{
		update(ce, true);
	}

	protected void update(ICacheElement<K, V> cacheElement, boolean localOnly) throws IOException
	{

		//		if (cacheElement.getKey() instanceof String
		//				&& cacheElement.getKey().toString().endsWith(CacheConstants.NAME_COMPONENT_DELIMITER))
		//		{
		//			throw new IllegalArgumentException(
		//					"key must not end with " + CacheConstants.NAME_COMPONENT_DELIMITER + " for a put operation");
		//		}
		//		else if (cacheElement.getKey() instanceof GroupId)
		//		{
		//			throw new IllegalArgumentException("key cannot be a GroupId " + " for a put operation");
		//		}

		if (log.isDebugEnabled())
		{
			log.debug("Update memory cache " + cacheElement.getKey());
		}

		updateCount.incrementAndGet();

		synchronized (this)
		{
			memCache.update(cacheElement);
			updateKits(cacheElement, localOnly);
		}

		cacheElement.getElementAttributes().setLastAccessTimeNow();
	}

	protected void updateKits(ICacheElement<K, V> cacheElement, boolean localOnly) throws IOException
	{
		if (log.isDebugEnabled())
		{
			if (kitCaches.length > 0)
			{
				log.debug("Update kit caches");
			}
			else
			{
				log.debug("No kit cache to update");
			}
		}

		for (ICache<K, V> kit : kitCaches)
		{
			if (kit == null)
			{
				continue;
			}

			if (log.isDebugEnabled())
			{
				log.debug("Kit cache type: " + kit.getCacheType());
			}

			switch (kit.getCacheType())
			{
				case PAXOS_CACHE:
					//	if (log.isDebugEnabled())
					// {
					// log.debug("ce.getElementAttributes().getIsRemote() = "
					//		+ cacheElement.getElementAttributes().getIsRemote());
					// }

					//if (cacheElement.getElementAttributes().getIsRemote() && !localOnly)
					if (!localOnly)
					{
						try
						{
							kit.update(cacheElement);

							if (log.isDebugEnabled())
							{
								log.debug("Update paxos cache  for " + cacheElement.getKey() + cacheElement);
							}
						}
						catch (IOException ex)
						{
							log.error("Fail to update in paxos cache", ex);
						}
					}
					break;

				case LATERAL_CACHE:

					if (log.isDebugEnabled())
					{
						log.debug("Lateral cache in kit list: cattr " + cacheAttr.isUseLateral());
					}

					if (cacheAttr.isUseLateral() && cacheElement.getElementAttributes().getIsLateral() && !localOnly)
					{
						kit.update(cacheElement);

						if (log.isDebugEnabled())
						{
							log.debug("Updated lateral cache for " + cacheElement.getKey());
						}
					}
					break;

				case DISK_CACHE:

					if (log.isDebugEnabled())
					{
						log.debug("Disk cache in kit list: cattr " + cacheAttr.isUseDisk());
					}

					if (cacheAttr.isUseDisk() && cacheAttr.getDiskUsagePattern() == IContextCacheAttributes.DiskUsagePattern.UPDATE
							&& cacheElement.getElementAttributes().getIsSpool())
					{
						kit.update(cacheElement);

						if (log.isDebugEnabled())
						{
							log.debug("Updated disk cache for " + cacheElement.getKey());
						}
					}
					break;

				default:
					break;
			}
		}
	}

	/**
	 * 持久化至磁盘中
	 */
	public void spoolToDisk(ICacheElement<K, V> ce)
	{
		if (!ce.getElementAttributes().getIsSpool())
		{
			handleElementEvent(ce, ElementEventType.SPOOLED_NOT_ALLOWED);
			return;
		}

		boolean diskAvailable = false;

		for (ICache<K, V> kitCache : kitCaches)
		{
			if (kitCache != null && kitCache.getCacheType() == CacheType.DISK_CACHE)
			{
				diskAvailable = true;

				if (cacheAttr.getDiskUsagePattern() == IContextCacheAttributes.DiskUsagePattern.SWAP)
				{
					try
					{
						handleElementEvent(ce, ElementEventType.SPOOLED_DISK_AVAILABLE);
						kitCache.update(ce);
					}
					catch (IOException ex)
					{
						log.error("Spool to disk cache error.", ex);
						throw new IllegalStateException(ex.getMessage());
					}

					if (log.isDebugEnabled())
					{
						log.debug("Spool to disk  for: " + ce.getKey() + " on disk cache[" + kitCache.getCacheName()
								+ "]");
					}
				}
				else
				{
					if (log.isDebugEnabled())
					{
						log.debug("CacheKit is not configured to use the DiskCache as a swap.");
					}
				}
			}
		}

		if (!diskAvailable)
		{
			try
			{
				handleElementEvent(ce, ElementEventType.SPOOLED_DISK_NOT_AVAILABLE);
			}
			catch (Exception e)
			{

			}
		}
	}

	@Override
	public ICacheElement<K, V> get(K key)
	{
		return get(key, false);
	}

	public ICacheElement<K, V> localGet(K key)
	{
		return get(key, true);
	}

	protected ICacheElement<K, V> get(K key, boolean localOnly)
	{
		ICacheElement<K, V> element = null;

		boolean found = false;

		if (log.isDebugEnabled())
		{
			log.debug("Get: key = " + key + ", localOnly = " + localOnly);
		}

		synchronized (this)
		{
			try
			{
				element = memCache.get(key);

				if (element != null)
				{
					if (isExpired(element))
					{
						if (log.isDebugEnabled())
						{
							log.debug(cacheAttr.getCacheName() + " Memory cache hit, but element expired");
						}

						doExpires(element);
						element = null;
					}
					else
					{
						if (log.isDebugEnabled())
						{
							log.debug(cacheAttr.getCacheName() + "  Memory cache hit");
						}

						hitCountMemCache.incrementAndGet();
					}

					found = true;
				}
				else
				{
					for (KitCache<K, V> kitCache : kitCaches)
					{
						if (kitCache != null)
						{
							CacheType cacheType = kitCache.getCacheType();

							if (!localOnly || cacheType == CacheType.DISK_CACHE)
							{
								if (log.isDebugEnabled())
								{
									log.debug("Get value from kit  [" + kitCache.getCacheName()
											+ "] kit cache type is:  " + cacheType);
								}

								try
								{
									element = kitCache.get(key);
								}
								catch (IOException e)
								{

								}
							}

							if (log.isDebugEnabled())
							{
								log.debug("Get cache element: " + element);
							}

							if (element != null)
							{
								if (isExpired(element))
								{
									if (log.isDebugEnabled())
									{
										log.debug(cacheAttr.getCacheName() + " -  kit cache[" + kitCache.getCacheName()
												+ "] hit, but element expired.");
									}

									doExpires(element);
									element = null;
								}
								else
								{
									if (log.isDebugEnabled())
									{
										log.debug(cacheAttr.getCacheName() + " - kit cache[" + kitCache.getCacheName()
												+ "] hit");
									}

									hitCountKitCache.incrementAndGet();
									copyKitCacheRetrievedItemToMemory(element);
								}

								found = true;

								break;
							}
						}
					}
				}
			}
			catch (IOException e)
			{
				log.error("Get element error.", e);
			}
		}

		if (!found)
		{
			missCountNotFound.incrementAndGet();

			if (log.isDebugEnabled())
			{
				log.debug(cacheAttr.getCacheName() + " - not find");
			}
		}

		if (element != null)
		{
			element.getElementAttributes().setLastAccessTimeNow();
		}

		return element;
	}

	protected void doExpires(ICacheElement<K, V> element)
	{
		missCountExpired.incrementAndGet();
		remove(element.getKey());
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys)
	{
		return getMultiple(keys, false);
	}

	public Map<K, ICacheElement<K, V>> localGetMultiple(Set<K> keys)
	{
		return getMultiple(keys, true);
	}

	protected Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys, boolean localOnly)
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		if (log.isDebugEnabled())
		{
			log.debug("Get: key = " + keys + ", localOnly = " + localOnly);
		}

		try
		{
			Map<K, ICacheElement<K, V>> elementsFromMemory = getMultipleFromMemory(keys);

			elements.putAll(elementsFromMemory);

			if (elements.size() != keys.size())
			{
				Set<K> remainingKeys = pruneKeysFound(keys, elements);

				elements.putAll(getMultipleFromKitCaches(remainingKeys, localOnly));
			}
		}
		catch (IOException e)
		{
			log.error("Get elements error.", e);
		}

		if (elements.size() != keys.size())
		{
			missCountNotFound.addAndGet(keys.size() - elements.size());

			if (log.isDebugEnabled())
			{
				log.debug(cacheAttr.getCacheName() + " - " + (keys.size() - elements.size()) + " not found.");
			}
		}

		return elements;
	}

	private Map<K, ICacheElement<K, V>> getMultipleFromMemory(Set<K> keys) throws IOException
	{
		Map<K, ICacheElement<K, V>> elementsFromMemory = memCache.getMultiple(keys);

		Iterator<ICacheElement<K, V>> iterator = new HashMap<K, ICacheElement<K, V>>(elementsFromMemory).values()
				.iterator();

		while (iterator.hasNext())
		{
			ICacheElement<K, V> element = iterator.next();

			if (element != null)
			{
				if (isExpired(element))
				{
					if (log.isDebugEnabled())
					{
						log.debug(cacheAttr.getCacheName() + " - Memory cache hit, but element expired");
					}

					doExpires(element);
					elementsFromMemory.remove(element.getKey());
				}
				else
				{
					if (log.isDebugEnabled())
					{
						log.debug(cacheAttr.getCacheName() + " - Memory cache hit");
					}

					hitCountMemCache.incrementAndGet();
				}
			}
		}
		return elementsFromMemory;
	}

	private Map<K, ICacheElement<K, V>> getMultipleFromKitCaches(Set<K> keys, boolean localOnly) throws IOException
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		Set<K> remainingKeys = new HashSet<K>(keys);

		for (KitCache<K, V> kitCache : kitCaches)
		{
			if (kitCache != null)
			{
				Map<K, ICacheElement<K, V>> elementsFromKitCache = new HashMap<K, ICacheElement<K, V>>();

				CacheType cacheType = kitCache.getCacheType();

				if (!localOnly || cacheType == CacheType.DISK_CACHE)
				{
					if (log.isDebugEnabled())
					{
						log.debug("Get cache element from kit [" + kitCache.getCacheName() + "] which is of type: "
								+ cacheType);
					}

					try
					{
						elementsFromKitCache.putAll(kitCache.getMultiple(remainingKeys));
					}
					catch (IOException e)
					{
						log.error("Get from kit error.", e);
					}
				}

				if (log.isDebugEnabled())
				{
					log.debug("Get cache elements: " + elementsFromKitCache);
				}

				processRetrievedElements(kitCache, elementsFromKitCache);

				elements.putAll(elementsFromKitCache);

				if (elements.size() == keys.size())
				{
					break;
				}
				else
				{
					remainingKeys = pruneKeysFound(keys, elements);
				}
			}
		}

		return elements;
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMatching(String pattern)
	{
		return getMatching(pattern, false);
	}

	public Map<K, ICacheElement<K, V>> localGetMatching(String pattern)
	{
		return getMatching(pattern, true);
	}

	protected Map<K, ICacheElement<K, V>> getMatching(String pattern, boolean localOnly)
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		if (log.isDebugEnabled())
		{
			log.debug("Get: pattern [" + pattern + "], localOnly = " + localOnly);
		}

		try
		{
			elements.putAll(getMatchingFromKitCaches(pattern, localOnly));

			elements.putAll(getMatchingFromMemory(pattern));
		}
		catch (Exception e)
		{
			log.error("Get cache elements error.", e);
		}

		return elements;
	}

	protected Map<K, ICacheElement<K, V>> getMatchingFromMemory(String pattern) throws IOException
	{
		Set<K> keyArray = memCache.getKeySet();

		Set<K> matchingKeys = getKeyMatcher().getMatchingKeysFromArray(pattern, keyArray);

		return getMultipleFromMemory(matchingKeys);
	}

	private Map<K, ICacheElement<K, V>> getMatchingFromKitCaches(String pattern, boolean localOnly) throws IOException
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		for (int i = kitCaches.length - 1; i >= 0; i--)
		{
			KitCache<K, V> kitCache = kitCaches[i];

			if (kitCache != null)
			{
				Map<K, ICacheElement<K, V>> elementsFromKitCache = new HashMap<K, ICacheElement<K, V>>();

				CacheType cacheType = kitCache.getCacheType();

				if (!localOnly || cacheType == CacheType.DISK_CACHE)
				{
					if (log.isDebugEnabled())
					{
						log.debug("Get pattern from kit [" + kitCache.getCacheName() + "] which is of type: "
								+ cacheType);
					}

					try
					{
						elementsFromKitCache.putAll(kitCache.getMatching(pattern));
					}
					catch (IOException e)
					{
						log.error("Error occur in getting from kit", e);
					}

					if (log.isDebugEnabled())
					{
						log.debug("Get CacheElements: " + elementsFromKitCache);
					}

					processRetrievedElements(kitCache, elementsFromKitCache);

					elements.putAll(elementsFromKitCache);
				}
			}
		}

		return elements;
	}

	private void processRetrievedElements(KitCache<K, V> kitCache, Map<K, ICacheElement<K, V>> elementsFromKitCache)
			throws IOException
	{
		Iterator<ICacheElement<K, V>> iterator = new HashMap<K, ICacheElement<K, V>>(elementsFromKitCache).values()
				.iterator();

		while (iterator.hasNext())
		{
			ICacheElement<K, V> element = iterator.next();

			if (element != null)
			{
				if (isExpired(element))
				{
					if (log.isDebugEnabled())
					{
						log.debug(cacheAttr.getCacheName() + " - kit cache[" + kitCache.getCacheName()
								+ "] hit, but element expired.");
					}

					doExpires(element);
					elementsFromKitCache.remove(element.getKey());
				}
				else
				{
					if (log.isDebugEnabled())
					{
						log.debug(cacheAttr.getCacheName() + " - kit cache[" + kitCache.getCacheName() + "] hit");
					}

					hitCountKitCache.incrementAndGet();
					copyKitCacheRetrievedItemToMemory(element);
				}
			}
		}
	}

	private void copyKitCacheRetrievedItemToMemory(ICacheElement<K, V> element) throws IOException
	{
		if (memCache.getCacheAttributes().getMaxObjects() > 0)
		{
			memCache.update(element);
		}
		else
		{
			if (log.isDebugEnabled())
			{
				log.debug("No items are allowed to copy in mem memory");
			}
		}
	}

	private Set<K> pruneKeysFound(Set<K> keys, Map<K, ICacheElement<K, V>> foundElements)
	{
		Set<K> remainingKeys = new HashSet<K>(keys);

		for (K key : foundElements.keySet())
		{
			remainingKeys.remove(key);
		}

		return remainingKeys;
	}

	public Set<K> getKeySet()
	{
		return getKeySet(false);
	}

	public Set<K> getKeySet(boolean localOnly)
	{
		HashSet<K> allKeys = new HashSet<K>();

		allKeys.addAll(memCache.getKeySet());
		for (KitCache<K, V> kitCache : kitCaches)
		{
			if (kitCache != null)
			{
				if (!localOnly || kitCache.getCacheType() == CacheType.DISK_CACHE)
				{
					try
					{
						allKeys.addAll(kitCache.getKeySet());
					}
					catch (IOException e)
					{

					}
				}
			}
		}
		return allKeys;
	}

	@Override
	public boolean remove(K key)
	{
		return remove(key, false);
	}

	public boolean localRemove(K key)
	{
		return remove(key, true);
	}

	protected boolean remove(K key, boolean localOnly)
	{
		removeCount.incrementAndGet();

		boolean removed = false;

		synchronized (this)
		{
			try
			{
				removed = memCache.remove(key);
			}
			catch (IOException e)
			{
				log.error(e);
			}

			for (ICache<K, V> kitCache : kitCaches)
			{
				if (kitCache == null)
				{
					continue;
				}

				CacheType cacheType = kitCache.getCacheType();

				if (localOnly && (cacheType == CacheType.PAXOS_CACHE || cacheType == CacheType.LATERAL_CACHE))
				{
					continue;
				}
				try
				{
					if (log.isDebugEnabled())
					{
						log.debug("Remove " + key + " from cacheType" + cacheType);
					}

					boolean b = kitCache.remove(key);

					if (!removed)
					{
						removed = b;
					}
				}
				catch (IOException ex)
				{
					log.error("Fail to remove from kit cache", ex);
				}
			}
		}

		return removed;
	}

	@Override
	public void removeAll() throws IOException
	{
		removeAll(false);
	}

	public void localRemoveAll() throws IOException
	{
		removeAll(true);
	}

	protected void removeAll(boolean localOnly) throws IOException
	{
		synchronized (this)
		{
			try
			{
				memCache.removeAll();

				if (log.isDebugEnabled())
				{
					log.debug("Remove all keys from the memory cache.");
				}
			}
			catch (IOException ex)
			{
				log.error("Remove all keys error.", ex);
			}

			for (ICache<K, V> kitCache : kitCaches)
			{
				if (kitCache != null && (kitCache.getCacheType() == CacheType.DISK_CACHE || !localOnly))
				{
					try
					{
						if (log.isDebugEnabled())
						{
							log.debug("Remove all keys from cacheType" + kitCache.getCacheType());
						}

						kitCache.removeAll();
					}
					catch (IOException ex)
					{
						log.error("Remove all from kit error", ex);
					}
				}
			}
		}
	}

	@Override
	public void dispose()
	{
		dispose(false);
	}

	public void dispose(boolean fromRemote)
	{
		if (!alive.compareAndSet(true, false))
		{
			return;
		}

		if (log.isInfoEnabled())
		{
			log.info("In dispose, [" + this.cacheAttr.getCacheName() + "] fromRemote [" + fromRemote + "]");
		}

		synchronized (this)
		{
			if (cacheManager != null)
			{
				cacheManager.freeCache(getCacheName(), fromRemote);
			}

			if (future != null)
			{
				future.cancel(true);
			}

			if (elementEventQueue != null)
			{
				elementEventQueue.dispose();
				elementEventQueue = null;
			}

			for (ICache<K, V> kit : kitCaches)
			{
				try
				{
					if (kit == null || kit.getStatus() != CacheStatus.ALIVE)
					{
						continue;
					}

					if (log.isInfoEnabled())
					{
						log.info("In dispose, [" + cacheAttr.getCacheName() + "] kit [" + kit.getCacheName() + "]");
					}

					if (kit.getCacheType() == CacheType.DISK_CACHE)
					{
						int numToFree = memCache.getSize();

						memCache.freeElements(numToFree);

						if (log.isInfoEnabled())
						{
							log.info("In dispose, [" + cacheAttr.getCacheName() + "] put " + numToFree + " into kit "
									+ kit.getCacheName());
						}
					}

					kit.dispose();
				}
				catch (IOException ex)
				{
					log.error("Dispose kit cache error.", ex);
				}
			}

			if (log.isInfoEnabled())
			{
				log.info("In dispose, [" + cacheAttr.getCacheName() + "] disposing of memory cache.");
			}
			try
			{
				memCache.dispose();
			}
			catch (IOException ex)
			{
				log.error("Dispose memCache error.", ex);
			}
		}
	}

	public void save()
	{
		if (alive.compareAndSet(true, false) == false)
		{
			return;
		}

		synchronized (this)
		{
			for (ICache<K, V> kit : kitCaches)
			{
				try
				{
					if (kit.getStatus() == CacheStatus.ALIVE)
					{
						for (K key : memCache.getKeySet())
						{
							ICacheElement<K, V> ce = memCache.get(key);

							if (ce != null)
							{
								kit.update(ce);
							}
						}
					}
				}
				catch (IOException ex)
				{
					log.error("Save kit caches error.", ex);
				}
			}
		}
		if (log.isDebugEnabled())
		{
			log.debug("Save for [" + cacheAttr.getCacheName() + "]");
		}
	}

	@Override
	public int getSize()
	{
		return memCache.getSize();
	}

	@Override
	public CacheType getCacheType()
	{
		return CacheType.CACHE_HUB;
	}

	@Override
	public CacheStatus getStatus()
	{
		return alive.get() ? CacheStatus.ALIVE : CacheStatus.DISPOSED;
	}

	@Override
	public String getStats()
	{
		return getStatistics().toString();
	}

	public ICacheStats getStatistics()
	{
		ICacheStats stats = new CacheStats();
		stats.setCacheName(this.getCacheName());

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();

		elems.add(new StatElement<Integer>("HitCountMemCache", Integer.valueOf(getHitCountMemCache())));
		elems.add(new StatElement<Integer>("HitCountKitCache", Integer.valueOf(getHitCountKitCache())));

		stats.setStatElements(elems);

		int total = kitCaches.length + 1;
		ArrayList<IStats> kitCacheStats = new ArrayList<IStats>(total);

		kitCacheStats.add(getMemoryCache().getStatistics());

		for (KitCache<K, V> kit : kitCaches)
		{
			kitCacheStats.add(kit.getStatistics());
		}

		stats.setKitCacheStats(kitCacheStats);

		return stats;
	}

	@Override
	public String getCacheName()
	{
		return cacheAttr.getCacheName();
	}

	public IElementAttributes getElementAttributes()
	{
		if (attr != null)
		{
			return attr.clone();
		}
		return null;
	}

	public void setElementAttributes(IElementAttributes attr)
	{
		this.attr = attr;
	}

	public IContextCacheAttributes getCacheAttributes()
	{
		return this.cacheAttr;
	}

	public void setCacheAttributes(IContextCacheAttributes cattr)
	{
		this.cacheAttr = cattr;
		this.memCache.initialize(this);
	}

	public IElementAttributes getElementAttributes(K key) throws CacheException, IOException
	{
		ICacheElement<K, V> ce = get(key);
		if (ce == null)
		{
			throw new ObjectNotFoundException("Key " + key + " is not found");
		}
		return ce.getElementAttributes();
	}

	public boolean isExpired(ICacheElement<K, V> element)
	{
		return isExpired(element, System.currentTimeMillis(), ElementEventType.EXCEEDED_MAXLIFE_ONREQUEST,
				ElementEventType.EXCEEDED_IDLETIME_ONREQUEST);
	}

	public boolean isExpired(ICacheElement<K, V> element, long timestamp, ElementEventType eventMaxlife,
			ElementEventType eventIdle)
	{
		try
		{
			IElementAttributes attributes = element.getElementAttributes();

			if (!attributes.getIsEternal())
			{

				long maxLifeSeconds = attributes.getMaxLife();
				long createTime = attributes.getCreateTime();

				final long timeFactorForMilliseconds = attributes.getTimeFactorForMilliseconds();

				if (maxLifeSeconds != -1 && (timestamp - createTime) > (maxLifeSeconds * timeFactorForMilliseconds))
				{
					if (log.isDebugEnabled())
					{
						log.debug("Exceed maxLife: " + element.getKey());
					}

					handleElementEvent(element, eventMaxlife);

					return true;
				}
				long idleTime = attributes.getIdleTime();
				long lastAccessTime = attributes.getLastAccessTime();

				if ((idleTime != -1) && (timestamp - lastAccessTime) > idleTime * timeFactorForMilliseconds)
				{
					if (log.isDebugEnabled())
					{
						log.debug("Exceed maxIdle: " + element.getKey());
					}

					handleElementEvent(element, eventIdle);

					return true;
				}
			}
		}
		catch (Exception e)
		{
			log.error("Handle expired element error ", e);

			return true;
		}

		return false;
	}

	/**
	 * 根据K-V对象拿出对应的事件处理器，
	 *
	 * @param element 被处理的对象实例
	 * @param eventType 被处理的对象的事件类型
	 */
	public void handleElementEvent(ICacheElement<K, V> element, ElementEventType eventType)
	{
		ArrayList<IElementEventHandler> eventHandlers = element.getElementAttributes().getElementEventHandlers();
		if (eventHandlers != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Create event type " + eventType);
			}
			if (elementEventQueue == null)
			{
				log.warn("No element event queue available for cache " + getCacheName());
				return;
			}
			IElementEvent<ICacheElement<K, V>> event = new ElementEvent<ICacheElement<K, V>>(element, eventType);
			for (IElementEventHandler hand : eventHandlers)
			{
				try
				{
					elementEventQueue.addElementEvent(hand, event);
				}
				catch (IOException e)
				{
					log.error("Add element event to queue error.", e);
				}
			}
		}
	}

	private void createMemoryCache(IContextCacheAttributes cattr)
	{
		if (memCache == null)
		{
			try
			{
				Class<?> c = Class.forName(cattr.getMemoryCacheName());
				@SuppressWarnings("unchecked")
				IMemoryCache<K, V> newInstance = (IMemoryCache<K, V>) c.newInstance();
				memCache = newInstance;
				memCache.initialize(this);
			}
			catch (Exception e)
			{
				log.warn("Fail to init mem cache.", e);

				this.memCache = new LRUMemoryCache<K, V>();
				this.memCache.initialize(this);
			}
		}
		else
		{
			log.warn("Memory cache already exists.");
		}
	}

	public IMemoryCache<K, V> getMemoryCache()
	{
		return memCache;
	}

	public int getHitCountMemCache()
	{
		return hitCountMemCache.get();
	}

	public int getHitCountKitCache()
	{
		return hitCountKitCache.get();
	}

	public int getMissCountNotFound()
	{
		return missCountNotFound.get();
	}

	public int getMissCountExpired()
	{
		return missCountExpired.get();
	}

	public int getUpdateCount()
	{
		return updateCount.get();
	}

	@Override
	public void setKeyMatcher(IKeyMatcher<K> keyMatcher)
	{
		if (keyMatcher != null)
		{
			this.keyMatcher = keyMatcher;
		}
	}

	public IKeyMatcher<K> getKeyMatcher()
	{
		return this.keyMatcher;
	}

	@Override
	public String toString()
	{
		return getStats();
	}
}
