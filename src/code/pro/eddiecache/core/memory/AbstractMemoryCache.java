package pro.eddiecache.core.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.memory.util.MemoryElementDescriptor;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IContextCacheAttributes;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;

public abstract class AbstractMemoryCache<K, V> implements IMemoryCache<K, V>
{
	private static final Log log = LogFactory.getLog(AbstractMemoryCache.class);

	private IContextCacheAttributes cacheAttributes;

	private ContextCache<K, V> cache;

	private CacheStatus status;

	protected int chunkSize;

	protected final Lock lock = new ReentrantLock();

	protected Map<K, MemoryElementDescriptor<K, V>> map;

	protected AtomicLong hitCnt;

	protected AtomicLong missCnt;

	protected AtomicLong putCnt;

	@Override
	public void initialize(ContextCache<K, V> hub)
	{
		hitCnt = new AtomicLong(0);
		missCnt = new AtomicLong(0);
		putCnt = new AtomicLong(0);

		this.cacheAttributes = hub.getCacheAttributes();
		this.chunkSize = cacheAttributes.getSpoolChunkSize();
		this.cache = hub;

		this.map = createMap();

		this.status = CacheStatus.ALIVE;
	}

	public abstract Map<K, MemoryElementDescriptor<K, V>> createMap();

	@Override
	public abstract boolean remove(K key) throws IOException;

	@Override
	public abstract ICacheElement<K, V> get(K key) throws IOException;

	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys) throws IOException
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
	public ICacheElement<K, V> getQuiet(K key) throws IOException
	{
		ICacheElement<K, V> ce = null;

		MemoryElementDescriptor<K, V> me = map.get(key);
		if (me != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(getCacheName() + ": MemoryCache quiet hit for " + key);
			}

			ce = me.getCacheElement();
		}
		else if (log.isDebugEnabled())
		{
			log.debug(getCacheName() + ": MemoryCache quiet miss for " + key);
		}

		return ce;
	}

	@Override
	public abstract void update(ICacheElement<K, V> ce) throws IOException;

	@Override
	public abstract Set<K> getKeySet();

	@Override
	public void removeAll() throws IOException
	{
		map.clear();
	}

	@Override
	public void dispose() throws IOException
	{
		removeAll();
		hitCnt.set(0);
		missCnt.set(0);
		putCnt.set(0);
		log.info("Memory Cache dispose called.");
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("Abstract Memory Cache");

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();
		stats.setStatElements(elems);

		elems.add(new StatElement<AtomicLong>("Put Count", putCnt));
		elems.add(new StatElement<AtomicLong>("Hit Count", hitCnt));
		elems.add(new StatElement<AtomicLong>("Miss Count", missCnt));
		elems.add(new StatElement<Integer>("Map Size", Integer.valueOf(getSize())));

		return stats;
	}

	@Override
	public int getSize()
	{
		return this.map.size();
	}

	public CacheStatus getStatus()
	{
		return this.status;
	}

	public String getCacheName()
	{
		String attributeCacheName = this.cacheAttributes.getCacheName();
		if (attributeCacheName != null)
		{
			return attributeCacheName;
		}
		return cache.getCacheName();
	}

	@Override
	public void waterfal(ICacheElement<K, V> ce)
	{
		this.cache.spoolToDisk(ce);
	}

	public void mapProbe()
	{
		log.debug("mapProbe");
		for (Map.Entry<K, MemoryElementDescriptor<K, V>> e : map.entrySet())
		{
			MemoryElementDescriptor<K, V> me = e.getValue();
			log.debug("mapProbe> key=" + e.getKey() + ", val=" + me.getCacheElement().getVal());
		}
	}

	@Override
	public IContextCacheAttributes getCacheAttributes()
	{
		return this.cacheAttributes;
	}

	@Override
	public void setCacheAttributes(IContextCacheAttributes cattr)
	{
		this.cacheAttributes = cattr;
	}

	@Override
	public ContextCache<K, V> getContextCache()
	{
		return this.cache;
	}
}
