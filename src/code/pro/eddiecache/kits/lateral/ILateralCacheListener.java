package pro.eddiecache.kits.lateral;

import pro.eddiecache.core.model.ICacheListener;
import pro.eddiecache.core.model.IContextCacheManager;

public interface ILateralCacheListener<K, V> extends ICacheListener<K, V>
{
	void init();

	void setCacheManager(IContextCacheManager cacheMgr);

	IContextCacheManager getCacheManager();

	void dispose();
}
