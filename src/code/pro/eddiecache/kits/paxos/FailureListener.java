package pro.eddiecache.kits.paxos;

import java.util.Set;

import pro.eddiecache.kits.paxos.comm.Member;

/**
 * @author eddie
 */
public interface FailureListener
{
	/**
	 * 标记某节点失效
	 *
	 * @param member 节点信息
	 * @param aliveMembers 存活的节点集合
	 */
	void memberFailed(Member member, Set<Member> aliveMembers);
}
