package pro.eddiecache.kits.lateral.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.io.IOClassLoaderWarpper;
import pro.eddiecache.kits.lateral.LateralElementDescriptor;

/**
 * @author eddie
 * 用于线性组建的缓存 更新/获取 信息传输
 */
public class LateralTCPSender
{
	private static final Log log = LogFactory.getLog(LateralTCPSender.class);

	private int socketOpenTimeOut;
	private int socketSoTimeOut;

	private ObjectOutputStream oos;

	private Socket socket;

	private int sendCnt = 0;

	private final Object getLock = new int[0];

	public LateralTCPSender(ITCPLateralCacheAttributes lca) throws IOException
	{
		this.socketOpenTimeOut = lca.getOpenTimeOut();
		this.socketSoTimeOut = lca.getSocketTimeOut();

		String server = lca.getTcpServer();
		if (server == null)
		{
			throw new IOException("Invalid server");
		}

		String host = server.substring(0, server.indexOf(":"));
		int port = Integer.parseInt(server.substring(server.indexOf(":") + 1));
		if (log.isDebugEnabled())
		{
			log.debug("host = " + host);
			log.debug("port = " + port);
		}

		if (host.length() == 0)
		{
			throw new IOException("Can not connect to invalid address [" + host + ":" + port + "]");
		}

		init(host, port);
	}

	protected void init(String host, int port) throws IOException
	{
		try
		{
			if (log.isInfoEnabled())
			{
				log.info("Attempt connection to [" + host + "]");
			}

			try
			{
				socket = new Socket();
				socket.connect(new InetSocketAddress(host, port), this.socketOpenTimeOut);
			}
			catch (IOException ioe)
			{
				if (socket != null)
				{
					socket.close();
				}

				throw new IOException("Can't not connect to " + host + ":" + port, ioe);
			}

			socket.setSoTimeout(socketSoTimeOut);
			synchronized (this)
			{
				oos = new ObjectOutputStream(socket.getOutputStream());
			}
		}
		catch (java.net.ConnectException e)
		{
			log.debug("Remote host [" + host + "] refused connection.");
			throw e;
		}
		catch (IOException e)
		{
			log.debug("Could not connect to [" + host + "]. exception is " + e);
			throw e;
		}
	}

	public <K, V> void send(LateralElementDescriptor<K, V> led) throws IOException
	{
		sendCnt++;
		if (log.isInfoEnabled() && sendCnt % 100 == 0)
		{
			log.info("send count (port " + socket.getPort() + ") = " + sendCnt);
		}

		if (log.isDebugEnabled())
		{
			log.debug("Sending in LateralElementDescriptor");
		}

		if (led == null)
		{
			return;
		}

		if (oos == null)
		{
			throw new IOException("No remote connection is available for LateralTCPSender.");
		}

		synchronized (this.getLock)
		{
			// writeObjectUnShared： 后置对该对象的修改都不会影响到流中的数据
			oos.writeUnshared(led);
			oos.flush();
		}
	}

	public <K, V> Object sendAndReceive(LateralElementDescriptor<K, V> led) throws IOException
	{
		if (led == null)
		{
			return null;
		}

		if (oos == null)
		{
			throw new IOException("No remote connection is available for LateralTCPSender.");
		}

		Object response = null;

		synchronized (this.getLock)
		{
			try
			{
				// 清空socket中数据流在发送数据之前
				if (socket.getInputStream().available() > 0)
				{
					socket.getInputStream().read(new byte[socket.getInputStream().available()]);
				}
			}
			catch (IOException ioe)
			{
				log.error("Problem cleaning socket before send " + socket, ioe);
				throw ioe;
			}

			oos.writeUnshared(led);
			oos.flush();
			ObjectInputStream ois = null;

			try
			{
				socket.setSoTimeout(socketSoTimeOut);
				ois = new IOClassLoaderWarpper(socket.getInputStream(), null);
				response = ois.readObject();
			}
			catch (IOException ioe)
			{
				String message = "Could not open ObjectInputStream to " + socket + " SoTimeout ["
						+ socket.getSoTimeout() + "] Connected [" + socket.isConnected() + "]";
				log.error(message, ioe);
				throw ioe;
			}
			catch (Exception e)
			{
				log.error(e);
			}
			finally
			{
				if (ois != null)
				{
					ois.close();
				}
			}
		}

		return response;
	}

	public void dispose() throws IOException
	{
		if (log.isInfoEnabled())
		{
			log.info("Dispose called");
		}
		oos.close();
		socket.close();
	}
}
