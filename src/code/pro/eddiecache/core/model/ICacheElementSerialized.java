package pro.eddiecache.core.model;

/**
 * serialized cache element
 */
public interface ICacheElementSerialized {
	byte[] getSerializedKey();
	byte[] getSerializedValue();
}
