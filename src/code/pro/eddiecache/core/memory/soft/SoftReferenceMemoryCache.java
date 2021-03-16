package pro.eddiecache.core.memory.soft;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheConstants;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.control.group.GroupAttrName;
import pro.eddiecache.core.memory.AbstractMemoryCache;
import pro.eddiecache.core.memory.util.MemoryElementDescriptor;
import pro.eddiecache.core.memory.util.SoftReferenceElementDescriptor;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;

public class SoftReferenceMemoryCache<K, V> extends AbstractMemoryCache<K, V>
{
	private static final Log log = LogFactory.getLog(SoftReferenceMemoryCache.class);

	private LinkedBlockingQueue<ICacheElement<K, V>> strongReferences;

	@Override
	public synchronized void initialize(ContextCache<K, V> hub)
	{
		super.initialize(hub);

		strongReferences = new LinkedBlockingQueue<ICacheElement<K, V>>();

		log.info("Initialized soft reference memory cache for " + getCacheName());
	}

	@Override
	public ConcurrentMap<K, MemoryElementDescriptor<K, V>> createMap()
	{
		return new ConcurrentHashMap<K, MemoryElementDescriptor<K, V>>();
	}

	@Override
	public Set<K> getKeySet()
	{
		Set<K> keys = new HashSet<K>();

		for (Map.Entry<K, MemoryElementDescriptor<K, V>> e : map.entrySet())
		{
			SoftReferenceElementDescriptor<K, V> sred = (SoftReferenceElementDescriptor<K, V>) e.getValue();

			if (sred.getCacheElement() != null)
			{
				keys.add(e.getKey());
			}
		}

		return keys;
	}

	@Override
	public int getSize()
	{
		int size = 0;
		for (MemoryElementDescriptor<K, V> me : map.values())
		{
			SoftReferenceElementDescriptor<K, V> sred = (SoftReferenceElementDescriptor<K, V>) me;

			if (sred.getCacheElement() != null)
			{
				size++;
			}
		}
		return size;
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = super.getStatistics();
		stats.setTypeName("soft reference memory cache");

		List<IStatElement<?>> elems = stats.getStatElements();
		int emptyrefs = map.size() - getSize();
		elems.add(new StatElement<Integer>("empty references", emptyrefs));
		elems.add(new StatElement<Integer>("strong references", strongReferences.size()));

		return stats;
	}

	@Override
	public boolean remove(K key) throws IOException
	{
		if (log.isDebugEnabled())
		{
			log.debug("Remove item for key: " + key);
		}

		boolean removed = false;

		if (key instanceof String && ((String) key).endsWith(CacheConstants.NAME_COMPONENT_DELIMITER))
		{
			for (Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> itr = map.entrySet().iterator(); itr.hasNext();)
			{
				Map.Entry<K, MemoryElementDescriptor<K, V>> entry = itr.next();
				K k = entry.getKey();

				if (k instanceof String && ((String) k).startsWith(key.toString()))
				{
					lock.lock();
					try
					{
						strongReferences.remove(entry.getValue().getCacheElement());
						itr.remove();
						removed = true;
					}
					finally
					{
						lock.unlock();
					}
				}
			}
		}
		else if (key instanceof GroupAttrName && ((GroupAttrName<?>) key).attrName == null)
		{
			for (Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> itr = map.entrySet().iterator(); itr.hasNext();)
			{
				Map.Entry<K, MemoryElementDescriptor<K, V>> entry = itr.next();
				K k = entry.getKey();

				if (k instanceof GroupAttrName
						&& ((GroupAttrName<?>) k).groupId.equals(((GroupAttrName<?>) key).groupId))
				{
					lock.lock();
					try
					{
						strongReferences.remove(entry.getValue().getCacheElement());
						itr.remove();
						removed = true;
					}
					finally
					{
						lock.unlock();
					}
				}
			}
		}
		else
		{
			lock.lock();
			try
			{
				MemoryElementDescriptor<K, V> me = map.remove(key);
				if (me != null)
				{
					strongReferences.remove(me.getCacheElement());
					removed = true;
				}
			}
			finally
			{
				lock.unlock();
			}
		}

		return removed;
	}

	@Override
	public void removeAll() throws IOException
	{
		super.removeAll();
		strongReferences.clear();
	}

	@Override
	public void update(ICacheElement<K, V> ce) throws IOException
	{
		putCnt.incrementAndGet();
		ce.getElementAttributes().setLastAccessTimeNow();

		lock.lock();

		try
		{
			map.put(ce.getKey(), new SoftReferenceElementDescriptor<K, V>(ce));
			strongReferences.add(ce);
			trimStrongReferences();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * 整理内存
	 * 将超出缓存大小的部分，从强引用队列中取出，并持久化到磁盘中
	 * 而使得，在JVM发送fullGC时，会将那些被持久化到磁盘中的K-V从内存中移除
	 */
	private void trimStrongReferences()
	{
		int max = getCacheAttributes().getMaxObjects();
		int startsize = strongReferences.size();

		for (int cursize = startsize; cursize > max; cursize--)
		{
			ICacheElement<K, V> ce = strongReferences.poll();
			waterfal(ce);
		}
	}

	@Override
	public ICacheElement<K, V> get(K key) throws IOException
	{
		ICacheElement<K, V> val = null;
		lock.lock();

		try
		{
			val = getQuiet(key);
			if (val != null)
			{
				val.getElementAttributes().setLastAccessTimeNow();

				strongReferences.add(val);
				trimStrongReferences();
			}
		}
		finally
		{
			lock.unlock();
		}

		if (val == null)
		{
			missCnt.incrementAndGet();
		}
		else
		{
			hitCnt.incrementAndGet();
		}

		return val;
	}

	@Override
	public int freeElements(int numberToFree) throws IOException
	{
		return 0;
	}
}
