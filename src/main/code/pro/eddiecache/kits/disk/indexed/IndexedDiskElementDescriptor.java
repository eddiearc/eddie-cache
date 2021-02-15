package pro.eddiecache.kits.disk.indexed;

import java.io.Serializable;

public class IndexedDiskElementDescriptor implements Serializable, Comparable<IndexedDiskElementDescriptor>
{

	private static final long serialVersionUID = 1L;

	long pos;

	int len;

	public IndexedDiskElementDescriptor(long pos, int len)
	{
		this.pos = pos;
		this.len = len;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[DED: ");
		sb.append(" pos = " + pos);
		sb.append(" len = " + len);
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		return Long.valueOf(this.pos).hashCode() ^ Integer.valueOf(len).hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		else if (o instanceof IndexedDiskElementDescriptor)
		{
			IndexedDiskElementDescriptor ided = (IndexedDiskElementDescriptor) o;
			return pos == ided.pos && len == ided.len;
		}

		return false;
	}

	@Override
	public int compareTo(IndexedDiskElementDescriptor o)
	{
		if (o == null)
		{
			return 1;
		}

		if (o.len == len)
		{
			if (o.pos == pos)
			{
				return 0;
			}
			else if (o.pos < pos)
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		else if (o.len > len)
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}
}
