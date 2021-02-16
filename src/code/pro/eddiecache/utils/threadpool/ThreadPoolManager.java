package pro.eddiecache.utils.threadpool;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadPoolManager
{
	private static final Log log = LogFactory.getLog(ThreadPoolManager.class);

	private static boolean useBoundary = true;

	private static int boundarySize = 2000;

	private static int maximumPoolSize = 150;

	private static int minimumPoolSize = 4;

	private static int keepAliveTime = 1000 * 60 * 5;

	private static PoolConfiguration.WhenBlockedPolicy whenBlockedPolicy = PoolConfiguration.WhenBlockedPolicy.RUN;

	private static int startUpSize = 4;

	private static PoolConfiguration defaultConfig;

	private static final String PROP_NAME_ROOT = "thread_pool";

	private static final String DEFAULT_PROP_NAME_ROOT = "thread_pool.default";

	private static volatile Properties props = null;

	private static ThreadPoolManager instance = null;

	private ConcurrentHashMap<String, ThreadPoolExecutor> pools;

	private ThreadPoolManager()
	{
		this.pools = new ConcurrentHashMap<String, ThreadPoolExecutor>();
		configure();
	}

	private ThreadPoolExecutor createPool(PoolConfiguration config)
	{
		BlockingQueue<Runnable> queue = null;
		if (config.isUseBoundary())
		{
			queue = new LinkedBlockingQueue<Runnable>(config.getBoundarySize());
		}
		else
		{
			queue = new LinkedBlockingQueue<Runnable>();
		}

		ThreadPoolExecutor pool = new ThreadPoolExecutor(config.getStartUpSize(), config.getMaximumPoolSize(),
				config.getKeepAliveTime(), TimeUnit.MILLISECONDS, queue,
				new CacheKitThreadFactory("CacheKit-ThreadPoolManager-"));

		switch (config.getWhenBlockedPolicy())
		{
			case ABORT:
				pool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
				break;

			case RUN:
				pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
				break;

			case WAIT:
				throw new RuntimeException("POLICY_WAIT no longer supported");

			case DISCARDOLDEST:
				pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
				break;

			default:
				break;
		}

		pool.prestartAllCoreThreads();

		return pool;
	}

	public static synchronized ThreadPoolManager getInstance()
	{
		if (instance == null)
		{
			instance = new ThreadPoolManager();
		}
		return instance;
	}

	public static synchronized void dispose()
	{
		if (instance != null)
		{
			for (String poolName : instance.getPoolNames())
			{
				try
				{
					instance.getPool(poolName).shutdownNow();
				}
				catch (Throwable t)
				{
					log.warn("fail to close pool " + poolName, t);
				}
			}

			instance = null;
		}
	}

	public ThreadPoolExecutor getPool(String name)
	{
		ThreadPoolExecutor pool = pools.get(name);

		if (pool == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("create pool for name [" + name + "]");
			}

			PoolConfiguration config = loadConfig(PROP_NAME_ROOT + "." + name);
			pool = createPool(config);
			ThreadPoolExecutor _pool = pools.putIfAbsent(name, pool);
			if (_pool != null)
			{
				pool = _pool;
			}

			if (log.isDebugEnabled())
			{
				log.debug("PoolName = " + getPoolNames());
			}
		}

		return pool;
	}

	public ArrayList<String> getPoolNames()
	{
		return new ArrayList<String>(pools.keySet());
	}

	public static void setProps(Properties props)
	{
		ThreadPoolManager.props = props;
	}

	private static void configure()
	{
		if (log.isDebugEnabled())
		{
			log.debug("initialize ThreadPoolManager");
		}

		if (props == null)
		{
			props = new Properties();
		}

		defaultConfig = new PoolConfiguration(useBoundary, boundarySize, maximumPoolSize, minimumPoolSize,
				keepAliveTime, whenBlockedPolicy, startUpSize);

		defaultConfig = loadConfig(DEFAULT_PROP_NAME_ROOT);
	}

	private static PoolConfiguration loadConfig(String root)
	{
		PoolConfiguration config = defaultConfig.clone();

		try
		{
			config.setUseBoundary(Boolean.parseBoolean(props.getProperty(root + ".useBoundary", "false")));
		}
		catch (NumberFormatException nfe)
		{
			log.error("useBoundary not a boolean.", nfe);
		}

		try
		{
			config.setBoundarySize(Integer.parseInt(props.getProperty(root + ".boundarySize", "2000")));
		}
		catch (NumberFormatException nfe)
		{
			log.error("boundarySize not a number.", nfe);
		}

		try
		{
			config.setMaximumPoolSize(Integer.parseInt(props.getProperty(root + ".maximumPoolSize", "150")));
		}
		catch (NumberFormatException nfe)
		{
			log.error("maximumPoolSize not a number.", nfe);
		}

		try
		{
			config.setMinimumPoolSize(Integer.parseInt(props.getProperty(root + ".minimumPoolSize", "4")));
		}
		catch (NumberFormatException nfe)
		{
			log.error("minimumPoolSize not a number.", nfe);
		}

		try
		{
			config.setKeepAliveTime(Integer.parseInt(props.getProperty(root + ".keepAliveTime", "300000")));
		}
		catch (NumberFormatException nfe)
		{
			log.error("keepAliveTime not a number.", nfe);
		}

		config.setWhenBlockedPolicy(props.getProperty(root + ".whenBlockedPolicy", "RUN"));

		try
		{
			config.setStartUpSize(Integer.parseInt(props.getProperty(root + ".startUpSize", "4")));
		}
		catch (NumberFormatException nfe)
		{
			log.error("startUpSize not a number.", nfe);
		}

		if (log.isInfoEnabled())
		{
			log.info(root + " PoolConfiguration = " + config);
		}

		return config;
	}
}
