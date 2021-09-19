package pro.eddiecache.utils.serialization;

import java.io.IOException;

import com.sun.istack.internal.NotNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheElement;
import pro.eddiecache.core.CacheElementSerialized;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheElementSerialized;
import pro.eddiecache.core.model.IElementSerializer;

public class SerializationConversionUtil {
	private static final Log log = LogFactory.getLog(SerializationConversionUtil.class);

	public static <K, V> ICacheElementSerialized serializeCacheElement(ICacheElement<K, V> element) throws IOException {
		return serializeCacheElement(element, StandardSerializer.getInstance());
	}

	public static <K, V> ICacheElementSerialized serializeCacheElement(
			ICacheElement<K, V> element,
			@NotNull
			final IElementSerializer elementSerializer
	) throws IOException {
		if (element == null) {
			return null;
		}

		byte[] serializedKey;
		byte[] serializedValue;

		if (element instanceof ICacheElementSerialized) {
			serializedKey = ((ICacheElementSerialized) element).getSerializedKey();
			serializedValue = ((ICacheElementSerialized) element).getSerializedValue();
		} else {
			try {
				serializedKey = elementSerializer.serialize(element.getKey());
				serializedValue = elementSerializer.serialize(element.getVal());
			} catch (IOException e) {
				log.error("Problem serializing object.", e);
				throw e;
			}
		}

		return new CacheElementSerialized(serializedKey, serializedValue);
	}

	public static <K, V> ICacheElement<K, V> deSerializeCacheElement(
			String cacheName,
			ICacheElementSerialized cacheElementSerialized
	) throws IOException, ClassNotFoundException {
		return deSerializeCacheElement(cacheName, cacheElementSerialized, StandardSerializer.getInstance());
	}

	public static <K, V> ICacheElement<K, V> deSerializeCacheElement(
			String cacheName,
			ICacheElementSerialized cacheElementSerialized,
			@NotNull final IElementSerializer elementSerializer
	) throws IOException, ClassNotFoundException {
		if (cacheElementSerialized == null) {
			return null;
		}

		K key;
		V val;

		try {
			key = elementSerializer.deSerialize(cacheElementSerialized.getSerializedKey(), null);
			val = elementSerializer.deSerialize(cacheElementSerialized.getSerializedValue(), null);
		} catch (IOException | ClassNotFoundException e) {
			log.error("Problem de-serializing object.", e);
			throw e;
		}

		return new CacheElement<>(cacheName, key, val);
	}

	public static <T> byte[] serializeSingleField(T t) throws IOException {
		return serializeSingleField(t, StandardSerializer.getInstance());
	}

	public static <T> byte[] serializeSingleField(T t, IElementSerializer elementSerializer) throws IOException {
		return elementSerializer.serialize(t);
	}

	public static <T> T deSerializeSingleField(byte[] t) throws IOException, ClassNotFoundException {
		return deSerializeSingleField(t, StandardSerializer.getInstance());
	}

	public static <T> T deSerializeSingleField(byte[] t, IElementSerializer elementSerializer) throws IOException, ClassNotFoundException {
		return elementSerializer.deSerialize(t, null);
	}
}
