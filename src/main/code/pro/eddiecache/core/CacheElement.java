package pro.eddiecache.core;

import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IElementAttributes;

public class CacheElement<K, V> implements ICacheElement<K, V>
{

	private static final long serialVersionUID = 1L;

	private final String cacheName;

	private final K key;

	private final V val;

	private IElementAttributes attr;

	public CacheElement(String cacheName, K key, V val)
	{
		this.cacheName = cacheName;
		this.key = key;
		this.val = val;
	}

	public CacheElement(String cacheName, K key, V val, IElementAttributes attrArg)
	{
		this(cacheName, key, val);
		this.attr = attrArg;
	}

	@Override
	public String getCacheName()
	{
		return this.cacheName;
	}

	@Override
	public K getKey()
	{
		return this.key;
	}

	@Override
	public V getVal()
	{
		return this.val;
	}

	@Override
	public void setElementAttributes(IElementAttributes attr)
	{
		this.attr = attr;
	}

	@Override
	public IElementAttributes getElementAttributes()
	{

		if (this.attr == null)
		{
			this.attr = new ElementAttributes();
		}
		return this.attr;
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

	@Override
	public String toString()
	{
		return "[CacheElement: cacheName [" + cacheName + "], key [" + key + "], val [" + val + "], attr [" + attr
				+ "]";
	}
}
