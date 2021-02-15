package pro.eddiecache.utils.threadpool;

import java.util.concurrent.ThreadFactory;

public class CacheKitThreadFactory implements ThreadFactory
{
	private String prefix;
	private boolean threadIsDaemon = true;
	private int threadPriority = Thread.NORM_PRIORITY;

	public CacheKitThreadFactory(String prefix)
	{
		this(prefix, Thread.NORM_PRIORITY);
	}

	public CacheKitThreadFactory(String prefix, int threadPriority)
	{
		this.prefix = prefix;
		this.threadPriority = threadPriority;
	}

	@Override
	public Thread newThread(Runnable runner)
	{
		Thread thread = new Thread(runner);
		String threadName = thread.getName();
		thread.setName(prefix + threadName);
		thread.setDaemon(threadIsDaemon);
		thread.setPriority(threadPriority);
		return thread;
	}
}
