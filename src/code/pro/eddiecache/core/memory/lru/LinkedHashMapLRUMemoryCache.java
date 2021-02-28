package pro.eddiecache.core.memory.lru;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheConstants;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.control.group.GroupAttrName;
import pro.eddiecache.core.memory.AbstractMemoryCache;
import pro.eddiecache.core.memory.util.MemoryElementDescriptor;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.stats.IStats;

/**
 * @author eddie
 */
public class LinkedHashMapLRUMemoryCache<K, V> extends AbstractMemoryCache<K, V>
{
	private static final Log log = LogFactory.getLog(LRUMemoryCache.class);

	@Override
	public void initialize(ContextCache<K, V> hub)
	{
		super.initialize(hub);
		log.info("Initialize LinkedHashMapLRUMemoryCache for " + getCacheName());
	}

	@Override
	public Map<K, MemoryElementDescriptor<K, V>> createMap()
	{
		return Collections.synchronizedMap(new LinkedHashMapLRUCacheCore());
	}

	@Override
	public void update(ICacheElement<K, V> ce) throws IOException
	{
		putCnt.incrementAndGet();
		map.put(ce.getKey(), new MemoryElementDescriptor<K, V>(ce));
	}

	@Override
	public ICacheElement<K, V> get(K key) throws IOException
	{
		if (log.isDebugEnabled())
		{
			log.debug("Get item from cache " + getCacheName() + " for key " + key);
		}

		MemoryElementDescriptor<K, V> me = map.get(key);

		if (me != null)
		{
			hitCnt.incrementAndGet();
			if (log.isDebugEnabled())
			{
				log.debug(getCacheName() + ": LinkedHashMapLRUMemoryCache hit for " + key);
			}
			return me.getCacheElement();
		}
		else
		{
			missCnt.incrementAndGet();
			if (log.isDebugEnabled())
			{
				log.debug(getCacheName() + ": LinkedHashMapLRUMemoryCache miss for " + key);
			}
		}

		return null;
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
			synchronized (map)
			{
				for (Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> itr = map.entrySet().iterator(); itr
						.hasNext();)
				{
					Map.Entry<K, MemoryElementDescriptor<K, V>> entry = itr.next();
					K k = entry.getKey();

					if (k instanceof String && ((String) k).startsWith(key.toString()))
					{
						itr.remove();
						removed = true;
					}
				}
			}
		}
		else if (key instanceof GroupAttrName && ((GroupAttrName<?>) key).attrName == null)
		{
			synchronized (map)
			{
				for (Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> itr = map.entrySet().iterator(); itr
						.hasNext();)
				{
					Map.Entry<K, MemoryElementDescriptor<K, V>> entry = itr.next();
					K k = entry.getKey();

					if (k instanceof GroupAttrName
							&& ((GroupAttrName<?>) k).groupId.equals(((GroupAttrName<?>) key).groupId))
					{
						itr.remove();
						removed = true;
					}
				}
			}
		}
		else
		{
			MemoryElementDescriptor<K, V> me = map.remove(key);
			if (me != null)
			{
				removed = true;
			}
		}

		return removed;
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
		stats.setTypeName("LinkedHashMapLRUMemoryCache Memory Cache");

		return stats;
	}

	public void cacheEntriesProbe()
	{
		mapProbe();
	}

	@Override
	public int freeElements(int numberToFree) throws IOException
	{
		return 0;
	}

	protected class LinkedHashMapLRUCacheCore extends java.util.LinkedHashMap<K, MemoryElementDescriptor<K, V>>
	{
		private static final long serialVersionUID = 1L;

		public LinkedHashMapLRUCacheCore()
		{
			super((int) (getCacheAttributes().getMaxObjects() * .5), .75F, true);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, MemoryElementDescriptor<K, V>> eldest)
		{
			ICacheElement<K, V> element = eldest.getValue().getCacheElement();

			if (size() <= getCacheAttributes().getMaxObjects())
			{
				return false;
			}
			else
			{

				if (log.isDebugEnabled())
				{
					log.debug("LinkedHashMapLRUCacheCore max size: " + getCacheAttributes().getMaxObjects()
							+ ".  Spooling element, key: " + element.getKey());
				}

				waterfal(element);

				if (log.isDebugEnabled())
				{
					log.debug("LinkedHashMapLRUCacheCore size: " + map.size());
				}
			}
			return true;
		}
	}
}
