package pro.eddiecache.kits.paxos.messages;

import java.io.Serializable;

import pro.eddiecache.kits.paxos.comm.Member;

/**
 * @author eddie
 * Accept由当选的leader发出，要求成员接受消息
 */
public class Accept implements SpecialMessage
{
	private static final long serialVersionUID = 1L;
	public long viewNo;
	public long seqNo;
	public Serializable message;
	public long msgId;
	public final Member sender;

	public Accept(long viewNo, long seqNo, Serializable message, long msgId, Member sender)
	{
		this.viewNo = viewNo;
		this.seqNo = seqNo;
		this.message = message;
		this.msgId = msgId;
		this.sender = sender;
	}

	@Override
	public MessageType getMessageType()
	{
		return MessageType.ACCEPT;
	}

	@Override
	public String toString()
	{
		return "ACCEPT " + seqNo + " " + msgId;
	}
}
