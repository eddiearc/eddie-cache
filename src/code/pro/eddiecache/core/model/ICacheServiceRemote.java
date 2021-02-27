package pro.eddiecache.core.model;

import java.io.IOException;
import java.rmi.Remote;
import java.util.Map;
import java.util.Set;

/**
 * @author eddie
 * 对远程服务的操作
 */
public interface ICacheServiceRemote<K, V> extends Remote, ICacheService<K, V>
{
	void update(ICacheElement<K, V> item, long requesterId) throws IOException;

	void remove(String cacheName, K key, long requesterId) throws IOException;

	void removeAll(String cacheName, long requesterId) throws IOException;

	ICacheElement<K, V> get(String cacheName, K key, long requesterId) throws IOException;

	Map<K, ICacheElement<K, V>> getMultiple(String cacheName, Set<K> keys, long requesterId) throws IOException;

	Map<K, ICacheElement<K, V>> getMatching(String cacheName, String pattern, long requesterId) throws IOException;

	Set<K> getKeySet(String cacheName) throws IOException;
}
