package pro.eddiecache.kits.paxos.messages;

import pro.eddiecache.kits.paxos.comm.Member;

/**
 *  旧leader下野，系统指定新的leader当做提名人，新leader提名人开启选举
 */
public class NewView implements SpecialMessage
{
	private static final long serialVersionUID = 1L;
	public final Member leader;
	public final long viewNumber;

	public NewView(Member leader, long viewNumber)
	{
		this.leader = leader;
		this.viewNumber = viewNumber;
	}

	@Override
	public String toString()
	{
		return "NEW_VIEW " + leader.toString() + "(" + viewNumber + ")";
	}

	@Override
	public MessageType getMessageType()
	{
		return MessageType.NEW_VIEW;
	}
}
