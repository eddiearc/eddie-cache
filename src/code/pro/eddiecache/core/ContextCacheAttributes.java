package pro.eddiecache.core;

import pro.eddiecache.core.model.IContextCacheAttributes;

public class ContextCacheAttributes implements IContextCacheAttributes
{
	private static final long serialVersionUID = 1L;

	private static final boolean DEFAULT_USE_LATERAL = true;

	private static final boolean DEFAULT_USE_REMOTE = true;

	private static final boolean DEFAULT_USE_DISK = true;

	private static final boolean DEFAULT_USE_SHRINKER = false;

	private static final int DEFAULT_MAX_OBJECTS = 100;

	private static final int DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS = 60 * 120;

	private static final int DEFAULT_SHRINKER_INTERVAL_SECONDS = 30;

	private static final int DEFAULT_MAX_SPOOL_PER_RUN = -1;

	private static final String DEFAULT_MEMORY_CACHE_NAME = "pro.eddiecache.core.memory.lru.LRUMemoryCache";

	private static final int DEFAULT_CHUNK_SIZE = 2;

	private boolean useLateral = DEFAULT_USE_LATERAL;

	private boolean useRemote = DEFAULT_USE_REMOTE;

	private boolean useDisk = DEFAULT_USE_DISK;

	private boolean useMemoryShrinker = DEFAULT_USE_SHRINKER;

	private int maxObjs = DEFAULT_MAX_OBJECTS;

	private long maxMemoryIdleTimeSeconds = DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS;

	private long shrinkerIntervalSeconds = DEFAULT_SHRINKER_INTERVAL_SECONDS;

	private int maxSpoolPerRun = DEFAULT_MAX_SPOOL_PER_RUN;

	private String cacheName;

	private String memoryCacheName;

	private DiskUsagePattern diskUsagePattern = DiskUsagePattern.SWAP;

	private int spoolChunkSize = DEFAULT_CHUNK_SIZE;

	public ContextCacheAttributes()
	{
		super();
		memoryCacheName = DEFAULT_MEMORY_CACHE_NAME;
	}

	@Override
	public void setMaxObjects(int maxObjs)
	{
		this.maxObjs = maxObjs;
	}

	@Override
	public int getMaxObjects()
	{
		return this.maxObjs;
	}

	@Override
	public void setUseDisk(boolean useDisk)
	{
		this.useDisk = useDisk;
	}

	@Override
	public boolean isUseDisk()
	{
		return useDisk;
	}

	@Override
	public void setUseLateral(boolean b)
	{
		this.useLateral = b;
	}

	@Override
	public boolean isUseLateral()
	{
		return this.useLateral;
	}

	@Override
	public void setUseRemote(boolean useRemote)
	{
		this.useRemote = useRemote;
	}

	@Override
	public boolean isUseRemote()
	{
		return this.useRemote;
	}

	@Override
	public void setCacheName(String name)
	{
		this.cacheName = name;
	}

	@Override
	public String getCacheName()
	{
		return this.cacheName;
	}

	@Override
	public void setMemoryCacheName(String name)
	{
		this.memoryCacheName = name;
	}

	@Override
	public String getMemoryCacheName()
	{
		return this.memoryCacheName;
	}

	@Override
	public void setUseMemoryShrinker(boolean useShrinker)
	{
		this.useMemoryShrinker = useShrinker;
	}

	@Override
	public boolean isUseMemoryShrinker()
	{
		return this.useMemoryShrinker;
	}

	@Override
	public void setMaxMemoryIdleTimeSeconds(long seconds)
	{
		this.maxMemoryIdleTimeSeconds = seconds;
	}

	@Override
	public long getMaxMemoryIdleTimeSeconds()
	{
		return this.maxMemoryIdleTimeSeconds;
	}

	@Override
	public void setShrinkerIntervalSeconds(long seconds)
	{
		this.shrinkerIntervalSeconds = seconds;
	}

	@Override
	public long getShrinkerIntervalSeconds()
	{
		return this.shrinkerIntervalSeconds;
	}

	@Override
	public void setMaxSpoolPerRun(int maxSpoolPerRun)
	{
		this.maxSpoolPerRun = maxSpoolPerRun;
	}

	@Override
	public int getMaxSpoolPerRun()
	{
		return this.maxSpoolPerRun;
	}

	@Override
	public void setDiskUsagePattern(DiskUsagePattern diskUsagePattern)
	{
		this.diskUsagePattern = diskUsagePattern;
	}

	@Override
	public void setDiskUsagePatternName(String diskUsagePatternName)
	{
		if (diskUsagePatternName != null)
		{
			String name = diskUsagePatternName.toUpperCase().trim();
			if (name.startsWith("SWAP"))
			{
				this.setDiskUsagePattern(DiskUsagePattern.SWAP);
			}
			else if (name.startsWith("UPDATE"))
			{
				this.setDiskUsagePattern(DiskUsagePattern.UPDATE);
			}
		}
	}

	@Override
	public int getSpoolChunkSize()
	{
		return spoolChunkSize;
	}

	@Override
	public void setSpoolChunkSize(int spoolChunkSize)
	{
		this.spoolChunkSize = spoolChunkSize;
	}

	@Override
	public DiskUsagePattern getDiskUsagePattern()
	{
		return diskUsagePattern;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		sb.append("useLateral = ").append(useLateral);
		sb.append(", useRemote = ").append(useRemote);
		sb.append(", useDisk = ").append(useDisk);
		sb.append(", maxObjs = ").append(maxObjs);
		sb.append(", maxSpoolPerRun = ").append(maxSpoolPerRun);
		sb.append(", diskUsagePattern = ").append(diskUsagePattern);
		sb.append(", spoolChunkSize = ").append(spoolChunkSize);
		sb.append(" ]");
		return sb.toString();
	}

	@Override
	public IContextCacheAttributes clone()
	{
		try
		{
			return (IContextCacheAttributes) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("IContextCacheAttributes clone not supported.", e);
		}
	}
}
