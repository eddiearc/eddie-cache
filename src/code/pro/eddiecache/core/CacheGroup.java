package pro.eddiecache.core;

import pro.eddiecache.core.model.IElementAttributes;

public class CacheGroup
{
	private IElementAttributes attr;

	public CacheGroup()
	{
		super();
	}

	public void setElementAttributes(IElementAttributes attr)
	{
		this.attr = attr;
	}

	public IElementAttributes getElementAttrributes()
	{
		return attr;
	}
}
