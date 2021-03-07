package pro.eddiecache.kits.paxos;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.core.CacheInfo;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.control.ContextCacheManager;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IContextCacheManager;

public class PaxosCacheReceiver<K, V> implements Receiver
{
	private static final Log log = LogFactory.getLog(PaxosCacheReceiver.class);

	private transient IContextCacheManager cacheManager;

	@Override
	public void receive(Serializable element)
	{
		System.out.println("received " + element.toString());

		CacheElementPaxos cep = (CacheElementPaxos) element;

		if (cep.getRequesterId() == CacheInfo.listenerId)
		{
			if (log.isDebugEnabled())
			{
				log.debug("paxos from self ");
			}
			return;
		}
		ICacheElement<K, V> ce = cep.getCacheElement();

		try
		{
			getCache(ce.getCacheName()).localUpdate(ce);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void setCacheManager(IContextCacheManager cacheMgr)
	{
		this.cacheManager = cacheMgr;
	}

	public IContextCacheManager getCacheManager()
	{
		return cacheManager;
	}

	protected ContextCache<K, V> getCache(String name)
	{
		if (getCacheManager() == null)
		{
			try
			{
				setCacheManager(ContextCacheManager.getInstance());
			}
			catch (CacheException e)
			{
				throw new RuntimeException("Could not retrieve cache manager instance", e);
			}

			if (log.isDebugEnabled())
			{
				log.debug("cacheMgr = " + getCacheManager());
			}
		}

		return getCacheManager().getCache(name);
	}
}
