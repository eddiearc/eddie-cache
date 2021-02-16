package pro.eddiecache.core.stats;

import java.util.List;

public interface ICacheStats extends IStats
{
	String getCacheName();

	void setCacheName(String name);

	List<IStats> getKitCacheStats();

	void setKitCacheStats(List<IStats> stats);
}
