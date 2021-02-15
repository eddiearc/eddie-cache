package pro.eddiecache.kits.disk.block;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.io.IOClassLoaderWarpper;
import pro.eddiecache.kits.disk.IDiskCacheAttributes.DiskLimitType;
import pro.eddiecache.utils.struct.AbstractLRUMap;
import pro.eddiecache.utils.struct.LRUMap;
import pro.eddiecache.utils.timing.ElapsedTimer;

public class BlockDiskKeyStore<K>
{
	private static final Log log = LogFactory.getLog(BlockDiskKeyStore.class);

	private final BlockDiskCacheAttributes blockDiskCacheAttributes;

	private Map<K, int[]> keyHash;

	private final File keyFile;

	protected final String cacheLogger;

	private final String fileName;

	private final int maxKeySize;

	protected final BlockDiskCache<K, ?> blockDiskCache;

	private DiskLimitType diskLimitType = DiskLimitType.COUNT;

	private int blockSize;

	public BlockDiskKeyStore(BlockDiskCacheAttributes cacheAttributes, BlockDiskCache<K, ?> blockDiskCache)
	{
		this.blockDiskCacheAttributes = cacheAttributes;
		this.cacheLogger = "CacheName [" + this.blockDiskCacheAttributes.getCacheName() + "] ";
		this.fileName = this.blockDiskCacheAttributes.getCacheName();
		this.maxKeySize = cacheAttributes.getMaxKeySize();
		this.blockDiskCache = blockDiskCache;
		this.diskLimitType = cacheAttributes.getDiskLimitType();
		this.blockSize = cacheAttributes.getBlockSizeBytes();

		File rootDirectory = cacheAttributes.getDiskPath();

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "cache file root directory [" + rootDirectory + "]");
		}

		this.keyFile = new File(rootDirectory, fileName + ".key");

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "key File [" + this.keyFile.getAbsolutePath() + "]");
		}

		if (keyFile.length() > 0)
		{
			loadKeys();

			if (!verify())
			{
				log.warn(cacheLogger + "key File is invalid. Resetting file.");
				initKeyMap();
				reset();
			}
		}
		else
		{
			initKeyMap();
		}
	}

	protected void saveKeys()
	{
		try
		{
			ElapsedTimer timer = new ElapsedTimer();
			int numKeys = keyHash.size();
			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "save keys to [" + this.keyFile.getAbsolutePath() + "], key count [" + numKeys
						+ "]");
			}

			synchronized (keyFile)
			{
				FileOutputStream fos = new FileOutputStream(keyFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos, 65536);
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				try
				{
					if (!verify())
					{
						throw new IOException("Inconsistent key file");
					}

					for (Map.Entry<K, int[]> entry : keyHash.entrySet())
					{
						BlockDiskElementDescriptor<K> descriptor = new BlockDiskElementDescriptor<K>();
						descriptor.setKey(entry.getKey());
						descriptor.setBlocks(entry.getValue());

						oos.writeUnshared(descriptor);
					}
				}
				finally
				{
					oos.flush();
					oos.close();
				}
			}

			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "finish  saving keys. It took " + timer.getElapsedTimeString() + " to store "
						+ numKeys + " keys.  Key file length [" + keyFile.length() + "]");
			}
		}
		catch (IOException e)
		{
			log.error(cacheLogger + "problem occur in storing keys.", e);
		}
	}

	protected void reset()
	{
		synchronized (keyFile)
		{
			clearMemoryMap();
			saveKeys();
		}
	}

	protected void clearMemoryMap()
	{
		this.keyHash.clear();
	}

	private void initKeyMap()
	{
		keyHash = null;
		if (maxKeySize >= 0)
		{
			if (this.diskLimitType == DiskLimitType.SIZE)
			{
				keyHash = new LRUMapSizeLimited(maxKeySize);
			}
			else
			{
				keyHash = new LRUMapCountLimited(maxKeySize);
			}
			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "set maxKeySize to: '" + maxKeySize + "'");
			}
		}
		else
		{
			keyHash = new HashMap<K, int[]>();
			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "set maxKeySize to unlimited'");
			}
		}
	}

	protected void loadKeys()
	{
		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "load keys for " + keyFile.toString());
		}

		try
		{
			initKeyMap();

			HashMap<K, int[]> keys = new HashMap<K, int[]>();

			synchronized (keyFile)
			{
				FileInputStream fis = new FileInputStream(keyFile);
				BufferedInputStream bis = new BufferedInputStream(fis, 65536);
				ObjectInputStream ois = new IOClassLoaderWarpper(bis, null);
				try
				{
					while (true)
					{
						@SuppressWarnings("unchecked")
						BlockDiskElementDescriptor<K> descriptor = (BlockDiskElementDescriptor<K>) ois.readObject();
						if (descriptor != null)
						{
							keys.put(descriptor.getKey(), descriptor.getBlocks());
						}
					}
				}
				catch (EOFException eof)
				{

				}
				finally
				{
					ois.close();
				}
			}

			if (!keys.isEmpty())
			{
				keyHash.putAll(keys);

				if (log.isDebugEnabled())
				{
					log.debug(cacheLogger + "found " + keys.size() + " in keys file.");
				}

				if (log.isInfoEnabled())
				{
					log.info(cacheLogger + "loaded keys from [" + fileName + "], key count: " + keyHash.size()
							+ "; up to " + maxKeySize + " will be available.");
				}
			}
		}
		catch (Exception e)
		{
			log.error(cacheLogger + "Problem loading keys for file " + fileName, e);
		}
	}

	public Set<Map.Entry<K, int[]>> entrySet()
	{
		return this.keyHash.entrySet();
	}

	public Set<K> keySet()
	{
		return this.keyHash.keySet();
	}

	public int size()
	{
		return this.keyHash.size();
	}

	public int[] get(K key)
	{
		return this.keyHash.get(key);
	}

	public void put(K key, int[] value)
	{
		this.keyHash.put(key, value);
	}

	public int[] remove(K key)
	{
		return this.keyHash.remove(key);
	}

	private boolean verify()
	{
		Map<Integer, Set<K>> blockAllocationMap = new TreeMap<Integer, Set<K>>();
		for (Entry<K, int[]> e : keyHash.entrySet())
		{
			for (int block : e.getValue())
			{
				Set<K> keys = blockAllocationMap.get(block);
				if (keys == null)
				{
					keys = new HashSet<K>();
					blockAllocationMap.put(block, keys);
				}
				else if (!log.isDebugEnabled())
				{
					return false;
				}
				keys.add(e.getKey());
			}
		}
		boolean ok = true;
		if (log.isDebugEnabled())
		{
			for (Entry<Integer, Set<K>> e : blockAllocationMap.entrySet())
			{
				log.debug("Block " + e.getKey() + ":" + e.getValue());
				if (e.getValue().size() > 1)
				{
					ok = false;
				}
			}
			return ok;
		}
		else
		{
			return ok;
		}
	}

	public class LRUMapSizeLimited extends AbstractLRUMap<K, int[]>
	{

		private AtomicInteger contentSize;
		private int maxSize;

		public LRUMapSizeLimited()
		{
			this(-1);
		}

		public LRUMapSizeLimited(int maxSize)
		{
			super();
			this.maxSize = maxSize;
			this.contentSize = new AtomicInteger(0);
		}

		private void subLengthFromCacheSize(int[] value)
		{
			contentSize.addAndGet(value.length * blockSize / -1024 - 1);
		}

		private void addLengthToCacheSize(int[] value)
		{
			contentSize.addAndGet(value.length * blockSize / 1024 + 1);
		}

		@Override
		public int[] put(K key, int[] value)
		{
			int[] oldValue = null;

			try
			{
				oldValue = super.put(key, value);
			}
			finally
			{
				if (value != null)
				{
					addLengthToCacheSize(value);
				}
				if (oldValue != null)
				{
					subLengthFromCacheSize(oldValue);
				}
			}

			return oldValue;
		}

		@Override
		public int[] remove(Object key)
		{
			int[] value = null;

			try
			{
				value = super.remove(key);
				return value;
			}
			finally
			{
				if (value != null)
				{
					subLengthFromCacheSize(value);
				}
			}
		}

		@Override
		protected void processRemovedLRU(K key, int[] value)
		{
			blockDiskCache.freeBlocks(value);
			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "remove key: [" + key + "] from key store.");
				log.debug(cacheLogger + "Key store size: [" + super.size() + "].");
			}

			if (value != null)
			{
				subLengthFromCacheSize(value);
			}
		}

		@Override
		protected boolean shouldRemove()
		{
			return maxSize > 0 && contentSize.get() > maxSize && this.size() > 1;
		}
	}

	public class LRUMapCountLimited extends LRUMap<K, int[]>
	{
		public LRUMapCountLimited(int maxKeySize)
		{
			super(maxKeySize);
		}

		@Override
		protected void processRemovedLRU(K key, int[] value)
		{
			blockDiskCache.freeBlocks(value);
			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "removing key: [" + key + "] from key store.");
				log.debug(cacheLogger + "key store size: [" + super.size() + "].");
			}
		}
	}
}
