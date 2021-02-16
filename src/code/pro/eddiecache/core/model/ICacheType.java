package pro.eddiecache.core.model;

public interface ICacheType
{
	enum CacheType
	{
		CACHE_HUB,

		DISK_CACHE,

		LATERAL_CACHE,

		REMOTE_CACHE,

		PAXOS_CACHE
	}

	CacheType getCacheType();

}
