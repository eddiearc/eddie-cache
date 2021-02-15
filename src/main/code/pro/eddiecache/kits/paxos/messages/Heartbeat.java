package pro.eddiecache.kits.paxos.messages;

import java.io.Serializable;

import pro.eddiecache.kits.paxos.comm.Member;

/**
 * 心跳
 */
public class Heartbeat implements Serializable
{
	private static final long serialVersionUID = 1L;

	public Member sender;

	public Heartbeat(Member sender)
	{
		this.sender = sender;
	}

	@Override
	public String toString()
	{
		return "heartbeat";
	}
}
