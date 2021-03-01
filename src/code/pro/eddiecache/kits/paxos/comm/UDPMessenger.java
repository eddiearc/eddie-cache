package pro.eddiecache.kits.paxos.comm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pro.eddiecache.kits.paxos.PaxosUtils;

/**
 * @author eddie
 */
public class UDPMessenger implements CommLayer
{
	public static final int BUFFER_SIZE = 128 * 1024;

	/**
	 * 心跳周期
	 */
	public static final int UPDATE_PERIOD = 100;

	/**
	 * 用于通信使用的socket
	 */
	private final DatagramSocket socket;

	/**
	 * 用于接收的Packet
	 */
	private final DatagramPacket receivePacket;

	/**
	 * 用于接收信息的线程
	 */
	private final ReceivingThread receivingThread;

	/**
	 * 用于发送心跳的（Leader专属）
	 */
	private final TickingThread tickingThread;

	/**
	 * 分发使用的线程
	 */
	private final DispatchingThread dispatchThread;

	/**
	 * 监听并处理消息
	 */
	private MessageListener listener;

	/**
	 * 运行标识
	 */
	private boolean running = true;

	/**
	 * 用于存储信息
	 */
	private BlockingQueue<byte[]> msgQueue = new LinkedBlockingQueue<byte[]>();

	public UDPMessenger() throws SocketException, UnknownHostException
	{
		this(2440);
	}

	public UDPMessenger(int port) throws SocketException
	{
		socket = new DatagramSocket(port);
		socket.setReuseAddress(true);
		receivePacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE, socket.getLocalAddress(), port);
		this.receivingThread = new ReceivingThread();
		this.tickingThread = new TickingThread();
		this.dispatchThread = new DispatchingThread();
		this.receivingThread.start();
		this.tickingThread.start();
		this.dispatchThread.start();
	}

	@Override
	public void setListener(MessageListener listener)
	{
		this.listener = listener;
	}

	/**
	 * 向多个成员发送信息
	 *
	 * @param members 成员列表
	 * @param message 信息
	 */
	@Override
	public void sendTo(List<Member> members, byte[] message)
	{
		DatagramPacket packet = new DatagramPacket(message, message.length);
		for (Member member : members)
		{
			try
			{
				packet.setAddress(member.getAddress());
				packet.setPort(member.getPort());
				synchronized (this)
				{
					socket.send(packet);
				}
			}
			catch (IOException e)
			{
				if (running)
				{
					e.printStackTrace();

				}

			}
		}
	}

	/**
	 * 向一个成员发送信息
	 *
	 * @param member 成员信息
	 * @param message 相关信息
	 */
	@Override
	public void sendTo(Member member, byte[] message)
	{
		DatagramPacket packet = new DatagramPacket(message, message.length);
		packet.setAddress(member.getAddress());
		packet.setPort(member.getPort());

		try
		{
			synchronized (this)
			{
				socket.send(packet);
			}
		}
		catch (IOException e)
		{
			if (running)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close()
	{
		this.running = false;
		this.socket.close();
		this.dispatchThread.interrupt();
	}

	/////////////// 接收信息线程 ///////////////////

	private class ReceivingThread extends Thread
	{
		@Override
		public void run()
		{
			while (running)
			{
				try
				{
					socket.receive(receivePacket);

					if (receivePacket.getLength() > BUFFER_SIZE)
					{
						throw new IOException("Message too big " + receivePacket.getLength());
					}
					msgQueue.put(receivePacket.getData().clone());
				}
				catch (IOException | InterruptedException e)
				{
					if (running)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 调度线程，用于调度消息队列中的消息
	 */
	private class DispatchingThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while (running)
				{
					byte[] msg = msgQueue.take();
					if (running)
					{
						dispatch(msg);
					}
				}
			}
			catch (InterruptedException e)
			{
				if (running)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 发送心跳的线程，用于维持Leader地位
	 */
	private class TickingThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while (running)
				{
					dispatch(PaxosUtils.serialize(new Tick(System.currentTimeMillis())));
					sleep(UPDATE_PERIOD);
				}
			}
			catch (Exception e)
			{
				if (running)
				{
					e.printStackTrace();
				}
			}
		}
	};

	private synchronized void dispatch(byte[] msg)
	{
		if (listener != null)
		{
			listener.receive(msg);
		}
	}

}
