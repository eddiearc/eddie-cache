package pro.eddiecache.core.model;

import java.io.IOException;

public interface IElementSerializer
{
	<T> byte[] serialize(T obj) throws IOException;

	<T> T deSerialize(byte[] bytes, ClassLoader loader) throws IOException, ClassNotFoundException;
}
