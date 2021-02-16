package pro.eddiecache.core.memory.fifo;

import java.io.IOException;

import pro.eddiecache.core.memory.AbstractDoubleLinkedListMemoryCache;
import pro.eddiecache.core.memory.util.MemoryElementDescriptor;
import pro.eddiecache.core.model.ICacheElement;

public class FIFOMemoryCache<K, V> extends AbstractDoubleLinkedListMemoryCache<K, V>
{
	@Override
	protected MemoryElementDescriptor<K, V> adjustListForUpdate(ICacheElement<K, V> ce) throws IOException
	{
		return addFirst(ce);
	}

	@Override
	protected void adjustListForGet(MemoryElementDescriptor<K, V> me)
	{

	}
}
