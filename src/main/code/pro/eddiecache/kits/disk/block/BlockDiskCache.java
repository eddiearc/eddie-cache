package pro.eddiecache.kits.disk.block;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheConstants;
import pro.eddiecache.core.control.group.GroupAttrName;
import pro.eddiecache.core.control.group.GroupId;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.core.model.IRequireScheduler;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;
import pro.eddiecache.kits.KitCacheAttributes;
import pro.eddiecache.kits.disk.AbstractDiskCache;

public class BlockDiskCache<K, V> extends AbstractDiskCache<K, V> implements IRequireScheduler
{
	private static final Log log = LogFactory.getLog(BlockDiskCache.class);

	private final String cacheLogger;

	private final String fileName;

	private BlockDisk dataFile;

	private final BlockDiskCacheAttributes blockDiskCacheAttributes;

	private final File rootDirectory;

	private BlockDiskKeyStore<K> keyStore;

	private final ReentrantReadWriteLock storageLock = new ReentrantReadWriteLock();

	private ScheduledFuture<?> future;

	public BlockDiskCache(BlockDiskCacheAttributes cacheAttributes)
	{
		this(cacheAttributes, null);
	}

	public BlockDiskCache(BlockDiskCacheAttributes cacheAttributes, IElementSerializer elementSerializer)
	{
		super(cacheAttributes);
		setElementSerializer(elementSerializer);

		this.blockDiskCacheAttributes = cacheAttributes;
		this.cacheLogger = "CacheName [" + getCacheName() + "] ";

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "construct BlockDiskCache with attributes " + cacheAttributes);
		}

		this.fileName = getCacheName().replaceAll("[^a-zA-Z0-9-_\\.]", "_");
		this.rootDirectory = cacheAttributes.getDiskPath();

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "cache file root directory: [" + rootDirectory + "]");
		}

		try
		{
			if (this.blockDiskCacheAttributes.getBlockSizeBytes() > 0)
			{
				this.dataFile = new BlockDisk(new File(rootDirectory, fileName + ".data"),
						this.blockDiskCacheAttributes.getBlockSizeBytes(), getElementSerializer());
			}
			else
			{
				this.dataFile = new BlockDisk(new File(rootDirectory, fileName + ".data"), getElementSerializer());
			}

			keyStore = new BlockDiskKeyStore<K>(this.blockDiskCacheAttributes, this);

			boolean alright = verifyDisk();

			if (keyStore.size() == 0 || !alright)
			{
				this.reset();
			}

			setAlive(true);
			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "Block Disk Cache is alive.");
			}
		}
		catch (IOException e)
		{
			log.error(cacheLogger + "fail to initialize for fileName: " + fileName + " and root directory: "
					+ rootDirectory, e);
		}
	}

	@Override
	public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutor)
	{
		if (this.blockDiskCacheAttributes.getKeyPersistenceIntervalSeconds() > 0)
		{
			future = scheduledExecutor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run()
				{
					keyStore.saveKeys();
				}
			}, this.blockDiskCacheAttributes.getKeyPersistenceIntervalSeconds(),
					this.blockDiskCacheAttributes.getKeyPersistenceIntervalSeconds(), TimeUnit.SECONDS);
		}
	}

	protected boolean verifyDisk()
	{
		boolean allRight = false;

		storageLock.readLock().lock();

		try
		{
			int maxToTest = 100;
			int count = 0;
			Iterator<Map.Entry<K, int[]>> it = this.keyStore.entrySet().iterator();
			while (it.hasNext() && count < maxToTest)
			{
				count++;
				Map.Entry<K, int[]> entry = it.next();
				Object data = this.dataFile.read(entry.getValue());
				if (data == null)
				{
					throw new Exception(cacheLogger + "couldn't find data for key [" + entry.getKey() + "]");
				}
			}
			allRight = true;
		}
		catch (Exception e)
		{
			log.warn(cacheLogger + "error occur in verifying disk.  Message [" + e.getMessage() + "]");
			allRight = false;
		}
		finally
		{
			storageLock.readLock().unlock();
		}

		return allRight;
	}

	@Override
	public Set<K> getKeySet() throws IOException
	{
		HashSet<K> keys = new HashSet<K>();

		storageLock.readLock().lock();

		try
		{
			keys.addAll(this.keyStore.keySet());
		}
		finally
		{
			storageLock.readLock().unlock();
		}

		return keys;
	}

	@Override
	public Map<K, ICacheElement<K, V>> processGetMatching(String pattern)
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		Set<K> keyArray = null;
		storageLock.readLock().lock();
		try
		{
			keyArray = new HashSet<K>(keyStore.keySet());
		}
		finally
		{
			storageLock.readLock().unlock();
		}

		Set<K> matchingKeys = getKeyMatcher().getMatchingKeysFromArray(pattern, keyArray);

		for (K key : matchingKeys)
		{
			ICacheElement<K, V> element = processGet(key);
			if (element != null)
			{
				elements.put(key, element);
			}
		}

		return elements;
	}

	@Override
	public int getSize()
	{
		return this.keyStore.size();
	}

	@Override
	protected ICacheElement<K, V> processGet(K key)
	{
		if (!isAlive())
		{
			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "no alive so returning null for key = " + key);
			}
			return null;
		}

		if (log.isDebugEnabled())
		{
			log.debug(cacheLogger + "try to get from disk: " + key);
		}

		ICacheElement<K, V> object = null;

		try
		{
			storageLock.readLock().lock();
			try
			{
				int[] ded = this.keyStore.get(key);
				if (ded != null)
				{
					object = this.dataFile.read(ded);
				}
			}
			finally
			{
				storageLock.readLock().unlock();
			}

		}
		catch (IOException ioe)
		{
			log.error(cacheLogger + "fail to get from disk--IOException, key = " + key, ioe);
			reset();
		}
		catch (Exception e)
		{
			log.error(cacheLogger + "fail to get from disk, key = " + key, e);
		}
		return object;
	}

	@Override
	protected void processUpdate(ICacheElement<K, V> element)
	{
		if (!isAlive())
		{
			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "no alive, aborting put of key = " + element.getKey());
			}
			return;
		}

		int[] old = null;

		storageLock.writeLock().lock();

		try
		{
			old = this.keyStore.get(element.getKey());

			if (old != null)
			{
				this.dataFile.freeBlocks(old);
			}

			int[] blocks = this.dataFile.write(element);

			this.keyStore.put(element.getKey(), blocks);

			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "put to file [" + fileName + "] key [" + element.getKey() + "]");
			}
		}
		catch (IOException e)
		{
			log.error(
					cacheLogger + "fail to update element, key: " + element.getKey() + " old: " + Arrays.toString(old),
					e);
		}
		finally
		{
			storageLock.writeLock().unlock();
		}

		if (log.isDebugEnabled())
		{
			log.debug(cacheLogger + "store element on disk, key: " + element.getKey());
		}
	}

	@Override
	protected boolean processRemove(K key)
	{
		if (!isAlive())
		{
			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "no alive so returning false for key = " + key);
			}
			return false;
		}

		boolean reset = false;
		boolean removed = false;

		storageLock.writeLock().lock();

		try
		{
			if (key instanceof String && key.toString().endsWith(CacheConstants.NAME_COMPONENT_DELIMITER))
			{
				removed = performPartialKeyRemoval((String) key);
			}
			else if (key instanceof GroupAttrName && ((GroupAttrName<?>) key).attrName == null)
			{
				removed = performGroupRemoval(((GroupAttrName<?>) key).groupId);
			}
			else
			{
				removed = performSingleKeyRemoval(key);
			}
		}
		catch (Exception e)
		{
			log.error(cacheLogger + "error occur in removing element.", e);
			reset = true;
		}
		finally
		{
			storageLock.writeLock().unlock();
		}

		if (reset)
		{
			reset();
		}

		return removed;
	}

	private boolean performGroupRemoval(GroupId key)
	{
		boolean removed = false;

		List<K> itemsToRemove = new LinkedList<K>();

		for (K k : keyStore.keySet())
		{
			if (k instanceof GroupAttrName && ((GroupAttrName<?>) k).groupId.equals(key))
			{
				itemsToRemove.add(k);
			}
		}

		for (K fullKey : itemsToRemove)
		{
			performSingleKeyRemoval(fullKey);
			removed = true;
		}

		return removed;
	}

	private boolean performPartialKeyRemoval(String key)
	{
		boolean removed = false;

		List<K> itemsToRemove = new LinkedList<K>();

		for (K k : keyStore.keySet())
		{
			if (k instanceof String && k.toString().startsWith(key))
			{
				itemsToRemove.add(k);
			}
		}

		for (K fullKey : itemsToRemove)
		{
			performSingleKeyRemoval(fullKey);
			removed = true;
		}

		return removed;
	}

	private boolean performSingleKeyRemoval(K key)
	{
		boolean removed;
		int[] ded = this.keyStore.remove(key);
		removed = ded != null;
		if (removed)
		{
			this.dataFile.freeBlocks(ded);
		}

		if (log.isDebugEnabled())
		{
			log.debug(cacheLogger + "disk removal: Removed from key hash, key [" + key + "] removed = " + removed);
		}
		return removed;
	}

	@Override
	protected void processRemoveAll()
	{
		reset();
	}

	@Override
	public void processDispose()
	{
		Runnable runner = new Runnable() {
			@Override
			public void run()
			{
				try
				{
					disposeInternal();
				}
				catch (InterruptedException e)
				{
					log.warn("InterruptedException occur in diposing.");
				}
			}
		};
		Thread thread = new Thread(runner, "BlockDiskCache-DisposalThread");
		thread.start();
		try
		{
			thread.join(60 * 1000);
		}
		catch (InterruptedException ex)
		{
			log.error(cacheLogger + "Interrupted while waiting for disposal thread to finish.", ex);
		}
	}

	protected void disposeInternal() throws InterruptedException
	{
		if (!isAlive())
		{
			log.error(cacheLogger + "no alive and dispose was called, filename: " + fileName);
			return;
		}
		storageLock.writeLock().lock();
		try
		{
			setAlive(false);
			this.keyStore.saveKeys();

			if (future != null)
			{
				future.cancel(true);
			}

			try
			{
				if (log.isDebugEnabled())
				{
					log.debug(cacheLogger + "close files, base filename: " + fileName);
				}
				dataFile.close();
				// dataFile = null;

				// TOD make a close
				// keyFile.close();
				// keyFile = null;
			}
			catch (IOException e)
			{
				log.error(cacheLogger + "fail to close files in dispose, filename: " + fileName, e);
			}
		}
		finally
		{
			storageLock.writeLock().unlock();
		}

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "shutdown complete.");
		}
	}

	@Override
	public KitCacheAttributes getKitCacheAttributes()
	{
		return this.blockDiskCacheAttributes;
	}

	private void reset()
	{
		if (log.isWarnEnabled())
		{
			log.warn(cacheLogger + "reset cache");
		}

		try
		{
			storageLock.writeLock().lock();

			this.keyStore.reset();

			if (dataFile != null)
			{
				dataFile.reset();
			}
		}
		catch (IOException e)
		{
			log.error(cacheLogger + "fail to reset state", e);
		}
		finally
		{
			storageLock.writeLock().unlock();
		}
	}

	protected void freeBlocks(int[] blocksToFree)
	{
		this.dataFile.freeBlocks(blocksToFree);
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("Block Disk Cache");

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();

		elems.add(new StatElement<Boolean>("Is Alive", Boolean.valueOf(isAlive())));
		elems.add(new StatElement<Integer>("Key Map Size", Integer.valueOf(this.keyStore.size())));

		if (this.dataFile != null)
		{
			try
			{
				elems.add(new StatElement<Long>("Data File Length", Long.valueOf(this.dataFile.length())));
			}
			catch (IOException e)
			{
				log.error(e);
			}

			elems.add(new StatElement<Integer>("Block Size Bytes", Integer.valueOf(this.dataFile.getBlockSizeBytes())));
			elems.add(new StatElement<Integer>("Number Of Blocks", Integer.valueOf(this.dataFile.getNumberOfBlocks())));
			elems.add(new StatElement<Long>("Average Put Size Bytes",
					Long.valueOf(this.dataFile.getAveragePutSizeBytes())));
			elems.add(new StatElement<Integer>("Empty Blocks", Integer.valueOf(this.dataFile.getEmptyBlocks())));
		}

		IStats sStats = super.getStatistics();
		elems.addAll(sStats.getStatElements());

		stats.setStatElements(elems);

		return stats;
	}

	@Override
	protected String getDiskLocation()
	{
		return dataFile.getFilePath();
	}
}
