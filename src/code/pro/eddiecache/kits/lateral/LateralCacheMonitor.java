package pro.eddiecache.kits.lateral;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.DaemonCacheServiceRemote;
import pro.eddiecache.core.model.ICacheServiceRemote;
import pro.eddiecache.kits.AbstractKitCacheMonitor;
import pro.eddiecache.kits.lateral.tcp.ITCPLateralCacheAttributes;
import pro.eddiecache.kits.lateral.tcp.LateralTCPCacheFactory;

public class LateralCacheMonitor extends AbstractKitCacheMonitor
{
	private ConcurrentHashMap<String, LateralCacheAsync<?, ?>> caches;

	private LateralTCPCacheFactory factory;

	protected static void forceShortIdlePeriod(long idlePeriod)
	{
		LateralCacheMonitor.idlePeriod = idlePeriod;
	}

	public LateralCacheMonitor(LateralTCPCacheFactory factory)
	{
		super("CacheKitLateralCacheMonitor");
		this.factory = factory;
		this.caches = new ConcurrentHashMap<String, LateralCacheAsync<?, ?>>();
		setIdlePeriod(20000L);
	}

	public void addCache(LateralCacheAsync<?, ?> cache)
	{
		this.caches.put(cache.getCacheName(), cache);

		if (this.getState() == Thread.State.NEW)
		{
			this.start();
		}
	}

	@Override
	public void dispose()
	{
		this.caches.clear();
	}

	@Override
	public void doWork()
	{

		for (Map.Entry<String, LateralCacheAsync<?, ?>> entry : caches.entrySet())
		{
			String cacheName = entry.getKey();

			@SuppressWarnings("unchecked")
			LateralCacheAsync<Object, Object> cache = (LateralCacheAsync<Object, Object>) entry.getValue();
			if (cache.getStatus() == CacheStatus.ERROR)
			{
				log.info("Find LateralCacheAsync in error, " + cacheName);

				ITCPLateralCacheAttributes attr = (ITCPLateralCacheAttributes) cache.getKitCacheAttributes();

				ICacheServiceRemote<Object, Object> cacheService = factory.getCacheServiceRemoteInstance(attr);

				if (cacheService instanceof DaemonCacheServiceRemote)
				{
					continue;
				}

				cache.fixCache(cacheService);
			}
		}
	}
}
