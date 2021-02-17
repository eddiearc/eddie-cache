package pro.eddiecache.core.memory.shrinking;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.control.event.ElementEventType;
import pro.eddiecache.core.memory.IMemoryCache;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IElementAttributes;

/**
 * @author eddie
 */
public class ShrinkerThread<K, V> implements Runnable
{
	private static final Log log = LogFactory.getLog(ShrinkerThread.class);

	private final ContextCache<K, V> cache;

	private final long maxMemoryIdleTime;

	private final int maxSpoolPerRun;

	private boolean spoolLimit = false;

	public ShrinkerThread(ContextCache<K, V> cache)
	{
		super();

		this.cache = cache;

		long maxMemoryIdleTimeSeconds = cache.getCacheAttributes().getMaxMemoryIdleTimeSeconds();

		if (maxMemoryIdleTimeSeconds < 0)
		{
			this.maxMemoryIdleTime = -1;
		}
		else
		{
			this.maxMemoryIdleTime = maxMemoryIdleTimeSeconds * 1000;
		}

		this.maxSpoolPerRun = cache.getCacheAttributes().getMaxSpoolPerRun();
		if (this.maxSpoolPerRun != -1)
		{
			this.spoolLimit = true;
		}

	}

	@Override
	public void run()
	{
		shrink();
	}

	protected void shrink()
	{
		if (log.isDebugEnabled())
		{
			log.debug("Shrink memory cache for: " + cache.getCacheName());
		}

		IMemoryCache<K, V> memCache = cache.getMemoryCache();

		try
		{
			Set<K> keys = memCache.getKeySet();
			int size = keys.size();

			if (log.isDebugEnabled())
			{
				log.debug("Keys size: " + size);
			}

			ICacheElement<K, V> cacheElement;
			IElementAttributes attributes;

			int spoolCount = 0;

			for (K key : keys)
			{
				cacheElement = memCache.getQuiet(key);

				if (cacheElement == null)
				{
					continue;
				}

				attributes = cacheElement.getElementAttributes();

				boolean remove = false;

				long now = System.currentTimeMillis();

				if (!cacheElement.getElementAttributes().getIsEternal())
				{
					remove = cache.isExpired(cacheElement, now, ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
							ElementEventType.EXCEEDED_IDLETIME_BACKGROUND);

					if (remove)
					{
						memCache.remove(cacheElement.getKey());
					}
				}

				if (!remove && maxMemoryIdleTime != -1)
				{
					if (!spoolLimit || spoolCount < this.maxSpoolPerRun)
					{
						final long lastAccessTime = attributes.getLastAccessTime();

						if (lastAccessTime + maxMemoryIdleTime < now)
						{
							if (log.isDebugEnabled())
							{
								log.debug("Exceed memory idle time: " + cacheElement.getKey());
							}

							spoolCount++;

							memCache.remove(cacheElement.getKey());

							memCache.waterfal(cacheElement);

							key = null;
							cacheElement = null;
						}
					}
					else
					{
						if (log.isDebugEnabled())
						{
							log.debug("spoolCount = '" + spoolCount + "'; " + "maxSpoolPerRun = '" + maxSpoolPerRun
									+ "'");
						}

						if (spoolLimit && spoolCount >= this.maxSpoolPerRun)
						{
							return;
						}
					}
				}
			}
		}
		catch (Throwable t)
		{
			log.info("Error occur in shrink", t);

			return;
		}
	}
}
