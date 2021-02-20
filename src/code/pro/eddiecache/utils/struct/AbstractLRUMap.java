package pro.eddiecache.utils.struct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.control.group.GroupAttrName;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;

public abstract class AbstractLRUMap<K, V> implements Map<K, V>
{
	private static final Log log = LogFactory.getLog(AbstractLRUMap.class);

	private final DoubleLinkedList<LRUElementDescriptor<K, V>> list;

	private Map<K, LRUElementDescriptor<K, V>> map;

	int hitCnt = 0;

	int missCnt = 0;

	int putCnt = 0;

	private int chunkSize = 1;

	private final Lock lock = new ReentrantLock();

	public AbstractLRUMap()
	{
		list = new DoubleLinkedList<LRUElementDescriptor<K, V>>();

		map = new ConcurrentHashMap<K, LRUElementDescriptor<K, V>>();
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public void clear()
	{
		lock.lock();
		try
		{
			map.clear();
			list.removeAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public Collection<V> values()
	{
		List<V> valueList = new ArrayList<V>(map.size());

		for (LRUElementDescriptor<K, V> value : map.values())
		{
			valueList.add(value.getPayload());
		}

		return valueList;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> source)
	{
		if (source != null)
		{
			for (Map.Entry<? extends K, ? extends V> entry : source.entrySet())
			{
				this.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public V get(Object key)
	{
		V retVal = null;

		if (log.isDebugEnabled())
		{
			log.debug("get item  for key " + key);
		}

		LRUElementDescriptor<K, V> me = map.get(key);

		if (me != null)
		{
			hitCnt++;
			if (log.isDebugEnabled())
			{
				log.debug("LRUMap hit for " + key);
			}

			retVal = me.getPayload();

			list.makeFirst(me);
		}
		else
		{
			missCnt++;
			log.debug("LRUMap miss for " + key);
		}

		// verifyCache();
		return retVal;
	}

	public V getQuiet(Object key)
	{
		V ce = null;

		LRUElementDescriptor<K, V> me = map.get(key);
		if (me != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("LRUMap quiet hit for " + key);
			}

			ce = me.getPayload();
		}
		else if (log.isDebugEnabled())
		{
			log.debug("LRUMap quiet miss for " + key);
		}

		return ce;
	}

	@Override
	public V remove(Object key)
	{
		if (log.isDebugEnabled())
		{
			log.debug("remove item for key: " + key);
		}

		lock.lock();
		try
		{
			LRUElementDescriptor<K, V> me = map.remove(key);

			if (me != null)
			{
				list.remove(me);
				return me.getPayload();
			}
		}
		finally
		{
			lock.unlock();
		}

		return null;
	}

	@Override
	public V put(K key, V value)
	{
		putCnt++;

		LRUElementDescriptor<K, V> old = null;
		lock.lock();
		try
		{
			addFirst(key, value);
			LRUElementDescriptor<K, V> first = list.getFirst();
			old = map.put(first.getKey(), first);

			if (old != null && first.getKey().equals(old.getKey()))
			{
				list.remove(old);
			}
		}
		finally
		{
			lock.unlock();
		}

		if (shouldRemove())
		{
			if (log.isDebugEnabled())
			{
				log.debug("In memory limit reached, removing least recently used.");
			}

			while (shouldRemove())
			{
				lock.lock();
				try
				{
					LRUElementDescriptor<K, V> last = list.getLast();
					if (last != null)
					{
						processRemovedLRU(last.getKey(), last.getPayload());
						if (map.remove(last.getKey()) == null)
						{
							log.warn("update: remove failed for key: " + last.getKey());
							verifyCache();
						}
						list.removeLast();
					}
					else
					{
						verifyCache();
						throw new Error("update: last is null!");
					}
				}
				finally
				{
					lock.unlock();
				}
			}

			if (log.isDebugEnabled())
			{
				log.debug("update: After spool map size: " + map.size());
			}
			if (map.size() != dumpCacheSize())
			{
				log.error("update: After spool, size mismatch: map.size() = " + map.size() + ", linked list size = "
						+ dumpCacheSize());
			}
		}

		if (old != null)
		{
			return old.getPayload();
		}
		return null;
	}

	/**
	 * 是否应该进行移除（LRU算法是否运行的依据）
	 */
	protected abstract boolean shouldRemove();

	private void addFirst(K key, V val)
	{
		lock.lock();
		try
		{
			LRUElementDescriptor<K, V> me = new LRUElementDescriptor<K, V>(key, val);
			list.addFirst(me);
		}
		finally
		{
			lock.unlock();
		}
	}

	private int dumpCacheSize()
	{
		return list.size();
	}

	@SuppressWarnings("unchecked")
	public void dumpCacheEntries()
	{
		log.debug("dumpingCacheEntries");
		for (LRUElementDescriptor<K, V> me = list.getFirst(); me != null; me = (LRUElementDescriptor<K, V>) me.next)
		{
			if (log.isDebugEnabled())
			{
				log.debug("dumpCacheEntries> key=" + me.getKey() + ", val=" + me.getPayload());
			}
		}
	}

	public void dumpMap()
	{
		log.debug("dumpingMap");
		for (Map.Entry<K, LRUElementDescriptor<K, V>> e : map.entrySet())
		{
			LRUElementDescriptor<K, V> me = e.getValue();
			if (log.isDebugEnabled())
			{
				log.debug("dumpMap> key=" + e.getKey() + ", val=" + me.getPayload());
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void verifyCache()
	{
		if (!log.isDebugEnabled())
		{
			return;
		}

		boolean found = false;
		log.debug("verifycache: mapContains " + map.size() + " elements, linked list contains " + dumpCacheSize()
				+ " elements");
		log.debug("verifycache: checking linked list by key ");
		for (LRUElementDescriptor<K, V> li = list.getFirst(); li != null; li = (LRUElementDescriptor<K, V>) li.next)
		{
			K key = li.getKey();
			if (!map.containsKey(key))
			{
				log.error("verifycache: map does not contain key : " + li.getKey());
				log.error("li.hashcode=" + li.getKey().hashCode());
				log.error("key class=" + key.getClass());
				log.error("key hashcode=" + key.hashCode());
				log.error("key toString=" + key.toString());
				if (key instanceof GroupAttrName)
				{
					GroupAttrName<?> name = (GroupAttrName<?>) key;
					log.error("GroupID hashcode=" + name.groupId.hashCode());
					log.error("GroupID.class=" + name.groupId.getClass());
					log.error("AttrName hashcode=" + name.attrName.hashCode());
					log.error("AttrName.class=" + name.attrName.getClass());
				}
				dumpMap();
			}
			else if (map.get(li.getKey()) == null)
			{
				log.error("verifycache: linked list retrieval returned null for key: " + li.getKey());
			}
		}

		log.debug("verifycache: checking linked list by value ");
		for (LRUElementDescriptor<K, V> li3 = list.getFirst(); li3 != null; li3 = (LRUElementDescriptor<K, V>) li3.next)
		{
			if (map.containsValue(li3) == false)
			{
				log.error("verifycache: map does not contain value : " + li3);
				dumpMap();
			}
		}

		log.debug("verifycache: checking via keysets!");
		for (Iterator<K> itr2 = map.keySet().iterator(); itr2.hasNext();)
		{
			found = false;
			Serializable val = null;
			try
			{
				val = (Serializable) itr2.next();
			}
			catch (NoSuchElementException nse)
			{
				log.error("verifycache: no such element exception");
				continue;
			}

			for (LRUElementDescriptor<K, V> li2 = list
					.getFirst(); li2 != null; li2 = (LRUElementDescriptor<K, V>) li2.next)
			{
				if (val.equals(li2.getKey()))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				log.error("verifycache: key not found in list : " + val);
				dumpCacheEntries();
				if (map.containsKey(val))
				{
					log.error("verifycache: map contains key");
				}
				else
				{
					log.error("verifycache: map does NOT contain key, what the HECK!");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void verifyCache(Object key)
	{
		if (!log.isDebugEnabled())
		{
			return;
		}

		boolean found = false;

		for (LRUElementDescriptor<K, V> li = list.getFirst(); li != null; li = (LRUElementDescriptor<K, V>) li.next)
		{
			if (li.getKey() == key)
			{
				found = true;
				log.debug("verifycache(key) key match: " + key);
				break;
			}
		}
		if (!found)
		{
			log.error("verifycache(key), couldn't find key! : " + key);
		}
	}

	/**
	 * 使用LRU算法进行移除
	 */
	protected void processRemovedLRU(K key, V value)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Removing key: [" + key + "] from LRUMap store, value = [" + value + "]");
			log.debug("LRUMap store size: '" + this.size() + "'.");
		}
	}

	public void setChunkSize(int chunkSize)
	{
		this.chunkSize = chunkSize;
	}

	public int getChunkSize()
	{
		return chunkSize;
	}

	public IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("LRUMap");

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();

		elems.add(new StatElement<Integer>("List Size", list.size()));
		elems.add(new StatElement<Integer>("Map Size", map.size()));
		elems.add(new StatElement<Integer>("Put Count", putCnt));
		elems.add(new StatElement<Integer>("Hit Count", hitCnt));
		elems.add(new StatElement<Integer>("Miss Count", missCnt));

		stats.setStatElements(elems);

		return stats;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet()
	{
		lock.lock();
		try
		{
			Set<Map.Entry<K, LRUElementDescriptor<K, V>>> entries = map.entrySet();
			Set<Map.Entry<K, V>> unWrapped = new HashSet<Map.Entry<K, V>>();

			for (Map.Entry<K, LRUElementDescriptor<K, V>> pre : entries)
			{
				Map.Entry<K, V> post = new LRUMapEntry<K, V>(pre.getKey(), pre.getValue().getPayload());
				unWrapped.add(post);
			}

			return unWrapped;
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public Set<K> keySet()
	{
		return map.keySet();
	}

}
