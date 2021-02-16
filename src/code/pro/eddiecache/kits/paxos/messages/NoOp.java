package pro.eddiecache.kits.paxos.messages;

import java.io.Serializable;

/**
 * 
 * 一朝天子一朝臣，新leader上任，要求各地补交前朝的亏空
 */
public class NoOp implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof NoOp;
	}
}
