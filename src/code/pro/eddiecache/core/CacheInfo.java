package pro.eddiecache.core;

import java.rmi.dgc.VMID;

/*
 * Node是节点的意思，在分布式领域节点的含义包括很多：单个主机，单个进程等都可以视为节点
 */
public final class CacheInfo
{
	private CacheInfo()
	{
		super();
	}

	private static final VMID vmid = new VMID();

	public static final long listenerId = vmid.hashCode();
}
