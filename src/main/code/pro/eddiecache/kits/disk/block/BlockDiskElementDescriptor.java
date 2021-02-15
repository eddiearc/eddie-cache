package pro.eddiecache.kits.disk.block;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class BlockDiskElementDescriptor<K> implements Serializable, Externalizable
{
	private static final long serialVersionUID = 1L;

	private K key;

	private int[] blocks;

	public void setKey(K key)
	{
		this.key = key;
	}

	public K getKey()
	{
		return key;
	}

	public void setBlocks(int[] blocks)
	{
		this.blocks = blocks;
	}

	public int[] getBlocks()
	{
		return blocks;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n BlockDiskElementDescriptor");
		sb.append("\n key [" + this.getKey() + "]");
		sb.append("\n blocks [");
		if (this.getBlocks() != null)
		{
			for (int i = 0; i < blocks.length; i++)
			{
				sb.append(this.getBlocks()[i]);
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException
	{
		this.key = (K) input.readObject();
		this.blocks = (int[]) input.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException
	{
		output.writeObject(this.key);
		output.writeObject(this.blocks);
	}
}
