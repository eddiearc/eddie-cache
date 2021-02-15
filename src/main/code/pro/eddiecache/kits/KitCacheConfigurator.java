package pro.eddiecache.kits;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.utils.config.OptionConverter;
import pro.eddiecache.utils.config.PropertySetter;
import pro.eddiecache.utils.serialization.StandardSerializer;

public class KitCacheConfigurator
{
	private static final Log log = LogFactory.getLog(KitCacheConfigurator.class);

	public static final String ATTRIBUTE_PREFIX = ".attributes";

	public static final String CACHE_EVENT_LOGGER_PREFIX = ".cacheeventlogger";

	public static final String SERIALIZER_PREFIX = ".serializer";

	public static ICacheEventWrapper parseCacheEventLogger(Properties props, String kitPrefix)
	{
		ICacheEventWrapper cacheEventWrapper = null;

		String eventLoggerClassName = kitPrefix + CACHE_EVENT_LOGGER_PREFIX;
		cacheEventWrapper = OptionConverter.instantiateByKey(props, eventLoggerClassName, null);
		if (cacheEventWrapper != null)
		{
			String cacheEventLoggerAttributePrefix = kitPrefix + CACHE_EVENT_LOGGER_PREFIX + ATTRIBUTE_PREFIX;
			PropertySetter.setProperties(cacheEventWrapper, props, cacheEventLoggerAttributePrefix + ".");
			if (log.isInfoEnabled())
			{
				log.info("Use custom cache event logger [" + cacheEventWrapper + "] for kit [" + kitPrefix + "]");
			}
		}
		else
		{
			if (log.isInfoEnabled())
			{
				log.info("No cache event logger defined for kit [" + kitPrefix + "]");
			}
		}
		return cacheEventWrapper;
	}

	public static IElementSerializer parseElementSerializer(Properties props, String kitPrefix)
	{
		IElementSerializer elementSerializer = null;

		String elementSerializerClassName = kitPrefix + SERIALIZER_PREFIX;
		elementSerializer = OptionConverter.instantiateByKey(props, elementSerializerClassName, null);
		if (elementSerializer != null)
		{
			String attributePrefix = kitPrefix + SERIALIZER_PREFIX + ATTRIBUTE_PREFIX;
			PropertySetter.setProperties(elementSerializer, props, attributePrefix + ".");
			if (log.isInfoEnabled())
			{
				log.info("Use custom element serializer [" + elementSerializer + "] for kit [" + kitPrefix + "]");
			}
		}
		else
		{
			elementSerializer = new StandardSerializer();
			if (log.isInfoEnabled())
			{
				log.info("Use standard serializer [" + elementSerializer + "] for kit [" + kitPrefix + "]");
			}
		}
		return elementSerializer;
	}
}
