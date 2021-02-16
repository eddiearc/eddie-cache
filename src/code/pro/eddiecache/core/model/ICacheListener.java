package pro.eddiecache.core.model;

import java.io.IOException;

public interface ICacheListener<K, V>
{
	void handlePut(ICacheElement<K, V> item) throws IOException;

	void handleRemove(String cacheName, K key) throws IOException;

	void handleRemoveAll(String cacheName) throws IOException;

	void handleDispose(String cacheName) throws IOException;

	void setListenerId(long id) throws IOException;

	long getListenerId() throws IOException;
}
