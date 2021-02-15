package pro.eddiecache.core.model;

import pro.eddiecache.access.exception.ObjectExistsException;
import pro.eddiecache.access.exception.ObjectNotFoundException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface ICacheService<K, V>
{
	void update(ICacheElement<K, V> item) throws ObjectExistsException, IOException;

	ICacheElement<K, V> get(String cacheName, K key) throws ObjectNotFoundException, IOException;

	Map<K, ICacheElement<K, V>> getMultiple(String cacheName, Set<K> keys) throws ObjectNotFoundException, IOException;

	Map<K, ICacheElement<K, V>> getMatching(String cacheName, String pattern) throws IOException;

	void remove(String cacheName, K key) throws IOException;

	void removeAll(String cacheName) throws IOException;

	void dispose(String cacheName) throws IOException;

	void release() throws IOException;
}
