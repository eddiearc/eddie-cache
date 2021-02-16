package pro.eddiecache.kits.paxos.messages;

import pro.eddiecache.kits.paxos.comm.Member;

/**
 * 
 * 成员给leader回复确认
 */
public class SuccessAck implements MessageWithSender, SpecialMessage
{
	private static final long serialVersionUID = 1L;
	private final long msgId;
	private Member sender;

	public SuccessAck(long msgId, Member sender)
	{
		this.msgId = msgId;
		this.sender = sender;
	}

	public Member getSender()
	{
		return sender;
	}

	public long getMsgId()
	{
		return msgId;
	}

	public MessageType getMessageType()
	{
		return MessageType.SUCCESS_ACK;
	}

	@Override
	public String toString()
	{
		return "SUCCESS_ACK " + msgId + " " + sender.toString();
	}
}
