package pro.eddiecache.utils.struct;

import java.util.concurrent.atomic.AtomicInteger;

public class LRUMap<K, V> extends AbstractLRUMap<K, V>
{
	int maxObjects = -1;
	AtomicInteger counter = new AtomicInteger(0);

	public LRUMap()
	{
		super();
	}

	public LRUMap(int maxObjects)
	{
		super();
		this.maxObjects = maxObjects;
	}

	@Override
	public boolean shouldRemove()
	{
		return maxObjects > 0 && this.size() > maxObjects;
	}

	public Object getMaxCounter()
	{
		return maxObjects;
	}
}
