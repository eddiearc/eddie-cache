package pro.eddiecache.core;

import java.util.Arrays;

import pro.eddiecache.core.model.ICacheElementSerialized;
import pro.eddiecache.core.model.IElementAttributes;

public class CacheElementSerialized<K, V> extends CacheElement<K, V> implements ICacheElementSerialized<K, V>
{

	private static final long serialVersionUID = 1L;
	private final byte[] serializedValue;

	public CacheElementSerialized(String cacheName, K key, byte[] serializedValue,
			IElementAttributes elementAttributesArg)
	{
		super(cacheName, key, null, elementAttributesArg);
		this.serializedValue = serializedValue;
	}

	@Override
	public byte[] getSerializedValue()
	{
		return this.serializedValue;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n CacheElementSerialized: ");
		sb.append("\n CacheName = [" + getCacheName() + "]");
		sb.append("\n Key = [" + getKey() + "]");
		sb.append("\n SerializedValue = " + Arrays.toString(getSerializedValue()));
		sb.append("\n ElementAttributes = " + getElementAttributes());
		return sb.toString();
	}

}
