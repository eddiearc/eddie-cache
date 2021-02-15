package pro.eddiecache.utils.threadpool;

public final class PoolConfiguration implements Cloneable
{
	private boolean useBoundary = true;

	private int boundarySize = 2000;

	private int maximumPoolSize = 150;

	private int minimumPoolSize = 4;

	private int keepAliveTime = 1000 * 60 * 5;

	public enum WhenBlockedPolicy
	{
		ABORT,

		BLOCK,

		RUN,

		WAIT,

		DISCARDOLDEST
	}

	private WhenBlockedPolicy whenBlockedPolicy = WhenBlockedPolicy.RUN;

	private int startUpSize = 4;

	public void setUseBoundary(boolean useBoundary)
	{
		this.useBoundary = useBoundary;
	}

	public boolean isUseBoundary()
	{
		return useBoundary;
	}

	public PoolConfiguration()
	{

	}

	public PoolConfiguration(boolean useBoundary, int boundarySize, int maximumPoolSize, int minimumPoolSize,
			int keepAliveTime, WhenBlockedPolicy whenBlockedPolicy, int startUpSize)
	{
		setUseBoundary(useBoundary);
		setBoundarySize(boundarySize);
		setMaximumPoolSize(maximumPoolSize);
		setMinimumPoolSize(minimumPoolSize);
		setKeepAliveTime(keepAliveTime);
		setWhenBlockedPolicy(whenBlockedPolicy);
		setStartUpSize(startUpSize);
	}

	public void setBoundarySize(int boundarySize)
	{
		this.boundarySize = boundarySize;
	}

	public int getBoundarySize()
	{
		return boundarySize;
	}

	public void setMaximumPoolSize(int maximumPoolSize)
	{
		this.maximumPoolSize = maximumPoolSize;
	}

	public int getMaximumPoolSize()
	{
		return maximumPoolSize;
	}

	public void setMinimumPoolSize(int minimumPoolSize)
	{
		this.minimumPoolSize = minimumPoolSize;
	}

	public int getMinimumPoolSize()
	{
		return minimumPoolSize;
	}

	public void setKeepAliveTime(int keepAliveTime)
	{
		this.keepAliveTime = keepAliveTime;
	}

	public int getKeepAliveTime()
	{
		return keepAliveTime;
	}

	public void setWhenBlockedPolicy(String whenBlockedPolicy)
	{
		if (whenBlockedPolicy != null)
		{
			WhenBlockedPolicy policy = WhenBlockedPolicy.valueOf(whenBlockedPolicy.trim().toUpperCase());
			setWhenBlockedPolicy(policy);
		}
		else
		{
			this.whenBlockedPolicy = WhenBlockedPolicy.RUN;
		}
	}

	public void setWhenBlockedPolicy(WhenBlockedPolicy whenBlockedPolicy)
	{
		if (whenBlockedPolicy != null)
		{
			this.whenBlockedPolicy = whenBlockedPolicy;
		}
		else
		{
			this.whenBlockedPolicy = WhenBlockedPolicy.RUN;
		}
	}

	public WhenBlockedPolicy getWhenBlockedPolicy()
	{
		return whenBlockedPolicy;
	}

	public void setStartUpSize(int startUpSize)
	{
		this.startUpSize = startUpSize;
	}

	public int getStartUpSize()
	{
		return startUpSize;
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		buf.append("useBoundary = [" + isUseBoundary() + "] ");
		buf.append("boundarySize = [" + boundarySize + "] ");
		buf.append("maximumPoolSize = [" + maximumPoolSize + "] ");
		buf.append("minimumPoolSize = [" + minimumPoolSize + "] ");
		buf.append("keepAliveTime = [" + keepAliveTime + "] ");
		buf.append("whenBlockedPolicy = [" + getWhenBlockedPolicy() + "] ");
		buf.append("startUpSize = [" + startUpSize + "]");
		return buf.toString();
	}

	@Override
	public PoolConfiguration clone()
	{
		return new PoolConfiguration(isUseBoundary(), boundarySize, maximumPoolSize, minimumPoolSize, keepAliveTime,
				getWhenBlockedPolicy(), startUpSize);
	}
}
