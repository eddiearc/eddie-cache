package pro.eddiecache.kits.paxos.messages;

public class Abort implements SpecialMessage
{
	private static final long serialVersionUID = 1L;
	public final long viewNo;
	public final long seqNo;

	public Abort(long viewNo, long seqNo)
	{
		this.viewNo = viewNo;
		this.seqNo = seqNo;
	}

	@Override
	public MessageType getMessageType()
	{
		return MessageType.ABORT;
	}
}
