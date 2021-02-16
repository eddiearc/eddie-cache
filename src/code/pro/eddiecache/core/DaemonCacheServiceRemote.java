package pro.eddiecache.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheServiceRemote;
import pro.eddiecache.core.model.IDaemon;
import pro.eddiecache.utils.timing.ElapsedTimer;

public class DaemonCacheServiceRemote<K, V> extends DaemonCacheService<K, V>
		implements ICacheServiceRemote<K, V>, IDaemon
{
	private static final Log log = LogFactory.getLog(DaemonCacheServiceRemote.class);

	private int maxQueueSize = 0;

	private final ConcurrentLinkedQueue<DaemonEvent> queue;

	public DaemonCacheServiceRemote()
	{
		queue = new ConcurrentLinkedQueue<DaemonEvent>();
	}

	public DaemonCacheServiceRemote(int maxQueueSize)
	{
		this.maxQueueSize = maxQueueSize;
		queue = new ConcurrentLinkedQueue<DaemonEvent>();
	}

	public int getQueueSize()
	{
		return queue.size();
	}

	private void addQueue(DaemonEvent event)
	{
		queue.add(event);
		if (queue.size() > maxQueueSize)
		{
			queue.poll();
		}
	}

	@Override
	public void update(ICacheElement<K, V> item, long listenerId)
	{
		if (maxQueueSize > 0)
		{
			PutEvent<K, V> event = new PutEvent<K, V>(item, listenerId);
			addQueue(event);
		}
	}

	@Override
	public void remove(String cacheName, K key, long listenerId)
	{
		if (maxQueueSize > 0)
		{
			RemoveEvent<K> event = new RemoveEvent<K>(cacheName, key, listenerId);
			addQueue(event);
		}
	}

	@Override
	public void removeAll(String cacheName, long listenerId)
	{
		if (maxQueueSize > 0)
		{
			RemoveAllEvent event = new RemoveAllEvent(cacheName, listenerId);
			addQueue(event);
		}
	}

	@Override
	public ICacheElement<K, V> get(String cacheName, K key, long requesterId) throws IOException
	{
		return null;
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMatching(String cacheName, String pattern, long requesterId)
			throws IOException
	{
		return Collections.emptyMap();
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(String cacheName, Set<K> keys, long requesterId)
	{
		return new HashMap<K, ICacheElement<K, V>>();
	}

	@Override
	public Set<K> getKeySet(String cacheName)
	{
		return Collections.emptySet();
	}

	public synchronized void spreadEvents(ICacheServiceRemote<K, V> service) throws Exception
	{
		int cnt = 0;

		ElapsedTimer timer = new ElapsedTimer();
		while (!queue.isEmpty())
		{
			cnt++;

			DaemonEvent event = queue.poll();

			if (event instanceof PutEvent)
			{
				@SuppressWarnings("unchecked")
				PutEvent<K, V> putEvent = (PutEvent<K, V>) event;
				service.update(putEvent.element, event.requesterId);
			}
			else if (event instanceof RemoveEvent)
			{
				@SuppressWarnings("unchecked")
				RemoveEvent<K> removeEvent = (RemoveEvent<K>) event;
				service.remove(event.cacheName, removeEvent.key, event.requesterId);
			}
			else if (event instanceof RemoveAllEvent)
			{
				service.removeAll(event.cacheName, event.requesterId);
			}
		}
		if (log.isInfoEnabled())
		{
			log.info("Spread  " + cnt + " events to the new ICacheServiceRemote in " + timer.getElapsedTimeString());
		}
	}

	protected static abstract class DaemonEvent
	{
		String cacheName;

		long requesterId;
	}

	private static class PutEvent<K, V> extends DaemonEvent
	{
		ICacheElement<K, V> element;

		public PutEvent(ICacheElement<K, V> element, long requesterId)
		{
			this.requesterId = requesterId;
			this.element = element;
		}
	}

	private static class RemoveEvent<K> extends DaemonEvent
	{
		K key;

		public RemoveEvent(String cacheName, K key, long requesterId)
		{
			this.cacheName = cacheName;
			this.requesterId = requesterId;
			this.key = key;
		}
	}

	private static class RemoveAllEvent extends DaemonEvent
	{
		public RemoveAllEvent(String cacheName, long requesterId)
		{
			this.cacheName = cacheName;
			this.requesterId = requesterId;
		}
	}
}
