package pro.eddiecache.core.model;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.match.IKeyMatcher;

public interface ICache<K, V> extends ICacheType {
	void update(ICacheElement<K, V> element) throws IOException;

	ICacheElement<K, V> get(K key) throws IOException;

	Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys) throws IOException;

	Map<K, ICacheElement<K, V>> getMatching(String pattern) throws IOException;

	boolean remove(K key) throws IOException;

	void removeAll() throws IOException;

	void dispose() throws IOException;

	int getSize();

	CacheStatus getStatus();

	String getStats();

	String getCacheName();

	void setKeyMatcher(IKeyMatcher<K> keyMatcher);
}
