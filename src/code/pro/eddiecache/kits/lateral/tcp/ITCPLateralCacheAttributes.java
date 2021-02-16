package pro.eddiecache.kits.lateral.tcp;

import pro.eddiecache.kits.lateral.ILateralCacheAttributes;

public interface ITCPLateralCacheAttributes extends ILateralCacheAttributes
{
	void setTcpServer(String server);

	String getTcpServer();

	void setTcpServers(String servers);

	String getTcpServers();

	void setTcpListenerPort(int port);

	int getTcpListenerPort();

	void setUdpDiscoveryEnabled(boolean udpDiscoveryEnabled);

	boolean isUdpDiscoveryEnabled();

	int getUdpDiscoveryPort();

	void setUdpDiscoveryPort(int udpDiscoveryPort);

	String getUdpDiscoveryAddr();

	void setUdpDiscoveryAddr(String udpDiscoveryAddr);

	void setAllowGet(boolean allowGet);

	boolean isAllowGet();

	void setAllowPut(boolean allowPut);

	boolean isAllowPut();

	void setAllowRemoveOnPut(boolean allowRemoveOnPut);

	boolean isAllowRemoveOnPut();

	boolean isFilterRemoveByHashCode();

	void setFilterRemoveByHashCode(boolean filter);

	void setSocketTimeOut(int socketTimeOut);

	int getSocketTimeOut();

	void setOpenTimeOut(int openTimeOut);

	int getOpenTimeOut();
}
