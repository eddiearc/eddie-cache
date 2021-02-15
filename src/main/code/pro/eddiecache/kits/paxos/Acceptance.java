package pro.eddiecache.kits.paxos;

import java.io.Serializable;

/**
 * 接受到的消息，编年体历史记录
 */
public class Acceptance implements Serializable
{
	private static final long serialVersionUID = 1L;
	public long viewNumber;
	public Serializable message;
	public long msgId;

	public Acceptance(long viewNumber, Serializable message, long msgId)
	{
		this.viewNumber = viewNumber;
		this.message = message;
		this.msgId = msgId;
	}
}
