package pro.eddiecache.utils.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HostNameUtil
{
	private static final Log log = LogFactory.getLog(HostNameUtil.class);

	public static String getLocalHostAddress() throws UnknownHostException
	{
		try
		{
			String hostAddress = getLocalHostLANAddress().getHostAddress();
			if (log.isDebugEnabled())
			{
				log.debug("hostAddress = [" + hostAddress + "]");
			}
			return hostAddress;
		}
		catch (UnknownHostException e)
		{
			log.error("Couldn't get localhost address", e);
			throw e;
		}
	}

	public static InetAddress getLocalHostLANAddress() throws UnknownHostException
	{
		try
		{
			InetAddress candidateAddress = null;
			for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
					.hasMoreElements();)
			{
				NetworkInterface iface = ifaces.nextElement();
				for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();)
				{
					InetAddress inetAddr = inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress())
					{
						if (inetAddr.isSiteLocalAddress())
						{
							return inetAddr;
						}
						else if (candidateAddress == null)
						{
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (candidateAddress != null)
			{

				return candidateAddress;
			}
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null)
			{
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress;
		}
		catch (Exception e)
		{
			UnknownHostException unknownHostException = new UnknownHostException("Fail to determine LAN address: " + e);
			unknownHostException.initCause(e);
			throw unknownHostException;
		}
	}
}
