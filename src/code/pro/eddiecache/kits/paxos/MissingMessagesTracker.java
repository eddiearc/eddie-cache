package pro.eddiecache.kits.paxos;

import java.util.HashSet;
import java.util.Set;

/**
 * @author eddie
 */
public class MissingMessagesTracker
{
	private long tail = 0;
	private Set<Long> received = new HashSet<Long>();

	public void received(long seqNo)
	{
		if (tail == seqNo)
		{
			tail++;
			advanceTail();
		}
		else
		{
			received.add(seqNo);
		}
	}

	private void advanceTail()
	{
		while (!received.isEmpty())
		{
			if (!received.contains(tail))
			{
				return;
			}
			received.remove(tail);
			tail++;
		}
	}

	public Set<Long> getMissing(long seqNo)
	{
		Set<Long> missingSuccess = new HashSet<Long>();
		for (long i = tail; i < seqNo; i++)
		{
			if (!received.contains(i))
			{
				missingSuccess.add(i);
			}
		}
		return missingSuccess;
	}
}
