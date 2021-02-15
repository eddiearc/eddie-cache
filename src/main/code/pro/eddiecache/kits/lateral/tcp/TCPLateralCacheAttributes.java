package pro.eddiecache.kits.lateral.tcp;

import pro.eddiecache.kits.lateral.LateralCacheAttributes;

public class TCPLateralCacheAttributes extends LateralCacheAttributes implements ITCPLateralCacheAttributes
{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_UDP_DISCOVERY_ADDRESS = "228.5.6.7";

	private static final int DEFAULT_UDP_DISCOVERY_PORT = 6789;

	private static final boolean DEFAULT_UDP_DISCOVERY_ENABLED = true;

	private static final boolean DEFAULT_ALLOW_GET = true;

	private static final boolean DEFAULT_ALLOW_PUT = true;

	private static final boolean DEFAULT_ALLOW_REMOVE_FOR_PUT = false;

	private static final boolean DEFAULT_FILTER_REMOVE_BY_HASH_CODE = true;

	private static final int DEFAULT_SOCKET_TIME_OUT = 1000;

	private static final int DEFAULT_OPEN_TIMEOUT = 2000;

	private String tcpServers = null;

	private String tcpServer = "";

	private int tcpListenerPort = 8888;

	private String udpDiscoveryAddr = DEFAULT_UDP_DISCOVERY_ADDRESS;

	private int udpDiscoveryPort = DEFAULT_UDP_DISCOVERY_PORT;

	private boolean udpDiscoveryEnabled = DEFAULT_UDP_DISCOVERY_ENABLED;

	private boolean allowPut = DEFAULT_ALLOW_GET;

	private boolean allowGet = DEFAULT_ALLOW_PUT;

	private boolean allowRemoveOnPut = DEFAULT_ALLOW_REMOVE_FOR_PUT;

	private boolean filterRemoveByHashCode = DEFAULT_FILTER_REMOVE_BY_HASH_CODE;

	private int socketTimeOut = DEFAULT_SOCKET_TIME_OUT;

	private int openTimeOut = DEFAULT_OPEN_TIMEOUT;

	public TCPLateralCacheAttributes()
	{
		super();
	}

	@Override
	public void setTcpServer(String server)
	{
		this.tcpServer = server;
	}

	@Override
	public String getTcpServer()
	{
		return this.tcpServer;
	}

	@Override
	public void setTcpServers(String servers)
	{
		this.tcpServers = servers;
	}

	@Override
	public String getTcpServers()
	{
		return this.tcpServers;
	}

	@Override
	public void setTcpListenerPort(int port)
	{
		this.tcpListenerPort = port;
	}

	@Override
	public int getTcpListenerPort()
	{
		return this.tcpListenerPort;
	}

	@Override
	public void setUdpDiscoveryEnabled(boolean udpDiscoveryEnabled)
	{
		this.udpDiscoveryEnabled = udpDiscoveryEnabled;
	}

	@Override
	public boolean isUdpDiscoveryEnabled()
	{
		return this.udpDiscoveryEnabled;
	}

	@Override
	public int getUdpDiscoveryPort()
	{
		return this.udpDiscoveryPort;
	}

	@Override
	public void setUdpDiscoveryPort(int udpDiscoveryPort)
	{
		this.udpDiscoveryPort = udpDiscoveryPort;
	}

	@Override
	public String getUdpDiscoveryAddr()
	{
		return this.udpDiscoveryAddr;
	}

	@Override
	public void setUdpDiscoveryAddr(String udpDiscoveryAddr)
	{
		this.udpDiscoveryAddr = udpDiscoveryAddr;
	}

	@Override
	public void setAllowGet(boolean allowGet)
	{
		this.allowGet = allowGet;
	}

	@Override
	public boolean isAllowGet()
	{
		return this.allowGet;
	}

	@Override
	public void setAllowPut(boolean allowPut)
	{
		this.allowPut = allowPut;
	}

	@Override
	public boolean isAllowPut()
	{
		return this.allowPut;
	}

	@Override
	public void setAllowRemoveOnPut(boolean allowRemoveOnPut)
	{
		this.allowRemoveOnPut = allowRemoveOnPut;
	}

	@Override
	public boolean isAllowRemoveOnPut()
	{
		return this.allowRemoveOnPut;
	}

	@Override
	public boolean isFilterRemoveByHashCode()
	{
		return this.filterRemoveByHashCode;
	}

	@Override
	public void setFilterRemoveByHashCode(boolean filter)
	{
		this.filterRemoveByHashCode = filter;
	}

	@Override
	public void setSocketTimeOut(int socketTimeOut)
	{
		this.socketTimeOut = socketTimeOut;
	}

	@Override
	public int getSocketTimeOut()
	{
		return socketTimeOut;
	}

	@Override
	public void setOpenTimeOut(int openTimeOut)
	{
		this.openTimeOut = openTimeOut;
	}

	@Override
	public int getOpenTimeOut()
	{
		return openTimeOut;
	}

	@Override
	public String toString()
	{
		return this.getTcpServer() + ":" + this.getTcpListenerPort();
	}
}
