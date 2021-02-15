package pro.eddiecache.kits.disk.block;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.model.IContextCacheManager;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.kits.AbstractKitCacheFactory;
import pro.eddiecache.kits.KitCacheAttributes;

public class BlockDiskCacheFactory extends AbstractKitCacheFactory
{
	private static final Log log = LogFactory.getLog(BlockDiskCacheFactory.class);

	@Override
	public <K, V> BlockDiskCache<K, V> createCache(KitCacheAttributes attr, IContextCacheManager cacheMgr,
			ICacheEventWrapper cacheEventWrapper, IElementSerializer elementSerializer)
	{
		BlockDiskCacheAttributes bdca = (BlockDiskCacheAttributes) attr;
		if (log.isDebugEnabled())
		{
			log.debug("Create DiskCache for attributes = " + bdca);
		}

		BlockDiskCache<K, V> cache = new BlockDiskCache<K, V>(bdca, elementSerializer);
		cache.setCacheEventLogger(cacheEventWrapper);

		return cache;
	}
}
