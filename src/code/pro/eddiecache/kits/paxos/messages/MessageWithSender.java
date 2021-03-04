package pro.eddiecache.kits.paxos.messages;

import java.io.Serializable;

import pro.eddiecache.kits.paxos.comm.Member;

/**
 * @author eddie
 * 信息 并 携带发送者的信息
 */
public interface MessageWithSender extends Serializable
{
	/**
	 * 获得发送者的相关信息 member
	 */
	Member getSender();
}
