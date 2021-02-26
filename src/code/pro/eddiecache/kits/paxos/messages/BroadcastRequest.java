package pro.eddiecache.kits.paxos.messages;

import java.io.Serializable;

/**
 * @author eddie
 *
 * 成员接受消息之后，转发给leader，由leader进行广播
 */
public class BroadcastRequest implements SpecialMessage
{
	private static final long serialVersionUID = 1L;
	public Serializable message;
	public long msgId;

	public BroadcastRequest(Serializable message, long msgId)
	{
		this.message = message;
		this.msgId = msgId;
	}

	@Override
	public MessageType getMessageType()
	{
		return MessageType.BROADCAST_REQ;
	}
}
