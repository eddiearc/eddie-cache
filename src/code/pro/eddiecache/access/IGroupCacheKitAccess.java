package pro.eddiecache.access;

import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.core.model.IElementAttributes;

import java.util.Set;

public interface IGroupCacheKitAccess<K, V> extends ICacheKitAccessManagement
{
	V getFromGroup(K name, String group);

	void putInGroup(K key, String group, V obj) throws CacheException;

	void putInGroup(K key, String group, V obj, IElementAttributes attr) throws CacheException;

	void removeFromGroup(K name, String group);

	Set<K> getGroupKeys(String group);

	void invalidateGroup(String group);
}
