package pro.eddiecache.core.logger;

import java.util.Date;

public class CacheEvent<K> implements ICacheEvent<K>
{

	private static final long serialVersionUID = 1L;

	private final long createTime = System.currentTimeMillis();

	private String source;

	private String cacheName;

	private String eventName;

	private String optionalDetails;

	private K key;

	@Override
	public void setSource(String source)
	{
		this.source = source;
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public void setCacheName(String cacheName)
	{
		this.cacheName = cacheName;
	}

	@Override
	public String getCacheName()
	{
		return cacheName;
	}

	@Override
	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}

	@Override
	public String getEventName()
	{
		return eventName;
	}

	@Override
	public void setOptionalDetails(String optionalDetails)
	{
		this.optionalDetails = optionalDetails;
	}

	@Override
	public String getOptionalDetails()
	{
		return optionalDetails;
	}

	@Override
	public void setKey(K key)
	{
		this.key = key;
	}

	@Override
	public K getKey()
	{
		return key;
	}

	public long getCreateTime()
	{
		return createTime;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("CacheEvent: ").append(eventName).append(" Created: ").append(new Date(createTime));
		if (source != null)
		{
			sb.append(" Source: ").append(source);
		}
		if (cacheName != null)
		{
			sb.append(" CacheName: ").append(cacheName);
		}
		if (key != null)
		{
			sb.append(" Key: ").append(key);
		}
		if (optionalDetails != null)
		{
			sb.append(" Details: ").append(optionalDetails);
		}
		return sb.toString();
	}
}
