package pro.eddiecache.kits.disk.indexed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.model.IContextCacheManager;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.kits.AbstractKitCacheFactory;
import pro.eddiecache.kits.KitCacheAttributes;

public class IndexedDiskCacheFactory extends AbstractKitCacheFactory
{
	private static final Log log = LogFactory.getLog(IndexedDiskCacheFactory.class);

	@Override
	public <K, V> IndexedDiskCache<K, V> createCache(KitCacheAttributes cattr, IContextCacheManager cacheMgr,
			ICacheEventWrapper cacheEventWrapper, IElementSerializer elementSerializer)
	{
		IndexedDiskCacheAttributes idcattr = (IndexedDiskCacheAttributes) cattr;

		if (log.isDebugEnabled())
		{
			log.debug("Create DiskCache for attributes = " + idcattr);
		}

		IndexedDiskCache<K, V> cache = new IndexedDiskCache<K, V>(idcattr, elementSerializer);

		cache.setCacheEventLogger(cacheEventWrapper);

		return cache;
	}
}
