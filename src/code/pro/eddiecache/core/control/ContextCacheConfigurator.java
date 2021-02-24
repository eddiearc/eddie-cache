package pro.eddiecache.core.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.match.IKeyMatcher;
import pro.eddiecache.core.match.KeyMatcher;
import pro.eddiecache.core.model.ICache;
import pro.eddiecache.core.model.IContextCacheAttributes;
import pro.eddiecache.core.model.IElementAttributes;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.core.model.IRequireScheduler;
import pro.eddiecache.kits.KitCache;
import pro.eddiecache.kits.KitCacheAttributes;
import pro.eddiecache.kits.KitCacheConfigurator;
import pro.eddiecache.kits.KitCacheFactory;
import pro.eddiecache.utils.config.OptionConverter;
import pro.eddiecache.utils.config.PropertySetter;

public class ContextCacheConfigurator
{
	private static final Log log = LogFactory.getLog(ContextCacheConfigurator.class);

	protected static final String SYSTEM_PROPERTY_KEY_PREFIX = "cachekit";

	protected static final String CACHE_PREFIX = "cachekit.";

	protected static final String SYSTEM_CACHE_PREFIX = "cachekit.system.";

	protected static final String KIT_PREFIX = "cachekit.kit.";

	protected static final String ATTRIBUTE_PREFIX = ".attributes";

	protected static final String CACHE_ATTRIBUTE_PREFIX = ".cacheattributes";

	protected static final String ELEMENT_ATTRIBUTE_PREFIX = ".elementattributes";

	public static final String KEY_MATCHER_PREFIX = ".keymatcher";

	public ContextCacheConfigurator()
	{

	}

	protected void parseSystemCaches(Properties props, ContextCacheManager ccm)
	{
		for (String key : props.stringPropertyNames())
		{
			if (key.startsWith(SYSTEM_CACHE_PREFIX) && key.indexOf("attributes") == -1)
			{
				String cacheName = key.substring(SYSTEM_CACHE_PREFIX.length());
				String kits = OptionConverter.findAndSubst(key, props);
				ICache<?, ?> cache;
				synchronized (cacheName)
				{
					cache = parseCache(props, ccm, cacheName, kits, null, SYSTEM_CACHE_PREFIX);
				}
				ccm.addCache(cacheName, cache);
			}
		}
	}

	protected void parseCaches(Properties props, ContextCacheManager ccm)
	{
		List<String> cacheNames = new ArrayList<String>();

		for (String key : props.stringPropertyNames())
		{
			if (key.startsWith(CACHE_PREFIX) && key.indexOf("attributes") == -1)
			{
				if (key.startsWith("cachekit.kit"))
				{
					continue;
				}
				String cacheName = key.substring(CACHE_PREFIX.length());
				cacheNames.add(cacheName);
				String kits = OptionConverter.findAndSubst(key, props);
				ICache<?, ?> cache;
				synchronized (cacheName)
				{
					cache = parseCache(props, ccm, cacheName, kits);
				}
				ccm.addCache(cacheName, cache);
			}
		}

		if (log.isInfoEnabled())
		{
			log.info("Parse caches " + cacheNames);
		}
	}

	protected <K, V> ContextCache<K, V> parseCache(Properties props, ContextCacheManager ccm, String cacheName,
			String kits)
	{
		return parseCache(props, ccm, cacheName, kits, null, CACHE_PREFIX);
	}

	protected <K, V> ContextCache<K, V> parseCache(Properties props, ContextCacheManager ccm, String cacheName,
			String kits, IContextCacheAttributes cca)
	{
		return parseCache(props, ccm, cacheName, kits, cca, CACHE_PREFIX);
	}

	/**
	 * 解析出对应Cache实例
	 *
	 * @param props
	 * @param ccm
	 * @param cacheName
	 * @param kits
	 * @param cca
	 * @param cachePrefix
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	protected <K, V> ContextCache<K, V> parseCache(Properties props, ContextCacheManager ccm, String cacheName,
			String kits, IContextCacheAttributes cca, String cachePrefix)
	{
		IElementAttributes ea = parseElementAttributes(props, cacheName, ccm.getDefaultElementAttributes(),
				cachePrefix);

		IContextCacheAttributes instantiationCca = cca == null
				? parseContextCacheAttributes(props, cacheName, ccm.getDefaultCacheAttributes(), cachePrefix)
				: cca;
		ContextCache<K, V> cache = newCache(instantiationCca, ea);

		cache.setContextCacheManager(ccm);

		cache.setScheduledExecutorService(ccm.getScheduledExecutorService());

		cache.setElementEventQueue(ccm.getElementEventQueue());

		if (cache.getMemoryCache() instanceof IRequireScheduler)
		{
			((IRequireScheduler) cache.getMemoryCache()).setScheduledExecutorService(ccm.getScheduledExecutorService());
		}

		if (kits != null)
		{
			List<KitCache<K, V>> kitList = new ArrayList<KitCache<K, V>>();

			if (log.isDebugEnabled())
			{
				log.debug("Parse cache name '" + cacheName + "', value '" + kits + "'");
			}
			StringTokenizer st = new StringTokenizer(kits, ",");

			if (!(kits.startsWith(",") || kits.equals("")))
			{
				if (!st.hasMoreTokens())
				{
					return null;
				}
			}
			KitCache<K, V> kitCache;
			String kitName;
			while (st.hasMoreTokens())
			{
				kitName = st.nextToken().trim();
				if (kitName == null || kitName.equals(","))
				{
					continue;
				}
				log.debug("Parse kit named: " + kitName);
				kitCache = parseKit(props, ccm, kitName, cacheName);
				if (kitCache != null)
				{
					if (kitCache instanceof IRequireScheduler)
					{
						((IRequireScheduler) kitCache).setScheduledExecutorService(ccm.getScheduledExecutorService());
					}
					kitList.add(kitCache);
				}
			}
			@SuppressWarnings("unchecked")
			KitCache<K, V>[] kitArray = kitList.toArray(new KitCache[0]);
			cache.setKitCaches(kitArray);
		}

		return cache;
	}

	protected <K, V> ContextCache<K, V> newCache(IContextCacheAttributes cca, IElementAttributes ea)
	{
		return new ContextCache<K, V>(cca, ea);
	}

	protected IContextCacheAttributes parseContextCacheAttributes(Properties props, String cacheName,
			IContextCacheAttributes defaultCCAttr)
	{
		return parseContextCacheAttributes(props, cacheName, defaultCCAttr, CACHE_PREFIX);
	}

	protected IContextCacheAttributes parseContextCacheAttributes(Properties props, String cacheName,
			IContextCacheAttributes defaultCCAttr, String regionPrefix)
	{
		IContextCacheAttributes ccAttr;

		String attrName = regionPrefix + cacheName + CACHE_ATTRIBUTE_PREFIX;

		ccAttr = OptionConverter.instantiateByKey(props, attrName, null);

		if (ccAttr == null)
		{
			if (log.isInfoEnabled())
			{
				log.info("No special ContextCacheAttributes class defined for key [" + attrName
						+ "], using default class.");
			}

			ccAttr = defaultCCAttr;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Parse options for: " + attrName);
		}

		PropertySetter.setProperties(ccAttr, props, attrName + ".");
		ccAttr.setCacheName(cacheName);

		if (log.isDebugEnabled())
		{
			log.debug("End of parsing for: " + attrName);
		}

		ccAttr.setCacheName(cacheName);
		return ccAttr;
	}

	protected IElementAttributes parseElementAttributes(Properties props, String cacheName,
			IElementAttributes defaultEAttr, String regionPrefix)
	{
		IElementAttributes eAttr;

		String attrName = regionPrefix + cacheName + ContextCacheConfigurator.ELEMENT_ATTRIBUTE_PREFIX;

		eAttr = OptionConverter.instantiateByKey(props, attrName, null);
		if (eAttr == null)
		{
			if (log.isInfoEnabled())
			{
				log.info("No special ElementAttribute class defined for key [" + attrName + "], using default class.");
			}

			eAttr = defaultEAttr;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Parse options for: " + attrName);
		}

		PropertySetter.setProperties(eAttr, props, attrName + ".");

		if (log.isDebugEnabled())
		{
			log.debug("End of parsing for: " + attrName);
		}

		return eAttr;
	}

	protected <K, V> KitCache<K, V> parseKit(Properties props, ContextCacheManager ccm, String kitName,
			String cacheName)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Parse kit " + kitName);
		}

		@SuppressWarnings("unchecked")
		KitCache<K, V> kitCache = (KitCache<K, V>) ccm.getKitCache(kitName, cacheName);

		if (kitCache == null)
		{
			KitCacheFactory kitFac = ccm.registryFacGet(kitName);
			if (kitFac == null)
			{
				String prefix = KIT_PREFIX + kitName;
				kitFac = OptionConverter.instantiateByKey(props, prefix, null);
				if (kitFac == null)
				{
					log.error("Could not instantiate kitFactory named: " + kitName);
					return null;
				}

				kitFac.setName(kitName);

				if (kitFac instanceof IRequireScheduler)
				{
					((IRequireScheduler) kitFac).setScheduledExecutorService(ccm.getScheduledExecutorService());
				}

				kitFac.initialize();
				ccm.registryFacPut(kitFac);
			}

			KitCacheAttributes kitAttr = ccm.registryAttrGet(kitName);
			String attrName = KIT_PREFIX + kitName + ATTRIBUTE_PREFIX;
			if (kitAttr == null)
			{
				String prefix = KIT_PREFIX + kitName + ATTRIBUTE_PREFIX;
				kitAttr = OptionConverter.instantiateByKey(props, prefix, null);
				if (kitAttr == null)
				{
					log.error("Could not instantiate kitAttr named '" + attrName + "'");
					return null;
				}
				kitAttr.setName(kitName);
				ccm.registryAttrPut(kitAttr);
			}

			kitAttr = kitAttr.clone();

			if (log.isDebugEnabled())
			{
				log.debug("Parse options for '" + attrName + "'");
			}

			PropertySetter.setProperties(kitAttr, props, attrName + ".");
			kitAttr.setCacheName(cacheName);

			if (log.isDebugEnabled())
			{
				log.debug("End of parsing for '" + attrName + "'");
			}

			kitAttr.setCacheName(cacheName);

			String kitPrefix = KIT_PREFIX + kitName;

			ICacheEventWrapper cacheEventWrapper = KitCacheConfigurator.parseCacheEventLogger(props, kitPrefix);

			IElementSerializer elementSerializer = KitCacheConfigurator.parseElementSerializer(props, kitPrefix);

			try
			{
				kitCache = kitFac.createCache(kitAttr, ccm, cacheEventWrapper, elementSerializer);
			}
			catch (Exception e)
			{
				log.error("Could not instantiate kit cache named: " + cacheName);
				return null;
			}

			ccm.addKitCache(kitName, cacheName, kitCache);
		}

		return kitCache;
	}

	/**
	 * 若JVM中的系统变量中存在相关变量，则覆盖props中的变量信息
	 */
	protected static void overrideWithSystemProperties(Properties props)
	{
		Properties sysProps = System.getProperties();
		for (String key : sysProps.stringPropertyNames())
		{
			if (key.startsWith(SYSTEM_PROPERTY_KEY_PREFIX))
			{
				if (log.isInfoEnabled())
				{
					log.info("Use system property [[" + key + "] [" + sysProps.getProperty(key) + "]]");
				}
				props.setProperty(key, sysProps.getProperty(key));
			}
		}
	}

	protected <K> IKeyMatcher<K> parseKeyMatcher(Properties props, String kitPrefix)
	{
		String keyMatcherClassName = kitPrefix + KEY_MATCHER_PREFIX;
		IKeyMatcher<K> keyMatcher = OptionConverter.instantiateByKey(props, keyMatcherClassName, null);
		if (keyMatcher != null)
		{
			String attributePrefix = kitPrefix + KEY_MATCHER_PREFIX + ATTRIBUTE_PREFIX;
			PropertySetter.setProperties(keyMatcher, props, attributePrefix + ".");
			if (log.isInfoEnabled())
			{
				log.info("Use custom key matcher [" + keyMatcher + "] for kit [" + kitPrefix + "]");
			}
		}
		else
		{
			keyMatcher = new KeyMatcher<K>();
			if (log.isInfoEnabled())
			{
				log.info("Use standard key matcher [" + keyMatcher + "] for kit [" + kitPrefix + "]");
			}
		}
		return keyMatcher;
	}
}
