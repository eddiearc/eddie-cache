package pro.eddiecache.kits.disk;

import pro.eddiecache.core.CacheElement;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IElementAttributes;

public class PurgatoryElement<K, V> extends CacheElement<K, V>
{
	private static final long serialVersionUID = 1L;

	private boolean spoolable = false;

	private ICacheElement<K, V> cacheElement;

	public PurgatoryElement(ICacheElement<K, V> cacheElement)
	{
		super(cacheElement.getCacheName(), cacheElement.getKey(), cacheElement.getVal(),
				cacheElement.getElementAttributes());
		this.cacheElement = cacheElement;
	}

	public boolean isSpoolable()
	{
		return spoolable;
	}

	public void setSpoolable(boolean spoolable)
	{
		this.spoolable = spoolable;
	}

	public ICacheElement<K, V> getCacheElement()
	{
		return cacheElement;
	}

	@Override
	public String getCacheName()
	{
		return cacheElement.getCacheName();
	}

	@Override
	public K getKey()
	{
		return cacheElement.getKey();
	}

	@Override
	public V getVal()
	{
		return cacheElement.getVal();
	}

	@Override
	public IElementAttributes getElementAttributes()
	{
		return cacheElement.getElementAttributes();
	}

	@Override
	public void setElementAttributes(IElementAttributes attr)
	{
		cacheElement.setElementAttributes(attr);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[PurgatoryElement: ");
		sb.append(" isSpoolable = " + isSpoolable());
		sb.append(" CacheElement = " + getCacheElement());
		sb.append(" CacheName = " + getCacheName());
		sb.append(" Key = " + getKey());
		sb.append(" Value = " + getVal());
		sb.append(" ElementAttributes = " + getElementAttributes());
		sb.append("]");
		return sb.toString();
	}
}
