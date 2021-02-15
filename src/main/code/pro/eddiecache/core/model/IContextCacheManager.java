package pro.eddiecache.core.model;

import java.util.Properties;

import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.kits.KitCache;

public interface IContextCacheManager extends IShutdownObservable
{
	<K, V> ContextCache<K, V> getCache(String cacheName);

	<K, V> KitCache<K, V> getKitCache(String kitName, String cacheName);

	Properties getConfigurationProperties();

	String getStats();
}
