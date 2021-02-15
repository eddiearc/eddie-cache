package pro.eddiecache.kits.paxos;

import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.model.IContextCacheManager;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.kits.AbstractKitCacheFactory;
import pro.eddiecache.kits.KitCache;
import pro.eddiecache.kits.KitCacheAttributes;

public class PaxosCacheFactory extends AbstractKitCacheFactory
{

	@Override
	public <K, V> KitCache<K, V> createCache(KitCacheAttributes attr, IContextCacheManager cacheMgr,
			ICacheEventWrapper cacheEventWrapper, IElementSerializer elementSerializer) throws Exception
	{
		PaxosCache paxos = new PaxosCache((PaxosCacheAttributes) attr);
		return paxos;
	}

}
