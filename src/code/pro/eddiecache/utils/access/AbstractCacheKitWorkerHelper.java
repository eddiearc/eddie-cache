package pro.eddiecache.utils.access;

public abstract class AbstractCacheKitWorkerHelper<V> implements CacheKitWorkerHelper<V>
{
	private boolean finished = false;

	public AbstractCacheKitWorkerHelper()
	{
		super();
	}

	@Override
	public synchronized boolean isFinished()
	{
		return finished;
	}

	@Override
	public synchronized void setFinished(boolean isFinished)
	{
		finished = isFinished;
	}
}
