package pro.eddiecache.utils.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class CompressionUtil
{
	private static final Log log = LogFactory.getLog(CompressionUtil.class);

	private CompressionUtil()
	{

	}

	public static byte[] decompressByteArray(final byte[] input)
	{
		return decompressByteArray(input, 1024);
	}

	public static byte[] decompressByteArray(final byte[] input, final int bufferLength)
	{
		if (null == input)
		{
			throw new IllegalArgumentException("Input was null");
		}

		final Inflater decompressor = new Inflater();

		decompressor.setInput(input);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);

		final byte[] buf = new byte[bufferLength];

		try
		{
			while (!decompressor.finished())
			{
				int count = decompressor.inflate(buf);
				baos.write(buf, 0, count);
			}
		}
		catch (DataFormatException ex)
		{
			log.error("Problem decompressing.", ex);
		}

		decompressor.end();

		try
		{
			baos.close();
		}
		catch (IOException ex)
		{
			log.error("Problem closing stream.", ex);
		}

		return baos.toByteArray();
	}

	public static byte[] compressByteArray(byte[] input) throws IOException
	{
		return compressByteArray(input, 1024);
	}

	public static byte[] compressByteArray(byte[] input, int bufferLength) throws IOException
	{

		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);

		compressor.setInput(input);
		compressor.finish();

		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

		byte[] buf = new byte[bufferLength];
		while (!compressor.finished())
		{
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}

		compressor.end();
		bos.close();

		return bos.toByteArray();

	}

	public static byte[] decompressGzipByteArray(byte[] compressedByteArray) throws IOException
	{
		return decompressGzipByteArray(compressedByteArray, 1024);
	}

	public static byte[] decompressGzipByteArray(byte[] compressedByteArray, int bufferlength) throws IOException
	{
		ByteArrayOutputStream uncompressedStream = new ByteArrayOutputStream();

		GZIPInputStream compressedStream = new GZIPInputStream(new ByteArrayInputStream(compressedByteArray));

		byte[] buffer = new byte[bufferlength];

		int index = -1;

		while ((index = compressedStream.read(buffer)) != -1)
		{
			uncompressedStream.write(buffer, 0, index);
		}

		return uncompressedStream.toByteArray();
	}
}
