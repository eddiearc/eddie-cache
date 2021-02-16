package pro.eddiecache.utils.serialization;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.io.IOClassLoaderWarpper;
import pro.eddiecache.utils.zip.CompressionUtil;

public class CompressingSerializer implements IElementSerializer
{
	@Override
	public <T> byte[] serialize(T obj) throws IOException
	{
		byte[] uncompressed = serializeObject(obj);
		byte[] compressed = CompressionUtil.compressByteArray(uncompressed);
		return compressed;
	}

	protected <T> byte[] serializeObject(T obj) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		try
		{
			oos.writeObject(obj);
		}
		finally
		{
			oos.close();
		}
		byte[] uncompressed = baos.toByteArray();
		return uncompressed;
	}

	@Override
	public <T> T deSerialize(byte[] data, ClassLoader loader) throws IOException, ClassNotFoundException
	{
		if (data == null)
		{
			return null;
		}
		byte[] decompressedByteArray = CompressionUtil.decompressByteArray(data);
		return deserializeObject(decompressedByteArray);
	}

	protected <T> T deserializeObject(byte[] decompressedByteArray) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(decompressedByteArray);
		BufferedInputStream bis = new BufferedInputStream(bais);
		ObjectInputStream ois = new IOClassLoaderWarpper(bis, null);

		try
		{
			@SuppressWarnings("unchecked")
			T readObject = (T) ois.readObject();
			return readObject;
		}
		finally
		{
			ois.close();
		}
	}
}
