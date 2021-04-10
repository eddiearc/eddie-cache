package pro.eddiecache.core.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author eddie
 */
public class CacheEventWrapper implements ICacheEventWrapper
{
	private String categoryName = CacheEventWrapper.class.getName();

	private Log log = LogFactory.getLog(categoryName);

	@Override
	public <T> ICacheEvent<T> createICacheEvent(String source, String cacheName, String eventName,
			String optionalDetails, T key)
	{
		ICacheEvent<T> event = new CacheEvent<T>();
		event.setSource(source);
		event.setCacheName(cacheName);
		event.setEventName(eventName);
		event.setOptionalDetails(optionalDetails);
		event.setKey(key);
		return event;
	}

	@Override
	public void applicationEventLogger(String source, String eventName, String optionalDetails)
	{
		if (log.isDebugEnabled())
		{
			log.debug(source + " | " + eventName + " | " + optionalDetails);
		}
	}

	@Override
	public void errorLogger(String source, String eventName, String errorMessage)
	{
		if (log.isDebugEnabled())
		{
			log.debug(source + " | " + eventName + " | " + errorMessage);
		}
	}

	@Override
	public <T> void cacheEventLogger(ICacheEvent<T> event)
	{
		if (log.isDebugEnabled())
		{
			log.debug(event);
		}
	}

	public synchronized void setLogCategoryName(String categoryName)
	{
		if (categoryName != null && !categoryName.equals(this.categoryName))
		{
			this.categoryName = categoryName;
			log = LogFactory.getLog(categoryName);
		}
	}
}
