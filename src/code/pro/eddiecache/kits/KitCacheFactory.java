package pro.eddiecache.kits;

import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.model.IContextCacheManager;
import pro.eddiecache.core.model.IElementSerializer;

/**
 * 缓存插件工厂类
 */
public interface KitCacheFactory
{
	<K, V> KitCache<K, V> createCache(KitCacheAttributes attr, IContextCacheManager cacheMgr,
			ICacheEventWrapper cacheEventWrapper, IElementSerializer elementSerializer) throws Exception;

	void initialize();

	void dispose();

	void setName(String s);

	String getName();
}
