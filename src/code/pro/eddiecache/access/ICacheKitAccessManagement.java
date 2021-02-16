package pro.eddiecache.access;

import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.core.model.IContextCacheAttributes;
import pro.eddiecache.core.model.IElementAttributes;
import pro.eddiecache.core.stats.ICacheStats;

/**
 * @author eddie
 */
public interface ICacheKitAccessManagement
{
	void dispose();

	void clear() throws CacheException;

	IElementAttributes getDefaultElementAttributes() throws CacheException;

	void setDefaultElementAttributes(IElementAttributes attr) throws CacheException;

	IContextCacheAttributes getCacheAttributes();

	void setCacheAttributes(IContextCacheAttributes cattr);

	int freeMemoryElements(int numberToFree) throws CacheException;

	ICacheStats getStatistics();

	String getStats();
}
