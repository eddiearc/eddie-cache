package pro.eddiecache.core.model;

import java.io.Serializable;

public interface ICacheElement<K, V> extends Serializable
{

	String getCacheName();

	K getKey();

	V getVal();

	IElementAttributes getElementAttributes();

	void setElementAttributes(IElementAttributes attr);
}
