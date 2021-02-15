package pro.eddiecache.kits;

import pro.eddiecache.core.model.ICacheEventQueue;

public abstract class AbstractKitCacheAttributes implements KitCacheAttributes
{
	private static final long serialVersionUID = 1L;

	private String cacheName;

	private String name;

	private ICacheEventQueue.QueueType eventQueueType;

	private String eventQueuePoolName;

	@Override
	public void setCacheName(String name)
	{
		this.cacheName = name;
	}

	@Override
	public String getCacheName()
	{
		return this.cacheName;
	}

	@Override
	public void setName(String s)
	{
		this.name = s;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public void setEventQueueType(ICacheEventQueue.QueueType queueType)
	{
		this.eventQueueType = queueType;
	}

	@Override
	public ICacheEventQueue.QueueType getEventQueueType()
	{
		return eventQueueType;
	}

	@Override
	public void setEventQueuePoolName(String s)
	{
		eventQueuePoolName = s;
	}

	@Override
	public String getEventQueuePoolName()
	{
		return eventQueuePoolName;
	}

	@Override
	public AbstractKitCacheAttributes clone()
	{
		try
		{
			return (AbstractKitCacheAttributes) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("Clone not supported. This should never happen.", e);
		}
	}
}
