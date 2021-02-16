package pro.eddiecache.kits;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractKitCacheMonitor extends Thread
{
	protected final Log log = LogFactory.getLog(this.getClass());

	protected static long idlePeriod = 20 * 1000;

	protected AtomicBoolean allright = new AtomicBoolean(true);

	private AtomicBoolean shutdown = new AtomicBoolean(false);

	private Lock lock = new ReentrantLock();

	private Condition trigger = lock.newCondition();

	public AbstractKitCacheMonitor(String name)
	{
		super(name);
	}

	public static void setIdlePeriod(long idlePeriod)
	{
		if (idlePeriod > AbstractKitCacheMonitor.idlePeriod)
		{
			AbstractKitCacheMonitor.idlePeriod = idlePeriod;
		}
	}

	public void notifyError()
	{
		if (allright.compareAndSet(true, false))
		{
			signalTrigger();
		}
	}

	public void notifyShutdown()
	{
		if (shutdown.compareAndSet(false, true))
		{
			signalTrigger();
		}
	}

	private void signalTrigger()
	{
		try
		{
			lock.lock();
			trigger.signal();
		}
		finally
		{
			lock.unlock();
		}
	}

	protected abstract void dispose();

	protected abstract void doWork();

	@Override
	public void run()
	{
		do
		{
			if (log.isDebugEnabled())
			{
				if (allright.get())
				{
					log.debug("allright = true, cache monitor will wait for an error.");
				}
				else
				{
					log.debug("allright = false cache monitor running.");
				}
			}

			if (allright.get())
			{
				try
				{
					lock.lock();
					trigger.await();
				}
				catch (InterruptedException ignore)
				{
				}
				finally
				{
					lock.unlock();
				}
			}

			if (shutdown.get())
			{
				log.info("Shutting down cache monitor");
				dispose();
				return;
			}

			allright.set(true);

			if (log.isDebugEnabled())
			{
				log.debug("Cache monitor running.");
			}

			doWork();

			try
			{
				if (log.isDebugEnabled())
				{
					log.debug("Cache monitor sleeping for " + idlePeriod + " between runs.");
				}

				Thread.sleep(idlePeriod);
			}
			catch (InterruptedException ex)
			{
			}
		}
		while (true);
	}
}
