package pro.eddiecache.kits.paxos.comm;

import java.util.List;

/**
 * @author eddie
 * 命令行层
 */
public interface CommLayer
{

	/**
	 * 向多个成员发送信息
	 *
	 * @param members 成员列表
	 * @param message 信息
	 */
	void sendTo(List<Member> members, byte[] message);

	/**
	 * 向一个成员发送信息
	 *
	 * @param member 成员信息
	 * @param message 相关信息
	 */
	void sendTo(Member member, byte[] message);

	void setListener(MessageListener listener);

	void close();

	interface MessageListener
	{
		void receive(byte[] message);
	}
}
