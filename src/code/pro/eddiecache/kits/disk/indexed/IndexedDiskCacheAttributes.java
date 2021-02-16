package pro.eddiecache.kits.disk.indexed;

import pro.eddiecache.kits.disk.AbstractDiskCacheAttributes;

public class IndexedDiskCacheAttributes extends AbstractDiskCacheAttributes
{
	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_MAX_KEY_SIZE = 5000;

	private int maxKeySize = DEFAULT_MAX_KEY_SIZE;

	private int optimizeAtRemoveCount = -1;

	public static final boolean DEFAULT_OPTIMIZE_ON_SHUTDOWN = true;

	private boolean optimizeOnShutdown = DEFAULT_OPTIMIZE_ON_SHUTDOWN;

	public static final boolean DEFAULT_CLEAR_DISK_ON_STARTUP = false;

	private boolean clearDiskOnStartup = DEFAULT_CLEAR_DISK_ON_STARTUP;

	public IndexedDiskCacheAttributes()
	{
		super();
	}

	public int getMaxKeySize()
	{
		return this.maxKeySize;
	}

	public void setMaxKeySize(int maxKeySize)
	{
		this.maxKeySize = maxKeySize;
	}

	public int getOptimizeAtRemoveCount()
	{
		return this.optimizeAtRemoveCount;
	}

	public void setOptimizeAtRemoveCount(int cnt)
	{
		this.optimizeAtRemoveCount = cnt;
	}

	public void setOptimizeOnShutdown(boolean optimizeOnShutdown)
	{
		this.optimizeOnShutdown = optimizeOnShutdown;
	}

	public boolean isOptimizeOnShutdown()
	{
		return optimizeOnShutdown;
	}

	public void setClearDiskOnStartup(boolean clearDiskOnStartup)
	{
		this.clearDiskOnStartup = clearDiskOnStartup;
	}

	public boolean isClearDiskOnStartup()
	{
		return clearDiskOnStartup;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("IndexedDiskCacheAttributes ");
		sb.append("\n diskPath = " + super.getDiskPath());
		sb.append("\n maxPurgatorySize   = " + super.getMaxPurgatorySize());
		sb.append("\n maxKeySize  = " + maxKeySize);
		sb.append("\n optimizeAtRemoveCount  = " + optimizeAtRemoveCount);
		sb.append("\n shutdownSpoolTimeLimit  = " + super.getShutdownSpoolTimeLimit());
		sb.append("\n optimizeOnShutdown  = " + optimizeOnShutdown);
		sb.append("\n clearDiskOnStartup  = " + clearDiskOnStartup);
		return sb.toString();
	}
}
