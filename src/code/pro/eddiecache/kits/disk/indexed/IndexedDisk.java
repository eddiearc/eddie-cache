package pro.eddiecache.kits.disk.indexed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.model.IElementSerializer;

class IndexedDisk
{
	public static final byte HEADER_SIZE_BYTES = 4;

	private final IElementSerializer elementSerializer;

	private static final Log log = LogFactory.getLog(IndexedDisk.class);

	private final String filepath;

	private final FileChannel fc;

	public IndexedDisk(File file, IElementSerializer elementSerializer) throws FileNotFoundException
	{
		this.filepath = file.getAbsolutePath();
		this.elementSerializer = elementSerializer;
		RandomAccessFile raf = new RandomAccessFile(filepath, "rw");
		this.fc = raf.getChannel();
	}

	protected <T extends Serializable> T readObject(IndexedDiskElementDescriptor ded)
			throws IOException, ClassNotFoundException
	{
		boolean corrupted = false;
		long fileLength = fc.size();
		if (ded.pos > fileLength)
		{
			corrupted = true;
		}
		else
		{
			ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE_BYTES);
			fc.read(buffer, ded.pos);
			//数据读写完成，则buffer执行flip，将指针重置为0，方便后续读写操作
			buffer.flip();
			int datalen = buffer.getInt();
			if (ded.len != datalen)
			{
				corrupted = true;
			}
			else if (ded.pos + ded.len > fileLength)
			{
				corrupted = true;
			}
		}

		if (corrupted)
		{
			throw new IOException("The file is corrupt.");
		}

		// 1. 分配一个对应大小的缓冲区； 2. 跳过索引部分，读取数据到缓冲区中； 3. 反序列号
		ByteBuffer data = ByteBuffer.allocate(ded.len);
		fc.read(data, ded.pos + HEADER_SIZE_BYTES);
		data.flip();

		return elementSerializer.deSerialize(data.array(), null);
	}

	protected void move(final IndexedDiskElementDescriptor ded, final long newPosition) throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE_BYTES);
		fc.read(buf, ded.pos);
		buf.flip();
		int length = buf.getInt();

		if (length != ded.len)
		{
			throw new IOException("Mismatch memory and disk length (" + length + ") for " + ded);
		}

		long readPos = ded.pos;
		long writePos = newPosition;

		int remaining = HEADER_SIZE_BYTES + length;
		ByteBuffer buffer = ByteBuffer.allocate(16384);

		while (remaining > 0)
		{

			int chunkSize = Math.min(remaining, buffer.capacity());
			buffer.limit(chunkSize);
			fc.read(buffer, readPos);
			buffer.flip();
			fc.write(buffer, writePos);
			buffer.clear();

			writePos += chunkSize;
			readPos += chunkSize;
			remaining -= chunkSize;
		}

		ded.pos = newPosition;
	}

	/**
	 * 根据文件的下标、长度等信息将数据写到磁盘中
	 *
	 * @param ded 下标信息
	 * @param data 写入的数据
	 */
	protected boolean write(IndexedDiskElementDescriptor ded, byte[] data) throws IOException
	{
		long pos = ded.pos;

		if (data.length != ded.len)
		{
			throw new IOException("Descriptor does not match data length");
		}

		ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE_BYTES + data.length);
		buffer.putInt(data.length);
		buffer.put(data);
		buffer.flip();
		int written = fc.write(buffer, pos);
		//fc.force(true);

		return written == data.length;
	}

	/**
	 * 将对象写入指定的偏移位置之后
	 *
	 * @param obj 写入的对象
	 * @param pos 指定的文件偏移位置
	 */
	protected boolean writeObject(Serializable obj, long pos) throws IOException
	{
		byte[] data = elementSerializer.serialize(obj);
		write(new IndexedDiskElementDescriptor(pos, data.length), data);
		return true;
	}

	protected long length() throws IOException
	{
		return fc.size();
	}

	protected void close() throws IOException
	{
		fc.close();
	}

	protected synchronized void reset() throws IOException
	{
		if (log.isDebugEnabled())
		{
			log.debug("Reset Indexed File [" + filepath + "]");
		}
		fc.truncate(0);
		fc.force(true);
	}

	//截断文件
	protected void truncate(long length) throws IOException
	{
		if (log.isInfoEnabled())
		{
			log.info("Truncate file [" + filepath + "] to " + length);
		}
		fc.truncate(length);
	}

	protected String getFilePath()
	{
		return filepath;
	}
}
