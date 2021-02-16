package pro.eddiecache.utils.struct;

import java.io.Serializable;
import java.util.Map.Entry;

public class LRUMapEntry<K, V> implements Entry<K, V>, Serializable
{
	private static final long serialVersionUID = 1L;

	private final K key;

	private V value;

	public LRUMapEntry(K key, V value)
	{
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey()
	{
		return this.key;
	}

	@Override
	public V getValue()
	{
		return this.value;
	}

	@Override
	public V setValue(V valueArg)
	{
		V old = this.value;
		this.value = valueArg;
		return old;
	}
}
