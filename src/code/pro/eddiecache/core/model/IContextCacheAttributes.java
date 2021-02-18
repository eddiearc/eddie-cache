package pro.eddiecache.core.model;

import java.io.Serializable;

public interface IContextCacheAttributes extends Serializable, Cloneable
{
	enum DiskUsagePattern
	{
		// 在关闭虚拟机的时候或者在持久化操作执行时，才更新至磁盘中
		SWAP,
		// 实时更新，每次update均执行
		UPDATE
	}

	//最大容量
	void setMaxObjects(int size);

	int getMaxObjects();

	//溢出刷盘
	void setUseDisk(boolean useDisk);

	boolean isUseDisk();

	//线性
	void setUseLateral(boolean d);

	boolean isUseLateral();

	//主从
	void setUseRemote(boolean isRemote);

	boolean isUseRemote();

	//学名
	void setCacheName(String s);

	String getCacheName();

	//小名
	void setMemoryCacheName(String s);

	String getMemoryCacheName();

	void setUseMemoryShrinker(boolean useShrinker);

	boolean isUseMemoryShrinker();

	void setMaxMemoryIdleTimeSeconds(long seconds);

	long getMaxMemoryIdleTimeSeconds();

	//自查间隔
	void setShrinkerIntervalSeconds(long seconds);

	long getShrinkerIntervalSeconds();

	void setMaxSpoolPerRun(int maxSpoolPerRun);

	int getMaxSpoolPerRun();

	void setDiskUsagePattern(DiskUsagePattern diskUsagePattern);

	void setDiskUsagePatternName(String diskUsagePatternName);

	DiskUsagePattern getDiskUsagePattern();

	//块的大小
	int getSpoolChunkSize();

	void setSpoolChunkSize(int spoolChunkSize);

	IContextCacheAttributes clone();
}
