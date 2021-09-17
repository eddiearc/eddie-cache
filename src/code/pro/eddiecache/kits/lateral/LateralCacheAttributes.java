package pro.eddiecache.kits.lateral;

import pro.eddiecache.kits.AbstractKitCacheAttributes;

public class LateralCacheAttributes extends AbstractKitCacheAttributes implements ILateralCacheAttributes
{
	private static final long serialVersionUID = 1L;

	private static final boolean DEFAULT_RECEIVE = true;

	private String transmissionTypeName = "TCP";

	private Type transmissionType = Type.TCP;

	private String httpServer = "";

	private String udpMulticastAddr = "228.5.6.7";

	private int udpMulticastPort = 6789;

	private boolean putOnlyMode = true;

	private boolean receive = DEFAULT_RECEIVE;

	private int deamonQueueMaxSize = DEFAULT_DAEMON_QUEUE_MAX_SIZE;

	public LateralCacheAttributes()
	{
		super();
	}

	@Override
	public void setTransmissionType(Type val)
	{
		this.transmissionType = val;
		this.transmissionTypeName = val.toString();
	}

	@Override
	public Type getTransmissionType()
	{
		return this.transmissionType;
	}

	@Override
	public void setPutOnlyMode(boolean val)
	{
		this.putOnlyMode = val;
	}

	@Override
	public boolean getPutOnlyMode()
	{
		return putOnlyMode;
	}

	@Override
	public void setReceive(boolean receive)
	{
		this.receive = receive;
	}

	@Override
	public boolean isReceive()
	{
		return receive;
	}

	@Override
	public void setDaemonQueueMaxSize(int daemonQueueMaxSize)
	{
		this.deamonQueueMaxSize = daemonQueueMaxSize;
	}

	@Override
	public int getDaemonQueueMaxSize()
	{
		return deamonQueueMaxSize;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(transmissionTypeName + httpServer + udpMulticastAddr + udpMulticastPort);
		return sb.toString();
	}
}
