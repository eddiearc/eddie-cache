package pro.eddiecache.access;

import java.io.IOException;

import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.model.IContextCacheAttributes;
import pro.eddiecache.core.model.IElementAttributes;
import pro.eddiecache.core.stats.ICacheStats;

public abstract class AbstractCacheKitAccess<K, V> implements ICacheKitAccessManagement
{
	private final ContextCache<K, V> cacheControl;

	protected AbstractCacheKitAccess(ContextCache<K, V> cacheControl)
	{
		this.cacheControl = cacheControl;
	}

	@Override
	public void clear() throws CacheException
	{
		try
		{
			this.getCacheControl().removeAll();
		}
		catch (IOException e)
		{
			throw new CacheException(e);
		}
	}

	@Override
	public void setDefaultElementAttributes(IElementAttributes attr) throws CacheException
	{
		this.getCacheControl().setElementAttributes(attr);
	}

	@Override
	public IElementAttributes getDefaultElementAttributes() throws CacheException
	{
		return this.getCacheControl().getElementAttributes();
	}

	@Override
	public ICacheStats getStatistics()
	{
		return this.getCacheControl().getStatistics();
	}

	@Override
	public String getStats()
	{
		return this.getCacheControl().getStats();
	}

	@Override
	public void dispose()
	{
		this.getCacheControl().dispose();
	}

	@Override
	public IContextCacheAttributes getCacheAttributes()
	{
		return this.getCacheControl().getCacheAttributes();
	}

	@Override
	public void setCacheAttributes(IContextCacheAttributes cattr)
	{
		this.getCacheControl().setCacheAttributes(cattr);
	}

	@Override
	public int freeMemoryElements(int numberToFree) throws CacheException
	{
		int numFreed = -1;
		try
		{
			numFreed = this.getCacheControl().getMemoryCache().freeElements(numberToFree);
		}
		catch (IOException ioe)
		{
			String message = "Fail to release memory elements";
			throw new CacheException(message, ioe);
		}
		return numFreed;
	}

	public ContextCache<K, V> getCacheControl()
	{
		return cacheControl;
	}

}
