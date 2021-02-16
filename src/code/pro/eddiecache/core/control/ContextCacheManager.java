package pro.eddiecache.core.control;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.core.CacheConstants;
import pro.eddiecache.core.ContextCacheAttributes;
import pro.eddiecache.core.ElementAttributes;
import pro.eddiecache.core.control.event.ElementEventQueue;
import pro.eddiecache.core.control.event.IElementEventQueue;
import pro.eddiecache.core.model.ICache;
import pro.eddiecache.core.model.IContextCacheAttributes;
import pro.eddiecache.core.model.IContextCacheManager;
import pro.eddiecache.core.model.IElementAttributes;
import pro.eddiecache.core.model.IProvideScheduler;
import pro.eddiecache.core.model.IShutdownObserver;
import pro.eddiecache.core.stats.CacheStats;
import pro.eddiecache.core.stats.ICacheStats;
import pro.eddiecache.kits.KitCache;
import pro.eddiecache.kits.KitCacheAttributes;
import pro.eddiecache.kits.KitCacheFactory;
import pro.eddiecache.utils.config.OptionConverter;
import pro.eddiecache.utils.threadpool.CacheKitThreadFactory;
import pro.eddiecache.utils.threadpool.ThreadPoolManager;
import pro.eddiecache.xml.XmlParser;
import pro.eddiecache.core.model.ICacheType;

public class ContextCacheManager implements IContextCacheManager, IProvideScheduler
{
	private static final Log log = LogFactory.getLog(ContextCacheManager.class);

	private static final String DEFAULT_REGION = "cachekit.default";

	private final ConcurrentMap<String, ICache<?, ?>> caches = new ConcurrentHashMap<String, ICache<?, ?>>();

	private final ReentrantLock cacheLock = new ReentrantLock();

	private final AtomicInteger clients = new AtomicInteger(0);

	private IContextCacheAttributes defaultCacheAttr = new ContextCacheAttributes();

	private IElementAttributes defaultElementAttr = new ElementAttributes();

	private final ConcurrentMap<String, KitCacheFactory> kitFactoryRegistry = new ConcurrentHashMap<String, KitCacheFactory>();

	private final ConcurrentMap<String, KitCacheAttributes> kitAttributeRegistry = new ConcurrentHashMap<String, KitCacheAttributes>();

	private final ConcurrentMap<String, KitCache<?, ?>> kitCaches = new ConcurrentHashMap<String, KitCache<?, ?>>();

	private Properties configurationProperties;

	private String defaultKitValues;

	private static ContextCacheManager instance;

	private static final boolean DEFAULT_USE_SYSTEM_PROPERTIES = true;

	private static final boolean DEFAULT_FORCE_RECONFIGURATION = false;

	private final LinkedBlockingDeque<IShutdownObserver> shutdownObservers = new LinkedBlockingDeque<IShutdownObserver>();

	private ScheduledExecutorService scheduledExecutor;

	private IElementEventQueue elementEventQueue;

	private ShutdownHook shutdownHook;

	private boolean isInitialized = false;

	private boolean isConfigured = false;

	public static synchronized ContextCacheManager getInstance() throws CacheException
	{
		return getInstance(CacheConstants.DEFAULT_CONFIG);
	}

	/**
	 * 单例模式，ContextCacheManager只有一个实例对象
	 *
	 * @param propsFilename 加载对应的配置
	 */
	public static synchronized ContextCacheManager getInstance(String propsFilename) throws CacheException
	{
		if (instance == null)
		{
			if (log.isInfoEnabled())
			{
				log.info("Instance is null, creating with config [" + propsFilename + "]");
			}

			instance = createInstance();
		}

		if (!instance.isInitialized())
		{
			instance.initialize();
		}

		if (!instance.isConfigured())
		{
			instance.configure(propsFilename);
		}

		instance.clients.incrementAndGet();

		return instance;
	}

	public static synchronized ContextCacheManager getUnconfiguredInstance()
	{
		if (instance == null)
		{
			if (log.isInfoEnabled())
			{
				log.info("Instance is null, returning unconfigured instance");
			}

			instance = createInstance();
		}

		if (!instance.isInitialized())
		{
			instance.initialize();
		}

		instance.clients.incrementAndGet();

		return instance;
	}

	protected static ContextCacheManager createInstance()
	{
		return new ContextCacheManager();
	}

	protected ContextCacheManager()
	{

	}

	protected void initialize()
	{
		if (!isInitialized)
		{
			this.shutdownHook = new ShutdownHook();
			try
			{
				Runtime.getRuntime().addShutdownHook(shutdownHook);
			}
			catch (AccessControlException e)
			{
				log.error("Could not register shutdown hook.", e);
			}

			this.scheduledExecutor = Executors.newScheduledThreadPool(4,
					new CacheKitThreadFactory("CacheKit-Scheduler-", Thread.MIN_PRIORITY));

			this.elementEventQueue = new ElementEventQueue();

			isInitialized = true;
		}
	}

	public IElementEventQueue getElementEventQueue()
	{
		return elementEventQueue;
	}

	@Override
	public ScheduledExecutorService getScheduledExecutorService()
	{
		return scheduledExecutor;
	}

	public void configure() throws CacheException
	{
		configure(CacheConstants.DEFAULT_CONFIG);
	}

	public void configure(String xmlFile) throws CacheException
	{
		log.info("Create cache manager from config file: " + xmlFile);

		Properties props = new Properties();

		props = XmlParser.getPropertiesFromXml(xmlFile);
		configure(props);
	}

	public void configure(Properties props)
	{
		configure(props, DEFAULT_USE_SYSTEM_PROPERTIES);
	}

	public void configure(Properties props, boolean useSystemProperties)
	{
		configure(props, useSystemProperties, DEFAULT_FORCE_RECONFIGURATION);
	}

	public synchronized void configure(Properties props, boolean useSystemProperties, boolean forceReconfiguration)
	{
		if (props == null)
		{
			log.error("No properties found.");
			return;
		}

		if (isConfigured)
		{
			if (!forceReconfiguration)
			{

				return;
			}
			else
			{

			}
		}
		if (useSystemProperties)
		{
			ContextCacheConfigurator.overrideWithSystemProperties(props);
		}
		doConfigure(props);
	}

	private void doConfigure(Properties properties)
	{
		this.configurationProperties = properties;

		ThreadPoolManager.setProps(properties);
		ThreadPoolManager poolMgr = ThreadPoolManager.getInstance();
		if (log.isDebugEnabled())
		{
			log.debug("ThreadPoolManager = " + poolMgr);
		}

		ContextCacheConfigurator configurator = newConfigurator();

		long start = System.currentTimeMillis();

		this.defaultKitValues = OptionConverter.findAndSubst(ContextCacheManager.DEFAULT_REGION, properties);

		log.info("Set default kits to " + defaultKitValues);

		this.defaultCacheAttr = configurator.parseContextCacheAttributes(properties, "", new ContextCacheAttributes(),
				DEFAULT_REGION);

		log.info("Set defaultContextCacheAttributes to " + defaultCacheAttr);

		this.defaultElementAttr = configurator.parseElementAttributes(properties, "", new ElementAttributes(),
				DEFAULT_REGION);

		log.info("Set defaultElementAttributes to " + defaultElementAttr);

		configurator.parseSystemCaches(properties, this);

		configurator.parseCaches(properties, this);

		long end = System.currentTimeMillis();
		if (log.isInfoEnabled())
		{
			log.info("Finish configuration in " + (end - start) + " ms.");
		}

		isConfigured = true;
	}

	public IContextCacheAttributes getDefaultCacheAttributes()
	{
		return this.defaultCacheAttr.clone();
	}

	public IElementAttributes getDefaultElementAttributes()
	{
		return this.defaultElementAttr.clone();
	}

	@Override
	public <K, V> ContextCache<K, V> getCache(String cacheName)
	{
		return getCache(cacheName, getDefaultCacheAttributes());
	}

	public <K, V> ContextCache<K, V> getCache(String cacheName, IContextCacheAttributes cattr)
	{
		cattr.setCacheName(cacheName);
		return getCache(cattr, getDefaultElementAttributes());
	}

	public <K, V> ContextCache<K, V> getCache(String cacheName, IContextCacheAttributes cattr, IElementAttributes attr)
	{
		cattr.setCacheName(cacheName);
		return getCache(cattr, attr);
	}

	public <K, V> ContextCache<K, V> getCache(IContextCacheAttributes cattr)
	{
		return getCache(cattr, getDefaultElementAttributes());
	}

	@SuppressWarnings("unchecked")
	public <K, V> ContextCache<K, V> getCache(IContextCacheAttributes cattr, IElementAttributes attr)
	{
		ContextCache<K, V> cache;

		if (log.isDebugEnabled())
		{
			log.debug("attr = " + attr);
		}

		cache = (ContextCache<K, V>) caches.get(cattr.getCacheName());

		if (cache == null)
		{
			cacheLock.lock();

			try
			{
				cache = (ContextCache<K, V>) caches.get(cattr.getCacheName());

				if (cache == null)
				{
					ContextCacheConfigurator configurator = newConfigurator();

					cache = configurator.parseCache(this.getConfigurationProperties(), this, cattr.getCacheName(),
							this.defaultKitValues, cattr);

					caches.put(cattr.getCacheName(), cache);
				}
			}
			finally
			{
				cacheLock.unlock();
			}
		}

		return cache;
	}

	protected ContextCacheConfigurator newConfigurator()
	{
		return new ContextCacheConfigurator();
	}

	public void freeCache(String name)
	{
		freeCache(name, false);
	}

	public void freeCache(String name, boolean fromRemote)
	{
		ContextCache<?, ?> cache = (ContextCache<?, ?>) caches.remove(name);

		if (cache != null)
		{
			cache.dispose(fromRemote);
		}
	}

	public void shutDown()
	{
		synchronized (ContextCacheManager.class)
		{
			this.elementEventQueue.dispose();

			this.scheduledExecutor.shutdownNow();

			ThreadPoolManager.dispose();

			IShutdownObserver observer = null;
			while ((observer = shutdownObservers.poll()) != null)
			{
				observer.shutdown();
			}

			for (String name : getCacheNames())
			{
				freeCache(name);
			}

			for (KitCacheFactory factory : kitFactoryRegistry.values())
			{
				factory.dispose();
			}

			kitAttributeRegistry.clear();
			kitFactoryRegistry.clear();

			if (shutdownHook != null)
			{
				try
				{
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
				}
				catch (IllegalStateException e)
				{
				}

				this.shutdownHook = null;
			}

			isConfigured = false;
			isInitialized = false;
		}
	}

	public void release()
	{
		release(false);
	}

	private void release(boolean fromRemote)
	{
		synchronized (ContextCacheManager.class)
		{
			if (clients.decrementAndGet() > 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Release called, but " + clients + " remain");
				}
				return;
			}

			for (ICache<?, ?> c : caches.values())
			{
				ContextCache<?, ?> cache = (ContextCache<?, ?>) c;

				if (cache != null)
				{
					cache.dispose(fromRemote);
				}
			}
		}
	}

	public String[] getCacheNames()
	{
		return caches.keySet().toArray(new String[caches.size()]);
	}

	public ICacheType.CacheType getCacheType()
	{
		return ICacheType.CacheType.CACHE_HUB;
	}

	public void registryFacPut(KitCacheFactory kitFac)
	{
		kitFactoryRegistry.put(kitFac.getName(), kitFac);
	}

	public KitCacheFactory registryFacGet(String name)
	{
		return kitFactoryRegistry.get(name);
	}

	public void registryAttrPut(KitCacheAttributes kitAttr)
	{
		kitAttributeRegistry.put(kitAttr.getName(), kitAttr);
	}

	public KitCacheAttributes registryAttrGet(String name)
	{
		return kitAttributeRegistry.get(name);
	}

	public void addCache(String cacheName, ICache<?, ?> cache)
	{
		caches.put(cacheName, cache);
	}

	public void addKitCache(String kitName, String cacheName, KitCache<?, ?> cache)
	{
		String key = String.format("kit.%s.cacheName.%s", kitName, cacheName);
		kitCaches.put(key, cache);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K, V> KitCache<K, V> getKitCache(String kitName, String cacheName)
	{
		String key = String.format("kit.%s.cacheName.%s", kitName, cacheName);
		return (KitCache<K, V>) kitCaches.get(key);
	}

	@Override
	public String getStats()
	{
		ICacheStats[] stats = getStatistics();
		if (stats == null)
		{
			return "NONE";
		}

		StringBuilder buf = new StringBuilder();
		int statsLen = stats.length;
		for (int i = 0; i < statsLen; i++)
		{
			buf.append("\n---------------------------\n");
			buf.append(stats[i]);
		}
		return buf.toString();
	}

	public ICacheStats[] getStatistics()
	{
		ArrayList<ICacheStats> cacheStats = new ArrayList<ICacheStats>();
		for (ICache<?, ?> c : caches.values())
		{
			ContextCache<?, ?> cache = (ContextCache<?, ?>) c;
			if (cache != null)
			{
				cacheStats.add(cache.getStatistics());
			}
		}
		ICacheStats[] stats = cacheStats.toArray(new CacheStats[0]);
		return stats;
	}

	@Override
	public void registerShutdownObserver(IShutdownObserver observer)
	{
		if (!shutdownObservers.contains(observer))
		{
			shutdownObservers.push(observer);
		}
		else
		{
			log.warn("Shutdown observer added twice " + observer);
		}
	}

	@Override
	public void deregisterShutdownObserver(IShutdownObserver observer)
	{
		shutdownObservers.remove(observer);
	}

	@Override
	public Properties getConfigurationProperties()
	{
		return configurationProperties;
	}

	public boolean isInitialized()
	{
		return isInitialized;
	}

	public boolean isConfigured()
	{
		return isConfigured;
	}

	class ShutdownHook extends Thread
	{
		@Override
		public void run()
		{
			if (isInitialized())
			{
				log.info("Shut down eddie-cache...");
				shutDown();
			}
		}
	}
}
