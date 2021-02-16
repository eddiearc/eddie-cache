package pro.eddiecache.kits.paxos.messages;

import java.io.Serializable;

import pro.eddiecache.kits.paxos.comm.Member;

public interface MessageWithSender extends Serializable
{
	Member getSender();
}
