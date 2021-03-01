package pro.eddiecache.kits.paxos.comm;

import java.io.Serializable;

/**
 * @author eddie
 * 心跳信息
 */
public class Tick implements Serializable
{

	private static final long serialVersionUID = 1L;

	/**
	 * 创建心跳的时间
	 */
	public final long time;

	public Tick(long time)
	{
		this.time = time;
	}
}
