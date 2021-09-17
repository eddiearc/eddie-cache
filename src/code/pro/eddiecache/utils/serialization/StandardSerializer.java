package pro.eddiecache.utils.serialization;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.io.IOClassLoaderWrapper;

public class StandardSerializer implements IElementSerializer
{
	@Override
	public <T> byte[] serialize(T obj) throws IOException
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
		return baos.toByteArray();
	}

	@Override
	public <T> T deSerialize(byte[] data, ClassLoader loader) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		BufferedInputStream bis = new BufferedInputStream(bais);
		ObjectInputStream ois = new IOClassLoaderWrapper(bis, loader);
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
