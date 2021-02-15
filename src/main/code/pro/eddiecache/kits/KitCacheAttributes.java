package pro.eddiecache.kits;

import java.io.Serializable;

import pro.eddiecache.core.model.ICacheEventQueue;

/**
 * 缓存插件属性封装类
 */
public interface KitCacheAttributes extends Serializable, Cloneable
{
	void setCacheName(String s);

	String getCacheName();

	void setName(String s);

	String getName();

	void setEventQueueType(ICacheEventQueue.QueueType s);

	ICacheEventQueue.QueueType getEventQueueType();

	void setEventQueuePoolName(String s);

	String getEventQueuePoolName();

	KitCacheAttributes clone();
}
