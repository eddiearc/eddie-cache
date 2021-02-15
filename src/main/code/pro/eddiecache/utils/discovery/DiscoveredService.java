package pro.eddiecache.utils.discovery;

import java.io.Serializable;
import java.util.ArrayList;

public class DiscoveredService implements Serializable
{

	private static final long serialVersionUID = 1L;

	private ArrayList<String> cacheNames;

	private String serviceAddress;

	private int servicePort;

	private long lastHearTime = 0;

	public void setCacheNames(ArrayList<String> cacheNames)
	{
		this.cacheNames = cacheNames;
	}

	public ArrayList<String> getCacheNames()
	{
		return cacheNames;
	}

	public void setServiceAddress(String serviceAddress)
	{
		this.serviceAddress = serviceAddress;
	}

	public String getServiceAddress()
	{
		return serviceAddress;
	}

	public void setServicePort(int servicePort)
	{
		this.servicePort = servicePort;
	}

	public int getServicePort()
	{
		return servicePort;
	}

	public void setLastHearTime(long lastHearTime)
	{
		this.lastHearTime = lastHearTime;
	}

	public long getLastHearTime()
	{
		return lastHearTime;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serviceAddress == null) ? 0 : serviceAddress.hashCode());
		result = prime * result + servicePort;
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null)
		{
			return false;
		}
		if (!(o instanceof DiscoveredService))
		{
			return false;
		}
		DiscoveredService other = (DiscoveredService) o;
		if (serviceAddress == null)
		{
			if (other.serviceAddress != null)
			{
				return false;
			}
		}
		else if (!serviceAddress.equals(other.serviceAddress))
		{
			return false;
		}
		if (servicePort != other.servicePort)
		{
			return false;
		}

		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n DiscoveredService");
		sb.append("\n CacheNames = [" + getCacheNames() + "]");
		sb.append("\n ServiceAddress = [" + getServiceAddress() + "]");
		sb.append("\n ServicePort = [" + getServicePort() + "]");
		sb.append("\n LastHearTime = [" + getLastHearTime() + "]");
		return sb.toString();
	}
}
