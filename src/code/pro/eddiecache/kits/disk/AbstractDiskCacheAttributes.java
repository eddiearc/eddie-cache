package pro.eddiecache.kits.disk;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.kits.AbstractKitCacheAttributes;

public abstract class AbstractDiskCacheAttributes extends AbstractKitCacheAttributes implements IDiskCacheAttributes
{
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(AbstractDiskCacheAttributes.class);

	private File diskPath;

	private boolean allowRemoveAll = true;

	private int maxPurgatorySize = MAX_PURGATORY_SIZE_DEFAULT;

	private static final int DEFAULT_SHUTDOWN_SPOOLTIME_LIMIT = 60;

	private int shutdownSpoolTimeLimit = DEFAULT_SHUTDOWN_SPOOLTIME_LIMIT;

	private DiskLimitType diskLimitType = DiskLimitType.COUNT;

	@Override
	public void setDiskPath(String path)
	{
		setDiskPath(new File(path));
	}

	public void setDiskPath(File diskPath)
	{
		this.diskPath = diskPath;
		boolean result = this.diskPath.isDirectory();

		if (!result)
		{
			result = this.diskPath.mkdirs();
		}
		if (!result)
		{
			log.error("Fail to create directory " + diskPath);
		}
	}

	@Override
	public File getDiskPath()
	{
		return this.diskPath;
	}

	@Override
	public int getMaxPurgatorySize()
	{
		return maxPurgatorySize;
	}

	@Override
	public void setMaxPurgatorySize(int maxPurgatorySize)
	{
		this.maxPurgatorySize = maxPurgatorySize;
	}

	@Override
	public int getShutdownSpoolTimeLimit()
	{
		return this.shutdownSpoolTimeLimit;
	}

	@Override
	public void setShutdownSpoolTimeLimit(int shutdownSpoolTimeLimit)
	{
		this.shutdownSpoolTimeLimit = shutdownSpoolTimeLimit;
	}

	@Override
	public void setAllowRemoveAll(boolean allowRemoveAll)
	{
		this.allowRemoveAll = allowRemoveAll;
	}

	@Override
	public boolean isAllowRemoveAll()
	{
		return allowRemoveAll;
	}

	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		str.append("AbstractDiskCacheAttributes ");
		str.append("\n diskPath = " + getDiskPath());
		str.append("\n maxPurgatorySize   = " + getMaxPurgatorySize());
		str.append("\n allowRemoveAll   = " + isAllowRemoveAll());
		str.append("\n ShutdownSpoolTimeLimit   = " + getShutdownSpoolTimeLimit());
		return str.toString();
	}

	@Override
	public void setDiskLimitType(DiskLimitType diskLimitType)
	{
		this.diskLimitType = diskLimitType;
	}

	@Override
	public void setDiskLimitTypeName(String diskLimitTypeName)
	{
		if (diskLimitTypeName != null)
		{
			diskLimitType = DiskLimitType.valueOf(diskLimitTypeName.trim());
		}
	}

	@Override
	public DiskLimitType getDiskLimitType()
	{
		return diskLimitType;
	}
}
