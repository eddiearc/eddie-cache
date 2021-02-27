package pro.eddiecache.kits.paxos.comm;

import java.io.Serializable;

/**
 * @author eddie
 */
public class Tick implements Serializable
{

	private static final long serialVersionUID = 1L;

	public final long time;

	public Tick(long time)
	{
		this.time = time;
	}
}
