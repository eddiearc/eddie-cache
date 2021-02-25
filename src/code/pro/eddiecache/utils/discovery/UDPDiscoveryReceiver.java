package pro.eddiecache.utils.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheInfo;
import pro.eddiecache.core.model.IShutdownObserver;
import pro.eddiecache.io.IOClassLoaderWarpper;
import pro.eddiecache.utils.threadpool.CacheKitThreadFactory;

public class UDPDiscoveryReceiver implements Runnable, IShutdownObserver
{
	private static final Log log = LogFactory.getLog(UDPDiscoveryReceiver.class);

	private final byte[] buffer = new byte[65536];

	private MulticastSocket socket;

	private static final int MAX_POOL_SIZE = 2;

	private ThreadPoolExecutor pooledExecutor = null;

	private int cnt = 0;

	private UDPDiscoveryService service = null;

	private String multicastAddressString = "";

	private int multicastPort = 0;

	private boolean shutdown = false;

	public UDPDiscoveryReceiver(UDPDiscoveryService service, String multicastAddressString, int multicastPort)
			throws IOException
	{
		this.service = service;
		this.multicastAddressString = multicastAddressString;
		this.multicastPort = multicastPort;

		pooledExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_POOL_SIZE,
				new CacheKitThreadFactory("CacheKit-UDPDiscoveryReceiver-", Thread.MIN_PRIORITY));
		pooledExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());

		if (log.isInfoEnabled())
		{
			log.info("Construct listener, [" + this.multicastAddressString + ":" + this.multicastPort + "]");
		}

		try
		{
			createSocket(this.multicastAddressString, this.multicastPort);
		}
		catch (IOException ioe)
		{
			throw ioe;
		}
	}

	private void createSocket(String multicastAddressString, int multicastPort) throws IOException
	{
		try
		{
			socket = new MulticastSocket(multicastPort);
			if (log.isInfoEnabled())
			{
				log.info("Join group: [" + InetAddress.getByName(multicastAddressString) + "]");
			}
			socket.joinGroup(InetAddress.getByName(multicastAddressString));
		}
		catch (IOException e)
		{
			log.error("Could not bind to multicast address [" + InetAddress.getByName(multicastAddressString) + ":"
					+ multicastPort + "]", e);
			throw e;
		}
	}

	/**
	 * 通过receive()方法阻塞等待
	 *
	 * @return UDP组播中获取的信息
	 */
	public Object waitForMessage() throws IOException
	{
		final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		ObjectInputStream objectStream = null;
		Object obj = null;
		try
		{
			if (log.isDebugEnabled())
			{
				log.debug("Wait for message.");
			}

			socket.receive(packet);

			if (log.isDebugEnabled())
			{
				log.debug("Receive packet from address [" + packet.getSocketAddress() + "]");
			}

			final ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer, 0, packet.getLength());
			objectStream = new IOClassLoaderWarpper(byteStream, null);
			obj = objectStream.readObject();

			if (obj instanceof UDPDiscoveryMessage)
			{
				UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
				msg.setHost(packet.getAddress().getHostAddress());

				if (log.isDebugEnabled())
				{
					log.debug("Read object from address [" + packet.getSocketAddress() + "], object=[" + obj + "]");
				}
			}
		}
		catch (Exception e)
		{
			log.error("Error in receiving multicast packet", e);
		}
		finally
		{
			if (objectStream != null)
			{
				try
				{
					objectStream.close();
				}
				catch (IOException e)
				{
					log.error("Error in closing object stream", e);
				}
			}
		}
		return obj;
	}

	@Override
	public void run()
	{
		try
		{
			while (!shutdown)
			{
				Object obj = waitForMessage();

				cnt++;

				if (log.isDebugEnabled())
				{
					log.debug(getCnt() + " messages received.");
				}

				UDPDiscoveryMessage message = null;

				try
				{
					message = (UDPDiscoveryMessage) obj;
					if (message != null)
					{
						MessageHandler handler = new MessageHandler(message);

						pooledExecutor.execute(handler);

						if (log.isDebugEnabled())
						{
							log.debug("Passed handler to executor.");
						}
					}
					else
					{
						log.warn("Message is null");
					}
				}
				catch (ClassCastException cce)
				{
					log.warn("Receive unknown message type " + cce.getMessage());
				}
			}
		}
		catch (Exception e)
		{
			log.error("Unexpected exception in UDP receiver.", e);
			try
			{
				Thread.sleep(100);
			}
			catch (Exception e2)
			{
				log.error("Exception in sleeping", e2);
			}
		}
	}

	public void setCnt(int cnt)
	{
		this.cnt = cnt;
	}

	public int getCnt()
	{
		return cnt;
	}

	/**
	 * 注册发现的UDP组播传输信息的相关执行器
	 */
	public class MessageHandler implements Runnable
	{
		private UDPDiscoveryMessage message = null;

		public MessageHandler(UDPDiscoveryMessage message)
		{
			this.message = message;
		}

		@Override
		public void run()
		{

			if (message.getRequesterId() == CacheInfo.listenerId)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Ignore message sent from self");
				}
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("Process message sent from another");
					log.debug("Message = " + message);
				}

				if (message.getHost() == null || message.getCacheNames() == null || message.getCacheNames().isEmpty())
				{
					if (log.isDebugEnabled())
					{
						log.debug("Ignore invalid message: " + message);
					}
				}
				else
				{
					processMessage();
				}
			}
		}

		/**
		 * 处理UDP组播传递的信息
		 */
		private void processMessage()
		{
			DiscoveredService discoveredService = new DiscoveredService();
			discoveredService.setServiceAddress(message.getHost());
			discoveredService.setCacheNames(message.getCacheNames());
			discoveredService.setServicePort(message.getPort());
			discoveredService.setLastHearTime(System.currentTimeMillis());

			if (message.getMessageType() == UDPDiscoveryMessage.BroadcastType.REQUEST)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Message is a Request Broadcast, will have the service handle it.");
				}
				service.serviceRequestBroadcast();
			}
			else if (message.getMessageType() == UDPDiscoveryMessage.BroadcastType.REMOVE)
			{
				if (log.isInfoEnabled())
				{
					log.info("Remove service from set " + discoveredService);
				}
				service.removeDiscoveredService(discoveredService);
			}
			else if (message.getMessageType() == UDPDiscoveryMessage.BroadcastType.PASSIVE)
			{
				service.addOrUpdateService(discoveredService);
			}
		}
	}

	@Override
	public void shutdown()
	{
		try
		{
			shutdown = true;
			socket.leaveGroup(InetAddress.getByName(multicastAddressString));
			socket.close();
			pooledExecutor.shutdownNow();
		}
		catch (IOException e)
		{
			log.error("Problem occur in closing socket");
		}
	}
}
