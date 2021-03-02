package pro.eddiecache.kits.paxos;

import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author eddie
 */
public class WaitingTimer
{
	private final HashMap<Long, Semaphore> waitingForResponse = new HashMap<Long, Semaphore>();

	public boolean waitALittle(long msgId) throws InterruptedException
	{
		Semaphore semaphore;
		synchronized (waitingForResponse)
		{
			semaphore = getOrCreateSemaphore(msgId);
		}
		return semaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);
	}

	/**
	 * 解除阻塞
	 *
	 * @param msgId 信息ID
	 */
	public void unblock(long msgId)
	{
		synchronized (waitingForResponse)
		{
			Semaphore semaphore = getOrCreateSemaphore(msgId);
			if (semaphore != null)
			{
				semaphore.release();
			}
		}
	}

	/**
	 * get Or Create 信号灯
	 *
	 * @param msgId 信息ID
	 * @return 相关类
	 */
	private Semaphore getOrCreateSemaphore(long msgId)
	{
		if (!waitingForResponse.containsKey(msgId))
		{
			waitingForResponse.put(msgId, new Semaphore(0));
		}
		return waitingForResponse.get(msgId);
	}
}
