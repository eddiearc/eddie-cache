package pro.eddiecache.kits.lateral;

import java.io.Serializable;

import pro.eddiecache.core.model.ICacheElement;

public class LateralElementDescriptor<K, V> implements Serializable
{
	private static final long serialVersionUID = 1L;

	public ICacheElement<K, V> ce;

	public long requesterId;

	public LateralCommand command = LateralCommand.UPDATE;

	public int valHashCode = -1;

	public LateralElementDescriptor()
	{
		super();
	}

	public LateralElementDescriptor(ICacheElement<K, V> ce)
	{
		this.ce = ce;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n LateralElementDescriptor ");
		sb.append("\n command = [" + this.command + "]");
		sb.append("\n valHashCode = [" + this.valHashCode + "]");
		sb.append("\n ICacheElement = [" + this.ce + "]");
		return sb.toString();
	}
}
