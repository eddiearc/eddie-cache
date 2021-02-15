package pro.eddiecache.core.logger;

import java.io.Serializable;

public interface ICacheEvent<K> extends Serializable
{
	void setSource(String source);

	String getSource();

	void setCacheName(String cacheName);

	String getCacheName();

	void setEventName(String eventName);

	String getEventName();

	void setOptionalDetails(String optionalDetails);

	String getOptionalDetails();

	void setKey(K key);

	K getKey();
}
