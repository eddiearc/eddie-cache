package pro.eddiecache.core.memory.util;

import java.lang.ref.SoftReference;

import pro.eddiecache.core.model.ICacheElement;

public class SoftReferenceElementDescriptor<K, V> extends MemoryElementDescriptor<K, V>
{

	private static final long serialVersionUID = 1L;

	private final SoftReference<ICacheElement<K, V>> srce;

	public SoftReferenceElementDescriptor(ICacheElement<K, V> ce)
	{
		super(null);
		this.srce = new SoftReference<ICacheElement<K, V>>(ce);
	}

	@Override
	public ICacheElement<K, V> getCacheElement()
	{
		if (srce != null)
		{
			return srce.get();
		}
		return null;
	}
}
