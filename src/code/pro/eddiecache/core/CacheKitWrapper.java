package pro.eddiecache.core;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.ICache;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheListener;

/**
 * @author eddie
 * @description
 * 		对LateralCache进行包装并实现了ICacheListener接口
 * 		Listener内部维护了一个LateralCache
 * 		接口的调用实际是调用了LateralCache的方法
 */
public class CacheKitWrapper<K, V> implements ICacheListener<K, V>
{
	private static final Log log = LogFactory.getLog(CacheKitWrapper.class);

	private final ICache<K, V> cache;

	private long listenerId = 0;

	@Override
	public void setListenerId(long id) throws IOException
	{
		this.listenerId = id;
		log.debug("listenerId = " + id);
	}

	@Override
	public long getListenerId() throws IOException
	{
		return this.listenerId;
	}

	public CacheKitWrapper(ICache<K, V> cache)
	{
		this.cache = cache;
	}

	@Override
	public void handlePut(ICacheElement<K, V> item) throws IOException
	{
		try
		{
			cache.update(item);
		}
		catch (Exception e)
		{

		}
	}

	@Override
	public void handleRemove(String cacheName, K key) throws IOException
	{
		cache.remove(key);
	}

	@Override
	public void handleRemoveAll(String cacheName) throws IOException
	{
		cache.removeAll();
	}

	@Override
	public void handleDispose(String cacheName) throws IOException
	{
		cache.dispose();
	}
}
