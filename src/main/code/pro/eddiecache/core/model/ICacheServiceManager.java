package pro.eddiecache.core.model;

import java.io.IOException;

public interface ICacheServiceManager
{

	String getStats() throws IOException;

	void shutdown() throws IOException;

	void shutdown(String host, int port) throws IOException;
}
