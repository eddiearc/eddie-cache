package pro.eddiecache.kits.disk;

import java.io.File;

import pro.eddiecache.kits.KitCacheAttributes;

public interface IDiskCacheAttributes extends KitCacheAttributes
{
	enum DiskLimitType
	{
		COUNT, SIZE
	}

	int MAX_PURGATORY_SIZE_DEFAULT = 5000;

	void setDiskPath(String path);

	File getDiskPath();

	int getMaxPurgatorySize();

	void setMaxPurgatorySize(int maxPurgatorySize);

	int getShutdownSpoolTimeLimit();

	void setShutdownSpoolTimeLimit(int shutdownSpoolTimeLimit);

	boolean isAllowRemoveAll();

	void setAllowRemoveAll(boolean allowRemoveAll);

	void setDiskLimitType(DiskLimitType diskLimitType);

	void setDiskLimitTypeName(String diskLimitTypeName);

	DiskLimitType getDiskLimitType();
}
