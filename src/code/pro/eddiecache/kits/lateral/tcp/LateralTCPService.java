package pro.eddiecache.kits.lateral.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheElement;
import pro.eddiecache.core.CacheInfo;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheServiceRemote;
import pro.eddiecache.kits.lateral.LateralCommand;
import pro.eddiecache.kits.lateral.LateralElementDescriptor;

/**
 * @author eddie
 */
public class LateralTCPService<K, V> implements ICacheServiceRemote<K, V>
{
	private static final Log log = LogFactory.getLog(LateralTCPService.class);

	private boolean allowPut;
	private boolean allowGet;
	private boolean allowRemoveOnPut;

	private LateralTCPSender sender;

	private long listenerId = CacheInfo.listenerId;

	public LateralTCPService(ITCPLateralCacheAttributes attr) throws IOException
	{
		this.allowGet = attr.isAllowGet();
		this.allowPut = attr.isAllowPut();
		this.allowRemoveOnPut = attr.isAllowRemoveOnPut();

		try
		{
			sender = new LateralTCPSender(attr);

			if (log.isInfoEnabled())
			{
				log.debug("Create sender to [" + attr.getTcpServer() + "]");
			}
		}
		catch (IOException e)
		{
			log.error("Could not create sender to [" + attr.getTcpServer() + "] -- " + e.getMessage());

			throw e;
		}
	}

	@Override
	public void update(ICacheElement<K, V> item) throws IOException
	{
		update(item, getListenerId());
	}

	@Override
	public void update(ICacheElement<K, V> item, long requesterId) throws IOException
	{
		if (!this.allowPut && !this.allowRemoveOnPut)
		{
			return;
		}

		if (!this.allowRemoveOnPut)
		{
			LateralElementDescriptor<K, V> led = new LateralElementDescriptor<K, V>(item);
			led.requesterId = requesterId;
			led.command = LateralCommand.UPDATE;
			sender.send(led);
		}
		else
		{
			CacheElement<K, V> ce = new CacheElement<K, V>(item.getCacheName(), item.getKey(), null);
			LateralElementDescriptor<K, V> led = new LateralElementDescriptor<K, V>(ce);
			led.requesterId = requesterId;
			led.command = LateralCommand.REMOVE;
			led.valHashCode = item.getVal().hashCode();
			sender.send(led);
		}
	}

	@Override
	public void remove(String cacheName, K key) throws IOException
	{
		remove(cacheName, key, getListenerId());
	}

	@Override
	public void remove(String cacheName, K key, long requesterId) throws IOException
	{
		CacheElement<K, V> ce = new CacheElement<K, V>(cacheName, key, null);
		LateralElementDescriptor<K, V> led = new LateralElementDescriptor<K, V>(ce);
		led.requesterId = requesterId;
		led.command = LateralCommand.REMOVE;
		sender.send(led);
	}

	@Override
	public void release() throws IOException
	{
	}

	@Override
	public void dispose(String cacheName) throws IOException
	{
		sender.dispose();
	}

	@Override
	public ICacheElement<K, V> get(String cacheName, K key) throws IOException
	{
		return get(cacheName, key, getListenerId());
	}

	@Override
	public ICacheElement<K, V> get(String cacheName, K key, long requesterId) throws IOException
	{
		if (this.allowGet)
		{
			CacheElement<K, V> ce = new CacheElement<K, V>(cacheName, key, null);
			LateralElementDescriptor<K, V> led = new LateralElementDescriptor<K, V>(ce);
			led.command = LateralCommand.GET;
			@SuppressWarnings("unchecked")
			ICacheElement<K, V> response = (ICacheElement<K, V>) sender.sendAndReceive(led);
			if (response != null)
			{
				return response;
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMatching(String cacheName, String pattern) throws IOException
	{
		return getMatching(cacheName, pattern, getListenerId());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<K, ICacheElement<K, V>> getMatching(String cacheName, String pattern, long requesterId)
			throws IOException
	{
		if (this.allowGet)
		{
			CacheElement<String, String> ce = new CacheElement<String, String>(cacheName, pattern, null);
			LateralElementDescriptor<String, String> led = new LateralElementDescriptor<String, String>(ce);
			led.requesterId = requesterId;
			led.command = LateralCommand.GET_MATCHING;

			Object response = sender.sendAndReceive(led);
			if (response != null)
			{
				return (Map<K, ICacheElement<K, V>>) response;
			}
			return Collections.emptyMap();
		}
		else
		{
			return null;
		}
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(String cacheName, Set<K> keys) throws IOException
	{
		return getMultiple(cacheName, keys, getListenerId());
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(String cacheName, Set<K> keys, long requesterId) throws IOException
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		if (keys != null && !keys.isEmpty())
		{
			for (K key : keys)
			{
				ICacheElement<K, V> element = get(cacheName, key);

				if (element != null)
				{
					elements.put(key, element);
				}
			}
		}
		return elements;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<K> getKeySet(String cacheName) throws IOException
	{
		CacheElement<String, String> ce = new CacheElement<String, String>(cacheName, null, null);
		LateralElementDescriptor<String, String> led = new LateralElementDescriptor<String, String>(ce);

		led.command = LateralCommand.GET_KEYSET;
		Object response = sender.sendAndReceive(led);
		if (response != null)
		{
			return (Set<K>) response;
		}

		return null;
	}

	@Override
	public void removeAll(String cacheName) throws IOException
	{
		removeAll(cacheName, getListenerId());
	}

	@Override
	public void removeAll(String cacheName, long requesterId) throws IOException
	{
		CacheElement<String, String> ce = new CacheElement<String, String>(cacheName, "ALL", null);
		LateralElementDescriptor<String, String> led = new LateralElementDescriptor<String, String>(ce);
		led.requesterId = requesterId;
		led.command = LateralCommand.REMOVEALL;
		sender.send(led);
	}

	protected void setListenerId(long listernId)
	{
		this.listenerId = listernId;
	}

	protected long getListenerId()
	{
		return listenerId;
	}
}
