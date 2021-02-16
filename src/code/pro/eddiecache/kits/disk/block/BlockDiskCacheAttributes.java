package pro.eddiecache.kits.disk.block;

import pro.eddiecache.kits.disk.AbstractDiskCacheAttributes;

public class BlockDiskCacheAttributes extends AbstractDiskCacheAttributes
{
	private static final long serialVersionUID = 1L;

	private int blockSizeBytes;

	private static final int DEFAULT_MAX_KEY_SIZE = 5000;

	private int maxKeySize = DEFAULT_MAX_KEY_SIZE;

	private static final long DEFAULT_KEY_PERSISTENCE_INTERVAL_SECONDS = 5 * 60;

	private long keyPersistenceIntervalSeconds = DEFAULT_KEY_PERSISTENCE_INTERVAL_SECONDS;

	public void setBlockSizeBytes(int blockSizeBytes)
	{
		this.blockSizeBytes = blockSizeBytes;
	}

	public int getBlockSizeBytes()
	{
		return blockSizeBytes;
	}

	public void setMaxKeySize(int maxKeySize)
	{
		this.maxKeySize = maxKeySize;
	}

	public int getMaxKeySize()
	{
		return maxKeySize;
	}

	public void setKeyPersistenceIntervalSeconds(long keyPersistenceIntervalSeconds)
	{
		this.keyPersistenceIntervalSeconds = keyPersistenceIntervalSeconds;
	}

	public long getKeyPersistenceIntervalSeconds()
	{
		return keyPersistenceIntervalSeconds;
	}

	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		str.append("\n BlockDiskAttributes ");
		str.append("\n DiskPath [" + this.getDiskPath() + "]");
		str.append("\n MaxKeySize [" + this.getMaxKeySize() + "]");
		str.append("\n MaxPurgatorySize [" + this.getMaxPurgatorySize() + "]");
		str.append("\n BlockSizeBytes [" + this.getBlockSizeBytes() + "]");
		str.append("\n KeyPersistenceIntervalSeconds [" + this.getKeyPersistenceIntervalSeconds() + "]");
		str.append("\n DiskLimitType [" + this.getDiskLimitType() + "]");
		return str.toString();
	}
}
