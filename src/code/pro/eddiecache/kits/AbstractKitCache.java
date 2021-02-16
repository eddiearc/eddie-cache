package pro.eddiecache.kits;

import pro.eddiecache.core.logger.CacheEvent;
import pro.eddiecache.core.logger.ICacheEvent;
import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.match.IKeyMatcher;
import pro.eddiecache.core.match.KeyMatcher;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.utils.serialization.StandardSerializer;

public abstract class AbstractKitCache<K, V> implements KitCache<K, V>
{
	private ICacheEventWrapper cacheEventWrapper;

	private IElementSerializer elementSerializer = new StandardSerializer();

	private IKeyMatcher<K> keyMatcher = new KeyMatcher<K>();

	protected ICacheEvent<K> createICacheEvent(ICacheElement<K, V> item, String eventName)
	{
		if (cacheEventWrapper == null)
		{
			return new CacheEvent<K>();
		}
		String diskLocation = getEventLoggerExtraInfo();
		String regionName = null;
		K key = null;
		if (item != null)
		{
			regionName = item.getCacheName();
			key = item.getKey();
		}
		return cacheEventWrapper.createICacheEvent(getKitCacheAttributes().getName(), regionName, eventName,
				diskLocation, key);
	}

	protected <T> ICacheEvent<T> createICacheEvent(String regionName, T key, String eventName)
	{
		if (cacheEventWrapper == null)
		{
			return new CacheEvent<T>();
		}
		String diskLocation = getEventLoggerExtraInfo();
		return cacheEventWrapper.createICacheEvent(getKitCacheAttributes().getName(), regionName, eventName,
				diskLocation, key);

	}

	protected <T> void cacheEventLogger(ICacheEvent<T> cacheEvent)
	{
		if (cacheEventWrapper != null)
		{
			cacheEventWrapper.cacheEventLogger(cacheEvent);
		}
	}

	protected void applicationEventLogger(String source, String eventName, String optionalDetails)
	{
		if (cacheEventWrapper != null)
		{
			cacheEventWrapper.applicationEventLogger(source, eventName, optionalDetails);
		}
	}

	protected void errorLogger(String source, String eventName, String errorMessage)
	{
		if (cacheEventWrapper != null)
		{
			cacheEventWrapper.errorLogger(source, eventName, errorMessage);
		}
	}

	public abstract String getEventLoggerExtraInfo();

	@Override
	public void setCacheEventLogger(ICacheEventWrapper cacheEventWrapper)
	{
		this.cacheEventWrapper = cacheEventWrapper;
	}

	public ICacheEventWrapper getCacheEventLogger()
	{
		return this.cacheEventWrapper;
	}

	@Override
	public void setElementSerializer(IElementSerializer elementSerializer)
	{
		if (elementSerializer != null)
		{
			this.elementSerializer = elementSerializer;
		}
	}

	public IElementSerializer getElementSerializer()
	{
		return this.elementSerializer;
	}

	@Override
	public void setKeyMatcher(IKeyMatcher<K> keyMatcher)
	{
		if (keyMatcher != null)
		{
			this.keyMatcher = keyMatcher;
		}
	}

	public IKeyMatcher<K> getKeyMatcher()
	{
		return this.keyMatcher;
	}
}
