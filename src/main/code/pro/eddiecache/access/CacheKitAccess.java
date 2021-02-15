package pro.eddiecache.access;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.access.exception.InvalidArgumentException;
import pro.eddiecache.access.exception.InvalidHandleException;
import pro.eddiecache.access.exception.ObjectExistsException;
import pro.eddiecache.core.CacheElement;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IElementAttributes;

public class CacheKitAccess<K, V> extends AbstractCacheKitAccess<K, V> implements ICacheKitAccess<K, V>
{
	private static final Log log = LogFactory.getLog(CacheKitAccess.class);

	public CacheKitAccess(ContextCache<K, V> cacheControl)
	{
		super(cacheControl);
	}

	@Override
	public V get(K name)
	{
		ICacheElement<K, V> element = this.getCacheControl().get(name);

		return (element != null) ? element.getVal() : null;
	}

	@Override
	public Map<K, V> getMatching(String pattern)
	{
		HashMap<K, V> unwrappedResults = new HashMap<K, V>();

		Map<K, ICacheElement<K, V>> wrappedResults = this.getCacheControl().getMatching(pattern);
		if (wrappedResults != null)
		{
			for (Map.Entry<K, ICacheElement<K, V>> entry : wrappedResults.entrySet())
			{
				ICacheElement<K, V> element = entry.getValue();
				if (element != null)
				{
					unwrappedResults.put(entry.getKey(), element.getVal());
				}
			}
		}
		return unwrappedResults;
	}

	@Override
	public ICacheElement<K, V> getCacheElement(K name)
	{
		return this.getCacheControl().get(name);
	}

	@Override
	public Map<K, ICacheElement<K, V>> getCacheElements(Set<K> names)
	{
		return this.getCacheControl().getMultiple(names);
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMatchingCacheElements(String pattern)
	{
		return this.getCacheControl().getMatching(pattern);
	}

	@Override
	public void putSafe(K key, V value)
	{
		if (this.getCacheControl().get(key) != null)
		{
			throw new ObjectExistsException("putSafe failed.  Object exists in the cache for key [" + key + "]");
		}
		put(key, value);
	}

	@Override
	public void put(K name, V obj)
	{

		put(name, obj, this.getCacheControl().getElementAttributes());
	}

	@Override
	public void put(K key, V val, IElementAttributes attr)
	{
		if (key == null)
		{
			throw new InvalidArgumentException("Key must not be null");
		}

		if (val == null)
		{
			throw new InvalidArgumentException("Value must not be null");
		}

		try
		{
			CacheElement<K, V> ce = new CacheElement<K, V>(this.getCacheControl().getCacheName(), key, val);

			ce.setElementAttributes(attr);

			this.getCacheControl().update(ce);
		}
		catch (IOException e)
		{
			throw new CacheException(e);
		}
	}

	@Override
	public void remove(K name)
	{
		this.getCacheControl().remove(name);
	}

	@Override
	public void resetElementAttributes(K name, IElementAttributes attr)
	{
		ICacheElement<K, V> element = this.getCacheControl().get(name);

		if (element == null)
		{
			throw new InvalidHandleException("Object for name [" + name + "] is not in the cache");
		}

		put(element.getKey(), element.getVal(), attr);
	}

	@Override
	public IElementAttributes getElementAttributes(K name)
	{
		IElementAttributes attr = null;

		try
		{
			attr = this.getCacheControl().getElementAttributes(name);
		}
		catch (IOException ioe)
		{
			log.error("Fail to  get element attributes", ioe);
		}

		return attr;
	}
}
