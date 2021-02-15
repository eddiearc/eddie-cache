package pro.eddiecache.utils.struct;

import java.io.Serializable;

public class DoubleLinkedListNode<T> implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final T payload;

	public DoubleLinkedListNode<T> prev;

	public DoubleLinkedListNode<T> next;

	public DoubleLinkedListNode(T payload)
	{
		this.payload = payload;
	}

	public T getPayload()
	{
		return this.payload;
	}
}
