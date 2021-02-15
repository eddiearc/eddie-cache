package pro.eddiecache.utils.discovery;

public final class UDPDiscoveryAttributes implements Cloneable
{
	private String serviceName;

	private String serviceAddress;

	private int servicePort;

	private boolean isDark;

	private static final String DEFAULT_UDP_DISCOVERY_ADDRESS = "228.4.5.6";

	private static final int DEFAULT_UDP_DISCOVERY_PORT = 5678;

	private String udpDiscoveryAddr = DEFAULT_UDP_DISCOVERY_ADDRESS;

	private int udpDiscoveryPort = DEFAULT_UDP_DISCOVERY_PORT;

	private static final int DEFAULT_SEND_DELAY_SECOND = 60;

	private int sendDelaySecond = DEFAULT_SEND_DELAY_SECOND;

	private static final int DEFAULT_MAX_IDLE_TIME_SECOND = 180;

	private int maxIdleTimeSecond = DEFAULT_MAX_IDLE_TIME_SECOND;

	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	public String getServiceName()
	{
		return serviceName;
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

	public void setUdpDiscoveryAddr(String udpDiscoveryAddr)
	{
		this.udpDiscoveryAddr = udpDiscoveryAddr;
	}

	public String getUdpDiscoveryAddr()
	{
		return udpDiscoveryAddr;
	}

	public void setUdpDiscoveryPort(int udpDiscoveryPort)
	{
		this.udpDiscoveryPort = udpDiscoveryPort;
	}

	public int getUdpDiscoveryPort()
	{
		return udpDiscoveryPort;
	}

	public void setSendDelaySecond(int sendDelaySecond)
	{
		this.sendDelaySecond = sendDelaySecond;
	}

	public int getSendDelaySecond()
	{
		return sendDelaySecond;
	}

	public void setMaxIdleTimeSecond(int maxIdleTimeSecond)
	{
		this.maxIdleTimeSecond = maxIdleTimeSecond;
	}

	public int getMaxIdleTimeSecond()
	{
		return maxIdleTimeSecond;
	}

	public boolean isDark()
	{
		return isDark;
	}

	public void setDark(boolean isDark)
	{
		this.isDark = isDark;
	}

	@Override
	public UDPDiscoveryAttributes clone()
	{
		UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
		attributes.setSendDelaySecond(this.getSendDelaySecond());
		attributes.setMaxIdleTimeSecond(this.getMaxIdleTimeSecond());
		attributes.setServiceName(this.getServiceName());
		attributes.setServicePort(this.getServicePort());
		attributes.setUdpDiscoveryAddr(this.getUdpDiscoveryAddr());
		attributes.setUdpDiscoveryPort(this.getUdpDiscoveryPort());
		attributes.setDark(this.isDark());
		return attributes;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n UDPDiscoveryAttributes");
		sb.append("\n ServiceName = [" + getServiceName() + "]");
		sb.append("\n ServiceAddress = [" + getServiceAddress() + "]");
		sb.append("\n ServicePort = [" + getServicePort() + "]");
		sb.append("\n UdpDiscoveryAddr = [" + getUdpDiscoveryAddr() + "]");
		sb.append("\n UdpDiscoveryPort = [" + getUdpDiscoveryPort() + "]");
		sb.append("\n SendDelaySecond = [" + getSendDelaySecond() + "]");
		sb.append("\n MaxIdleTimeSecond = [" + getMaxIdleTimeSecond() + "]");
		sb.append("\n IsDark = [" + isDark() + "]");
		return sb.toString();
	}
}
