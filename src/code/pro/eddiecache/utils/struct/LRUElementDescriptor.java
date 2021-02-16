package pro.eddiecache.utils.struct;

public class LRUElementDescriptor<K, V> extends DoubleLinkedListNode<V>
{
	private static final long serialVersionUID = 1L;

	private K key;

	public LRUElementDescriptor(K key, V payloadP)
	{
		super(payloadP);
		this.setKey(key);
	}

	public void setKey(K key)
	{
		this.key = key;
	}

	public K getKey()
	{
		return key;
	}
}
