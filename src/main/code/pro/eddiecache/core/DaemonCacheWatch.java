package pro.eddiecache.core;

import pro.eddiecache.core.model.ICacheListener;
import pro.eddiecache.core.model.ICacheObserver;
import pro.eddiecache.core.model.IDaemon;

public class DaemonCacheWatch implements ICacheObserver, IDaemon
{

	@Override
	public <K, V> void addCacheListener(String cacheName, ICacheListener<K, V> obj)
	{

	}

	@Override
	public <K, V> void addCacheListener(ICacheListener<K, V> obj)
	{

	}

	@Override
	public <K, V> void removeCacheListener(String cacheName, ICacheListener<K, V> obj)
	{

	}

	@Override
	public <K, V> void removeCacheListener(ICacheListener<K, V> obj)
	{

	}
}
