package pro.eddiecache.kits;

import java.io.IOException;
import java.util.Set;

import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.model.ICache;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.core.stats.IStats;

/**
 * 缓存插件接口
 */

public interface KitCache<K, V> extends ICache<K, V>
{
	Set<K> getKeySet() throws IOException;

	IStats getStatistics();

	KitCacheAttributes getKitCacheAttributes();

	void setElementSerializer(IElementSerializer elementSerializer);

	void setCacheEventLogger(ICacheEventWrapper cacheEventWrapper);
}
