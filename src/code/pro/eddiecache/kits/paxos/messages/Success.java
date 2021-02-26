package pro.eddiecache.kits.paxos.messages;

import java.io.Serializable;

/**
 * 
 * leader收到半数以上的确认消息，则发送Success消息给成员
 */
public class Success implements SpecialMessage
{
	private static final long serialVersionUID = 1L;
	public long seqNo;
	public Serializable message;
	public long msgId;

	public Success(long seqNo, Serializable message, long msgId)
	{
		this.seqNo = seqNo;
		this.message = message;
		this.msgId = msgId;
	}

	@Override
	public String toString()
	{
		return "SUCCESS " + seqNo + " " + msgId + "(" + message + ")";
	}

	@Override
	public MessageType getMessageType()
	{
		return MessageType.SUCCESS;
	}
}
