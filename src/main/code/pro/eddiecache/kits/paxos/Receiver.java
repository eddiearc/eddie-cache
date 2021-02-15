package pro.eddiecache.kits.paxos;

import java.io.Serializable;

public interface Receiver
{
	void receive(Serializable message);
}
