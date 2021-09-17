package pro.eddiecache.core.stats;

import java.util.List;

public class CacheStats extends Stats implements ICacheStats
{

	private static final long serialVersionUID = 1L;

	private String cacheName = null;

	private List<IStats> kitStats = null;

	@Override
	public String getCacheName()
	{
		return cacheName;
	}

	@Override
	public void setCacheName(String name)
	{
		cacheName = name;
	}

	@Override
	public List<IStats> getKitCacheStats()
	{
		return kitStats;
	}

	@Override
	public void setKitCacheStats(List<IStats> stats)
	{
		kitStats = stats;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("cache name = ").append(cacheName);

		if (getStatElements() != null)
		{
			for (Object stat : getStatElements())
			{
				sb.append("\n");
				sb.append(stat);
			}
		}

		if (kitStats != null)
		{
			for (Object kitStat : kitStats)
			{
				sb.append("\n");
				sb.append("---------------------------");
				sb.append(kitStat);
			}
		}

		return sb.toString();
	}
}
