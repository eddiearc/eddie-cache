package pro.eddiecache.kits.paxos;

import java.util.Set;

import pro.eddiecache.kits.paxos.comm.Member;

public interface FailureListener
{
	void memberFailed(Member member, Set<Member> aliveMembers);
}
