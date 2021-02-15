package pro.eddiecache.utils.discovery;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.IContextCacheManager;
import pro.eddiecache.core.model.IProvideScheduler;

public class UDPDiscoveryManager
{
	private static final Log log = LogFactory.getLog(UDPDiscoveryManager.class);

	private static UDPDiscoveryManager INSTANCE = new UDPDiscoveryManager();

	private final Map<String, UDPDiscoveryService> services = new HashMap<String, UDPDiscoveryService>();

	private UDPDiscoveryManager()
	{

	}

	public static UDPDiscoveryManager getInstance()
	{
		return INSTANCE;
	}

	public synchronized UDPDiscoveryService getService(String discoveryAddress, int discoveryPort, int servicePort,
			IContextCacheManager cacheMgr)
	{
		String key = discoveryAddress + ":" + discoveryPort + ":" + servicePort;

		UDPDiscoveryService service = services.get(key);
		if (service == null)
		{
			if (log.isInfoEnabled())
			{
				log.info("Create service for address:port:servicePort [" + key + "]");
			}

			UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
			attributes.setUdpDiscoveryAddr(discoveryAddress);
			attributes.setUdpDiscoveryPort(discoveryPort);
			attributes.setServicePort(servicePort);

			service = new UDPDiscoveryService(attributes);

			cacheMgr.registerShutdownObserver(service);

			if (cacheMgr instanceof IProvideScheduler)
			{
				service.setScheduledExecutorService(((IProvideScheduler) cacheMgr).getScheduledExecutorService());
			}

			service.startup();
			services.put(key, service);
		}

		if (log.isDebugEnabled())
		{
			log.debug("Return service [" + service + "] for key [" + key + "]");
		}

		return service;
	}
}
