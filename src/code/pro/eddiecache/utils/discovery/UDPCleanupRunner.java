package pro.eddiecache.utils.discovery;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UDPCleanupRunner implements Runnable
{
	private static final Log log = LogFactory.getLog(UDPCleanupRunner.class);

	private final UDPDiscoveryService discoveryService;

	private static final long DEFAULT_MAX_IDLE_TIME_SECONDS = 180;

	private final long maxIdleTimeSeconds = DEFAULT_MAX_IDLE_TIME_SECONDS;

	public UDPCleanupRunner(UDPDiscoveryService service)
	{
		this.discoveryService = service;
	}

	@Override
	public void run()
	{
		long now = System.currentTimeMillis();

		Set<DiscoveredService> toRemove = new HashSet<DiscoveredService>();
		for (DiscoveredService service : discoveryService.getDiscoveredServices())
		{
			if ((now - service.getLastHearTime()) > (maxIdleTimeSeconds * 1000))
			{
				if (log.isInfoEnabled())
				{
					log.info("Remove service, since haven't heard from it in " + maxIdleTimeSeconds
							+ " seconds.  service = " + service);
				}
				toRemove.add(service);
			}
		}

		for (DiscoveredService service : toRemove)
		{
			discoveryService.removeDiscoveredService(service);
		}
	}
}
