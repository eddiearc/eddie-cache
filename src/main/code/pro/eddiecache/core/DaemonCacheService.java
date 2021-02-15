package pro.eddiecache.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheService;
import pro.eddiecache.core.model.IDaemon;

public class DaemonCacheService<K, V> implements ICacheService<K, V>, IDaemon
{
	private static final Log log = LogFactory.getLog(DaemonCacheService.class);

	public void put(ICacheElement<K, V> item)
	{
		if (log.isDebugEnabled())
		{
			log.debug("DaemonCacheService put for item " + item);
		}
	}

	@Override
	public void update(ICacheElement<K, V> item)
	{
	}

	@Override
	public ICacheElement<K, V> get(String cacheName, K key)
	{
		return null;
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(String cacheName, Set<K> keys)
	{
		return Collections.emptyMap();
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMatching(String cacheName, String pattern)
	{
		return Collections.emptyMap();
	}

	public Serializable get(String cacheName, K key, boolean container)
	{
		if (log.isDebugEnabled())
		{
			log.debug("DaemonCacheService get for key [" + key + "] cacheName [" + cacheName + "] container ["
					+ container + "]");
		}
		return null;
	}

	@Override
	public void remove(String cacheName, K key)
	{
	}

	@Override
	public void removeAll(String cacheName)
	{
	}

	@Override
	public void dispose(String cacheName)
	{
	}

	@Override
	public void release()
	{
	}
}
