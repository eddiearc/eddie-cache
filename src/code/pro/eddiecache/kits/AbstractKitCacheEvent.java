package pro.eddiecache.kits;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import pro.eddiecache.core.logger.ICacheEvent;
import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.model.ICacheElement;

public abstract class AbstractKitCacheEvent<K, V> extends AbstractKitCache<K, V> {

	private final String cacheName;

	protected AbstractKitCacheEvent(String cacheName) {
		this.cacheName = cacheName;
	}

	@Override
	public String getCacheName() {
		return this.cacheName;
	}

	@Override
	public void update(ICacheElement<K, V> cacheElement) throws IOException
	{
		updateWithEventLogger(cacheElement);
	}

	protected final void updateWithEventLogger(ICacheElement<K, V> cacheElement) throws IOException
	{
		ICacheEvent<K> cacheEvent = createICacheEvent(cacheElement, ICacheEventWrapper.UPDATE_EVENT);
		try
		{
			processUpdate(cacheElement);
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
	}

	/**
	 * 更新缓存
	 * @param cacheElement 被更新的缓存对象
	 */
	protected abstract void processUpdate(ICacheElement<K, V> cacheElement) throws IOException;

	@Override
	public ICacheElement<K, V> get(K key) throws IOException
	{
		return getWithEventLogger(key);
	}

	protected final ICacheElement<K, V> getWithEventLogger(K key) throws IOException
	{
		ICacheEvent<K> cacheEvent = createICacheEvent(getCacheName(), key, ICacheEventWrapper.GET_EVENT);
		try
		{
			return processGet(key);
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
	}

	protected abstract ICacheElement<K, V> processGet(K key) throws IOException;

	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys) throws IOException
	{
		return getMultipleWithEventLogger(keys);
	}

	protected final Map<K, ICacheElement<K, V>> getMultipleWithEventLogger(Set<K> keys) throws IOException
	{
		ICacheEvent<Serializable> cacheEvent = createICacheEvent(getCacheName(), (Serializable) keys,
				ICacheEventWrapper.GETMULTIPLE_EVENT);
		try
		{
			return processGetMultiple(keys);
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
	}

	protected abstract Map<K, ICacheElement<K, V>> processGetMultiple(Set<K> keys) throws IOException;

	/**
	 * 根据正则匹配，匹配对应的Key，找寻对应的K-V数据
	 *
	 * @param pattern 正则表达式
	 */
	@Override
	public Map<K, ICacheElement<K, V>> getMatching(String pattern) throws IOException
	{
		return getMatchingWithEventLogger(pattern);
	}

	protected final Map<K, ICacheElement<K, V>> getMatchingWithEventLogger(String pattern) throws IOException
	{
		ICacheEvent<String> cacheEvent = createICacheEvent(getCacheName(), pattern,
				ICacheEventWrapper.GETMATCHING_EVENT);
		try
		{
			return processGetMatching(pattern);
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
	}

	/**
	 * 根据正则匹配，匹配对应的Key，找寻对应的K-V数据
	 *
	 * @param pattern 正则表达式
	 */
	protected abstract Map<K, ICacheElement<K, V>> processGetMatching(String pattern) throws IOException;

	@Override
	public boolean remove(K key) throws IOException
	{
		return removeWithEventLogger(key);
	}

	protected final boolean removeWithEventLogger(K key) throws IOException
	{
		ICacheEvent<K> cacheEvent = createICacheEvent(getCacheName(), key, ICacheEventWrapper.REMOVE_EVENT);
		try
		{
			return processRemove(key);
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
	}

	protected abstract boolean processRemove(K key) throws IOException;

	@Override
	public void removeAll() throws IOException
	{
		removeAllWithEventLogger();
	}

	protected final void removeAllWithEventLogger() throws IOException
	{
		ICacheEvent<String> cacheEvent = createICacheEvent(getCacheName(), "all", ICacheEventWrapper.REMOVEALL_EVENT);
		try
		{
			processRemoveAll();
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
	}

	protected abstract void processRemoveAll() throws IOException;

	@Override
	public void dispose() throws IOException
	{
		disposeWithEventLogger();
	}

	protected final void disposeWithEventLogger() throws IOException
	{
		ICacheEvent<String> cacheEvent = createICacheEvent(getCacheName(), "none", ICacheEventWrapper.DISPOSE_EVENT);
		try
		{
			processDispose();
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
	}

	protected abstract void processDispose() throws IOException;
}
