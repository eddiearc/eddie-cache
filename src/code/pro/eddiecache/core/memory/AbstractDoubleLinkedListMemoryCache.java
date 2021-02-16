package pro.eddiecache.core.memory;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheConstants;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.control.group.GroupAttrName;
import pro.eddiecache.core.memory.util.MemoryElementDescriptor;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.utils.struct.DoubleLinkedList;

public abstract class AbstractDoubleLinkedListMemoryCache<K, V> extends AbstractMemoryCache<K, V>
{
	private static final Log log = LogFactory.getLog(AbstractDoubleLinkedListMemoryCache.class);

	protected DoubleLinkedList<MemoryElementDescriptor<K, V>> list;

	@Override
	public void initialize(ContextCache<K, V> hub)
	{
		super.initialize(hub);
		list = new DoubleLinkedList<MemoryElementDescriptor<K, V>>();
		log.info("Initialize memory cache for " + getCacheName());
	}

	@Override
	public ConcurrentMap<K, MemoryElementDescriptor<K, V>> createMap()
	{
		return new ConcurrentHashMap<K, MemoryElementDescriptor<K, V>>();
	}

	@Override
	public final void update(ICacheElement<K, V> ce) throws IOException
	{
		putCnt.incrementAndGet();

		lock.lock();
		try
		{
			MemoryElementDescriptor<K, V> newNode = adjustListForUpdate(ce);

			final K key = newNode.getCacheElement().getKey();
			MemoryElementDescriptor<K, V> oldNode = map.put(key, newNode);

			if (oldNode != null && key.equals(oldNode.getCacheElement().getKey()))
			{
				list.remove(oldNode);
			}
		}
		finally
		{
			lock.unlock();
		}

		spoolIfNeeded();
	}

	protected abstract MemoryElementDescriptor<K, V> adjustListForUpdate(ICacheElement<K, V> ce) throws IOException;

	private void spoolIfNeeded() throws Error
	{
		int size = map.size();

		if (size <= this.getCacheAttributes().getMaxObjects())
		{
			return;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Memory limit reached, begin to spool");
		}

		int chunkSizeCorrected = Math.min(size, chunkSize);

		if (log.isDebugEnabled())
		{
			log.debug("Spool to disk cache, map size: " + size + ", max objects: "
					+ getCacheAttributes().getMaxObjects() + ", maximum items to spool: " + chunkSizeCorrected);
		}

		lock.lock();

		try
		{
			for (int i = 0; i < chunkSizeCorrected; i++)
			{
				ICacheElement<K, V> lastElement = spoolLastElement();

				if (lastElement == null)
				{
					break;
				}
			}

			if (log.isDebugEnabled() && map.size() != list.size())
			{
				log.debug("Update: after spool, size mismatch: map.size() = " + map.size() + ", linked list size = "
						+ list.size());
			}
		}
		finally
		{
			lock.unlock();
		}

		if (log.isDebugEnabled())
		{
			log.debug("Update: after spool map size: " + map.size() + " linked list size = " + list.size());
		}
	}

	@Override
	public final ICacheElement<K, V> get(K key) throws IOException
	{
		ICacheElement<K, V> ce = null;

		if (log.isDebugEnabled())
		{
			log.debug(getCacheName() + ": get item for key " + key);
		}

		MemoryElementDescriptor<K, V> me = map.get(key);

		if (me != null)
		{
			hitCnt.incrementAndGet();

			lock.lock();
			try
			{
				ce = me.getCacheElement();

				adjustListForGet(me);
			}
			finally
			{
				lock.unlock();
			}

			if (log.isDebugEnabled())
			{
				log.debug(getCacheName() + ": LRUMemoryCache hit for " + key);
			}
		}
		else
		{
			missCnt.incrementAndGet();

			if (log.isDebugEnabled())
			{
				log.debug(getCacheName() + ": LRUMemoryCache miss for " + key);
			}
		}

		if (log.isDebugEnabled())
		{
			verifyMemCache();
		}

		return ce;
	}

	protected abstract void adjustListForGet(MemoryElementDescriptor<K, V> me);

	@Override
	public int freeElements(int numberToFree) throws IOException
	{
		int freed = 0;

		lock.lock();

		try
		{
			for (; freed < numberToFree; freed++)
			{
				ICacheElement<K, V> element = spoolLastElement();
				if (element == null)
				{
					break;
				}
			}
		}
		finally
		{
			lock.unlock();
		}

		return freed;
	}

	private ICacheElement<K, V> spoolLastElement() throws Error
	{
		ICacheElement<K, V> toSpool = null;

		final MemoryElementDescriptor<K, V> last = list.getLast();
		if (last != null)
		{
			toSpool = last.getCacheElement();
			if (toSpool != null)
			{
				getContextCache().spoolToDisk(toSpool);
				if (map.remove(toSpool.getKey()) == null)
				{
					log.warn("Update: remove failed for key: " + toSpool.getKey());

					if (log.isDebugEnabled())
					{
						verifyMemCache();
					}
				}
			}
			else
			{
				throw new Error("Update: last cache element is null!");
			}

			list.remove(last);
		}

		return toSpool;
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
						list.remove(entry.getValue());
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
						list.remove(entry.getValue());
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
					list.remove(me);
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
		lock.lock();
		try
		{
			list.removeAll();
			map.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	protected MemoryElementDescriptor<K, V> addFirst(ICacheElement<K, V> ce)
	{
		lock.lock();
		try
		{
			MemoryElementDescriptor<K, V> me = new MemoryElementDescriptor<K, V>(ce);
			list.addFirst(me);
			if (log.isDebugEnabled())
			{
				verifyMemCache(ce.getKey());
			}
			return me;
		}
		finally
		{
			lock.unlock();
		}
	}

	protected MemoryElementDescriptor<K, V> addLast(ICacheElement<K, V> ce)
	{
		lock.lock();
		try
		{
			MemoryElementDescriptor<K, V> me = new MemoryElementDescriptor<K, V>(ce);
			list.addLast(me);
			if (log.isDebugEnabled())
			{
				verifyMemCache(ce.getKey());
			}
			return me;
		}
		finally
		{
			lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private void dumpCacheEntries()
	{
		log.debug("dumpingCacheEntries");
		for (MemoryElementDescriptor<K, V> ed = list
				.getFirst(); ed != null; ed = (MemoryElementDescriptor<K, V>) ed.next)
		{
			log.debug("dumpCacheEntries> key=" + ed.getCacheElement().getKey() + ", val="
					+ ed.getCacheElement().getVal());
		}
	}

	@SuppressWarnings("unchecked")
	private void verifyMemCache()
	{
		boolean found = false;

		log.debug("verifycache: checke linked list by key ");

		for (MemoryElementDescriptor<K, V> ed = list
				.getFirst(); ed != null; ed = (MemoryElementDescriptor<K, V>) ed.next)
		{
			K key = ed.getCacheElement().getKey();
			if (!map.containsKey(key))
			{
				log.error("verifycache[" + getCacheName() + "]: map does not contain key : " + key);

				if (key instanceof GroupAttrName)
				{
					GroupAttrName<?> name = (GroupAttrName<?>) key;
					log.error("GroupID hashcode=" + name.groupId.hashCode());
					log.error("GroupID.class=" + name.groupId.getClass());
					log.error("AttrName hashcode=" + name.attrName.hashCode());
					log.error("AttrName.class=" + name.attrName.getClass());
				}
				mapProbe();
			}
			else if (map.get(key) == null)
			{
				log.error("verifycache[" + getCacheName() + "]: linked list retrieval returned null for key: " + key);
			}
		}

		log.debug("verifycache: checke linked list by value ");

		for (MemoryElementDescriptor<K, V> ed = list
				.getFirst(); ed != null; ed = (MemoryElementDescriptor<K, V>) ed.next)
		{
			if (map.containsValue(ed) == false)
			{
				log.error("verifycache[" + getCacheName() + "]: map does not contain value : " + ed);
				mapProbe();
			}
		}

		log.debug("verifycache: checke linked list by keysets!");
		for (Object val : map.keySet())
		{
			found = false;

			for (MemoryElementDescriptor<K, V> ed = list
					.getFirst(); ed != null; ed = (MemoryElementDescriptor<K, V>) ed.next)
			{
				if (val.equals(ed.getCacheElement().getKey()))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				log.error("verifycache[" + getCacheName() + "]: key not found in list : " + val);
				dumpCacheEntries();
				if (map.containsKey(val))
				{
					log.error("verifycache: map contains key");
				}
				else
				{
					log.error("verifycache: map does not contain key");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void verifyMemCache(K key)
	{
		boolean found = false;

		for (MemoryElementDescriptor<K, V> ed = list
				.getFirst(); ed != null; ed = (MemoryElementDescriptor<K, V>) ed.next)
		{
			if (ed.getCacheElement().getKey() == key)
			{
				found = true;
				log.debug("verifycache(key) key match: " + key);
				break;
			}
		}
		if (!found)
		{
			log.error("verifycache(key)[" + getCacheName() + "], couldn't find key! : " + key);
		}
	}

	@Override
	public Set<K> getKeySet()
	{
		return new LinkedHashSet<K>(map.keySet());
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = super.getStatistics();
		stats.setTypeName("Memory Cache");

		List<IStatElement<?>> elems = stats.getStatElements();

		elems.add(new StatElement<Integer>("List Size", Integer.valueOf(list.size())));

		return stats;
	}
}
