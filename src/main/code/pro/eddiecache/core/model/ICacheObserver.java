package pro.eddiecache.core.model;

import java.io.IOException;

public interface ICacheObserver
{
	<K, V> void addCacheListener(String cacheName, ICacheListener<K, V> listener) throws IOException;

	<K, V> void addCacheListener(ICacheListener<K, V> listener) throws IOException;

	<K, V> void removeCacheListener(String cacheName, ICacheListener<K, V> listener) throws IOException;

	<K, V> void removeCacheListener(ICacheListener<K, V> listener) throws IOException;
}
