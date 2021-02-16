package pro.eddiecache.core.control.event;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.utils.threadpool.CacheKitThreadFactory;

/**
 * 事件队列
 * @author eddie
 */
public class ElementEventQueue implements IElementEventQueue
{

	private static final Log log = LogFactory.getLog(ElementEventQueue.class);

	private static final String THREAD_PREFIX = "CacheKit-EventQueue-";

	private boolean destroyed = false;

	private LinkedBlockingQueue<Runnable> queue;

	private ThreadPoolExecutor queueProcessor;

	public ElementEventQueue()
	{
		queue = new LinkedBlockingQueue<Runnable>();

		queueProcessor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queue,
				new CacheKitThreadFactory(THREAD_PREFIX));

		if (log.isDebugEnabled())
		{
			log.debug("Element Event Queue construct: " + this);
		}
	}

	/**
	 * 销毁该事件处理队列
	 */
	@Override
	public void dispose()
	{
		if (!destroyed)
		{
			destroyed = true;

			queueProcessor.shutdownNow();
			queueProcessor = null;

			if (log.isInfoEnabled())
			{
				log.info("Element Event Queue destroy: " + this);
			}
		}
	}

	/**
	 * 将事件处理器和需要被处理的事件传入队列中并执行
	 *
	 * @param handler 事件处理器
	 * @param event 处理的事件
	 */
	@Override
	public <T> void addElementEvent(IElementEventHandler handler, IElementEvent<T> event) throws IOException
	{

		if (log.isDebugEnabled())
		{
			log.debug("Add event handler to queue.");
		}

		if (destroyed)
		{
			log.warn("Event submitted to disposed element event queue " + event);
		}
		else
		{
			ElementEventRunner runner = new ElementEventRunner(handler, event);

			if (log.isDebugEnabled())
			{
				log.debug("Element event runner = " + runner);
			}

			queueProcessor.execute(runner);
		}
	}

	///////////////////////////// 内部类 /////////////////////////////

	protected abstract class AbstractElementEventRunner implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				doRun();
			}
			catch (IOException e)
			{
				log.warn("Element event runner failure" + ElementEventQueue.this, e);
			}
		}

		protected abstract void doRun() throws IOException;
	}

	private class ElementEventRunner extends AbstractElementEventRunner
	{
		private final IElementEventHandler handler;

		private final IElementEvent<?> event;

		ElementEventRunner(IElementEventHandler hand, IElementEvent<?> event) throws IOException
		{
			if (log.isDebugEnabled())
			{
				log.debug("Element event runner constructed: " + this);
			}
			this.handler = hand;
			this.event = event;
		}

		@Override
		protected void doRun() throws IOException
		{
			handler.handleElementEvent(event);
		}
	}
}
