package pro.eddiecache.core;

import java.util.Arrays;

import pro.eddiecache.core.model.ICacheElementSerialized;

public class CacheElementSerialized implements ICacheElementSerialized {

	private static final long serialVersionUID = 1L;

	private final byte[] serializedKey;
	private final byte[] serializedValue;

	public CacheElementSerialized(byte[] serializedKey, byte[] serializedValue) {
		this.serializedKey = serializedKey;
		this.serializedValue = serializedValue;
	}

	@Override
	public byte[] getSerializedKey() {
		return this.serializedKey;
	}

	@Override
	public byte[] getSerializedValue() {
		return this.serializedValue;
	}

	@Override
	public String toString() {
		return "CacheElementSerialized{" +
				"serializedKey=" + Arrays.toString(serializedKey) +
				", serializedValue=" + Arrays.toString(serializedValue) +
				'}';
	}
}
