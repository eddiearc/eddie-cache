package pro.eddiecache.kits.disk.indexed;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheConstants;
import pro.eddiecache.core.control.group.GroupAttrName;
import pro.eddiecache.core.control.group.GroupId;
import pro.eddiecache.core.logger.ICacheEvent;
import pro.eddiecache.core.logger.ICacheEventWrapper;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;
import pro.eddiecache.kits.KitCacheAttributes;
import pro.eddiecache.kits.disk.AbstractDiskCache;
import pro.eddiecache.utils.struct.AbstractLRUMap;
import pro.eddiecache.utils.struct.LRUMap;
import pro.eddiecache.utils.timing.ElapsedTimer;
import pro.eddiecache.kits.disk.IDiskCacheAttributes;

public class IndexedDiskCache<K, V> extends AbstractDiskCache<K, V>
{
	private static final Log log = LogFactory.getLog(IndexedDiskCache.class);

	protected final String cacheLogger;

	private final String fileName;

	private IndexedDisk dataFile;

	private IndexedDisk keyFile;

	private Map<K, IndexedDiskElementDescriptor> keyHash;

	private final int maxKeySize;

	private File cacheFileDir;

	private boolean doRecycle = true;

	private boolean isRealTimeOptimizationEnabled = true;

	private boolean isShutdownOptimizationEnabled = true;

	private boolean isOptimizing = false;

	private int timesOptimized = 0;

	private volatile Thread currentOptimizationThread;

	private int removeCount = 0;

	private boolean queueInput = false;

	private final ConcurrentSkipListSet<IndexedDiskElementDescriptor> queuedPutList = new ConcurrentSkipListSet<IndexedDiskElementDescriptor>(
			new PositionComparator());

	// （回收站）：用于记录已经分配了，但空闲的磁盘位置
	private ConcurrentSkipListSet<IndexedDiskElementDescriptor> recycle;

	private final IndexedDiskCacheAttributes cattr;

	private int recycleCnt = 0;

	private int startupSize = 0;

	private AtomicLong bytesFree = new AtomicLong(0);

	private IDiskCacheAttributes.DiskLimitType diskLimitType = IDiskCacheAttributes.DiskLimitType.COUNT;

	private AtomicInteger hitCount = new AtomicInteger(0);

	protected ReentrantReadWriteLock storageLock = new ReentrantReadWriteLock();

	public IndexedDiskCache(IndexedDiskCacheAttributes cacheAttributes)
	{
		this(cacheAttributes, null);
	}

	public IndexedDiskCache(IndexedDiskCacheAttributes cattr, IElementSerializer elementSerializer)
	{
		super(cattr);

		setElementSerializer(elementSerializer);

		this.cattr = cattr;
		this.maxKeySize = cattr.getMaxKeySize();
		this.isRealTimeOptimizationEnabled = cattr.getOptimizeAtRemoveCount() > 0;
		this.isShutdownOptimizationEnabled = cattr.isOptimizeOnShutdown();
		this.cacheLogger = "CacheName [" + getCacheName() + "] ";
		this.diskLimitType = cattr.getDiskLimitType();
		this.fileName = getCacheName().replaceAll("[^a-zA-Z0-9-_\\.]", "_");

		try
		{
			initializeFileSystem(cattr);

			initializeKeysAndData(cattr);

			initializeRecycleBin();

			setAlive(true);

			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "Indexed Disk Cache is alive.");
			}

			if (isRealTimeOptimizationEnabled && keyHash.size() > 0)
			{
				doOptimizeRealTime();
			}
		}
		catch (IOException e)
		{
			log.error(cacheLogger + "fail to initialize for fileName: " + fileName + " and directory: "
					+ this.cacheFileDir.getAbsolutePath(), e);
		}
	}

	private void initializeFileSystem(IndexedDiskCacheAttributes cattr)
	{
		this.cacheFileDir = cattr.getDiskPath();

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "cache file root directory: " + cacheFileDir);
		}
	}

	/**
	 * 根据配置信息，从磁盘中获取索引信息与对应的数据
	 *
	 * @param cattr 配置信息
	 */
	private void initializeKeysAndData(IndexedDiskCacheAttributes cattr) throws IOException
	{
		this.dataFile = new IndexedDisk(new File(cacheFileDir, fileName + ".data"), getElementSerializer());
		this.keyFile = new IndexedDisk(new File(cacheFileDir, fileName + ".key"), getElementSerializer());

		if (cattr.isClearDiskOnStartup())
		{
			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "ClearDiskOnStartup is set to true. Ingnore any persisted data.");
			}
			initializeEmptyStore();
		}
		else if (keyFile.length() > 0)
		{
			initializeStoreFromPersistedData();
		}
		else
		{
			initializeEmptyStore();
		}
	}

	/**
	 * 从磁盘存储中恢复空的数据
	 */
	private void initializeEmptyStore() throws IOException
	{
		initializeKeyMap();

		if (dataFile.length() > 0)
		{
			dataFile.reset();
		}
	}

	/**
	 * 从磁盘存储中恢复数据
	 */
	private void initializeStoreFromPersistedData() throws IOException
	{
		loadKeys();

		if (keyHash.isEmpty())
		{
			dataFile.reset();
		}
		else
		{
			boolean isOk = checkKeyDataConsistency(false);

			if (!isOk)
			{
				keyHash.clear();
				keyFile.reset();
				dataFile.reset();

				log.warn(cacheLogger + "corruption detected.  reset data and keys files.");
			}
			else
			{
				synchronized (this)
				{
					startupSize = keyHash.size();
				}
			}
		}
	}

	/**
	 * 从磁盘中加载数据索引
	 */
	protected void loadKeys()
	{
		if (log.isDebugEnabled())
		{
			log.debug(cacheLogger + "load keys for " + keyFile.toString());
		}

		storageLock.writeLock().lock();

		try
		{
			initializeKeyMap();

			HashMap<K, IndexedDiskElementDescriptor> keys = keyFile.readObject(
					new IndexedDiskElementDescriptor(0, (int) keyFile.length() - IndexedDisk.HEADER_SIZE_BYTES));

			if (keys != null)
			{
				if (log.isDebugEnabled())
				{
					log.debug(cacheLogger + "found " + keys.size() + " in keys file.");
				}

				keyHash.putAll(keys);

				if (log.isInfoEnabled())
				{
					log.info(cacheLogger + "load keys from [" + fileName + "], key count: " + keyHash.size()
							+ "; up to " + maxKeySize + " will be available.");
				}
			}

			if (log.isDebugEnabled())
			{
				dump(false);
			}
		}
		catch (Exception e)
		{
			log.error(cacheLogger + "error occur in loading keys for file " + fileName, e);
		}
		finally
		{
			storageLock.writeLock().unlock();
		}
	}

	private boolean checkKeyDataConsistency(boolean checkForDedOverlaps)
	{
		ElapsedTimer timer = new ElapsedTimer();

		boolean isOk = true;
		long fileLength = 0;
		try
		{
			fileLength = dataFile.length();

			for (Map.Entry<K, IndexedDiskElementDescriptor> e : keyHash.entrySet())
			{
				IndexedDiskElementDescriptor ded = e.getValue();

				isOk = ded.pos + IndexedDisk.HEADER_SIZE_BYTES + ded.len <= fileLength;

				if (!isOk)
				{
					log.warn(cacheLogger + "the dataFile is corrupted!" + "\n dataFile.length() = " + fileLength
							+ "\n ded.pos = " + ded.pos);
					break;
				}
			}

			if (isOk && checkForDedOverlaps)
			{
				IndexedDiskElementDescriptor[] deds = createPositionSortedDescriptorList();
				isOk = checkForDedOverlaps(deds);
			}
		}
		catch (IOException e)
		{
			log.error(e);
			isOk = false;
		}

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "finish inital consistency check, isOk = " + isOk + " in "
					+ timer.getElapsedTimeString());
		}

		return isOk;
	}

	protected boolean checkForDedOverlaps(IndexedDiskElementDescriptor[] sortedDescriptors)
	{
		long start = System.currentTimeMillis();
		boolean isOk = true;
		long expectedNextPos = 0;
		for (int i = 0; i < sortedDescriptors.length; i++)
		{
			IndexedDiskElementDescriptor ded = sortedDescriptors[i];
			if (expectedNextPos > ded.pos)
			{
				log.error(cacheLogger + "corrupt file: overlapping deds " + ded);
				isOk = false;
				break;
			}
			else
			{
				expectedNextPos = ded.pos + IndexedDisk.HEADER_SIZE_BYTES + ded.len;
			}
		}
		long end = System.currentTimeMillis();
		if (log.isDebugEnabled())
		{
			log.debug(cacheLogger + "check for DiskElementDescriptor overlaps took " + (end - start) + " ms.");
		}

		return isOk;
	}

	/**
	 * 持久化key
	 */
	protected void saveKeys()
	{
		try
		{
			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "save keys to: " + fileName + ", key count: " + keyHash.size());
			}

			keyFile.reset();

			HashMap<K, IndexedDiskElementDescriptor> keys = new HashMap<K, IndexedDiskElementDescriptor>(keyHash);

			if (keys.size() > 0)
			{
				keyFile.writeObject(keys, 0);
			}

			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "finish saving keys.");
			}
		}
		catch (IOException e)
		{
			log.error(cacheLogger + "error occur in storing keys.", e);
		}
	}

	@Override
	protected void processUpdate(ICacheElement<K, V> ce)
	{
		if (!isAlive())
		{
			log.error(cacheLogger + "no alive, abort to put of key = " + ce.getKey());
			return;
		}

		if (log.isDebugEnabled())
		{
			log.debug(cacheLogger + "store element on disk, key: " + ce.getKey());
		}

		IndexedDiskElementDescriptor ded = null;

		IndexedDiskElementDescriptor old = null;

		try
		{
			byte[] data = getElementSerializer().serialize(ce);

			storageLock.writeLock().lock();
			try
			{
				old = keyHash.get(ce.getKey());

				// 如果已经存在相同Key的缓存，并且新的缓存的大小「不大于」旧的缓存大小，则...
				if (old != null && data.length <= old.len)
				{
					ded = old;
					ded.len = data.length;
				}
				else
				{
					// 默认从文件的最末尾处开始放置数据
					ded = new IndexedDiskElementDescriptor(dataFile.length(), data.length);

					// 检查是否开启回收位置循环使用
					if (doRecycle)
					{
						// 从回收站中获取获取下一个对象的位置（长度大于等于该位置的，长度相等时 -> 位置靠前的）
						IndexedDiskElementDescriptor rep = recycle.ceiling(ded);
						if (rep != null)
						{
							recycle.remove(rep);
							ded = rep;
							ded.len = data.length;
							recycleCnt++;
							this.adjustBytesFree(ded, false);
							if (log.isDebugEnabled())
							{
								log.debug(cacheLogger + "use recycled ded " + ded.pos + " rep.len = " + rep.len
										+ " ded.len = " + ded.len);
							}
						}
					}

					keyHash.put(ce.getKey(), ded);

					// 当正在优化存储空间的时候，queueInput值为true
					if (queueInput)
					{
						queuedPutList.add(ded);
						if (log.isDebugEnabled())
						{
							log.debug(cacheLogger + "add to queued put list." + queuedPutList.size());
						}
					}

					// 存在相同Key的旧缓存，但放不下新的缓存内容，将该缓存位置加入回收站
					if (old != null)
					{
						addToRecycleBin(old);
					}
				}

				dataFile.write(ded, data);
			}
			finally
			{
				storageLock.writeLock().unlock();
			}

			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "put to file: " + fileName + ", key: " + ce.getKey() + ", position: " + ded.pos
						+ ", size: " + ded.len);
			}
		}
		catch (IOException e)
		{
			log.error(cacheLogger + "fail to update element, key: " + ce.getKey() + " old: " + old, e);
		}
	}

	@Override
	protected ICacheElement<K, V> processGet(K key)
	{
		if (!isAlive())
		{
			log.error(cacheLogger + "no alive so returning null for key = " + key);
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
				object = readElement(key);
			}
			finally
			{
				storageLock.readLock().unlock();
			}

			if (object != null)
			{
				hitCount.incrementAndGet();
			}
		}
		catch (IOException ioe)
		{
			log.error(cacheLogger + "fail to get from disk, key = " + key, ioe);
			reset();
		}
		return object;
	}

	@Override
	public Map<K, ICacheElement<K, V>> processGetMatching(String pattern)
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();
		Set<K> keyArray = null;
		storageLock.readLock().lock();
		try
		{
			keyArray = new HashSet<K>(keyHash.keySet());
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

	private ICacheElement<K, V> readElement(K key) throws IOException
	{
		ICacheElement<K, V> object = null;

		IndexedDiskElementDescriptor ded = keyHash.get(key);

		if (ded != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "find on disk, key: " + key);
			}
			try
			{
				object = dataFile.readObject(ded);
			}
			catch (IOException e)
			{
				log.error(cacheLogger + "IO Exception, error occur in reading object from file", e);
				throw e;
			}
			catch (Exception e)
			{
				log.error(cacheLogger + "error occur in reading object from file", e);
				throw new IOException(cacheLogger + "error occur in reading object from disk. " + e.getMessage());
			}
		}

		return object;
	}

	@Override
	public Set<K> getKeySet() throws IOException
	{
		HashSet<K> keys = new HashSet<K>();

		storageLock.readLock().lock();

		try
		{
			keys.addAll(this.keyHash.keySet());
		}
		finally
		{
			storageLock.readLock().unlock();
		}

		return keys;
	}

	@Override
	protected boolean processRemove(K key)
	{
		if (!isAlive())
		{
			log.error(cacheLogger + "no alive so returning false for key = " + key);
			return false;
		}

		if (key == null)
		{
			return false;
		}

		boolean reset = false;
		boolean removed = false;
		try
		{
			storageLock.writeLock().lock();

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
		finally
		{
			storageLock.writeLock().unlock();
		}

		if (reset)
		{
			reset();
		}

		if (removed)
		{
			doOptimizeRealTime();
		}

		return removed;
	}

	private boolean performPartialKeyRemoval(String key)
	{
		boolean removed = false;

		List<K> itemsToRemove = new LinkedList<K>();

		for (K k : keyHash.keySet())
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

	private boolean performGroupRemoval(GroupId key)
	{
		boolean removed = false;

		List<K> itemsToRemove = new LinkedList<K>();

		for (K k : keyHash.keySet())
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

	private boolean performSingleKeyRemoval(K key)
	{
		boolean removed;
		IndexedDiskElementDescriptor ded = keyHash.remove(key);
		removed = ded != null;
		addToRecycleBin(ded);

		if (log.isDebugEnabled())
		{
			log.debug(cacheLogger + "disk removal: removed from key hash, key [" + key + "] removed = " + removed);
		}
		return removed;
	}

	@Override
	public void processRemoveAll()
	{
		ICacheEvent<String> cacheEvent = createICacheEvent(getCacheName(), "all", ICacheEventWrapper.REMOVEALL_EVENT);
		try
		{
			reset();
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
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

			if (dataFile != null)
			{
				dataFile.close();
			}
			File dataFileTemp = new File(cacheFileDir, fileName + ".data");
			boolean result = dataFileTemp.delete();
			if (!result && log.isDebugEnabled())
			{
				log.debug("Could not delete file " + dataFileTemp);
			}

			if (keyFile != null)
			{
				keyFile.close();
			}
			File keyFileTemp = new File(cacheFileDir, fileName + ".key");
			result = keyFileTemp.delete();
			if (!result && log.isDebugEnabled())
			{
				log.debug("Could not delete file " + keyFileTemp);
			}

			dataFile = new IndexedDisk(new File(cacheFileDir, fileName + ".data"), getElementSerializer());
			keyFile = new IndexedDisk(new File(cacheFileDir, fileName + ".key"), getElementSerializer());

			initializeRecycleBin();

			initializeKeyMap();
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

	private void initializeRecycleBin()
	{
		recycle = new ConcurrentSkipListSet<IndexedDiskElementDescriptor>();
	}

	private void initializeKeyMap()
	{
		keyHash = null;
		if (maxKeySize >= 0)
		{
			if (this.diskLimitType == IDiskCacheAttributes.DiskLimitType.COUNT)
			{
				keyHash = new LRUMapCountLimited(maxKeySize);
			}
			else
			{
				keyHash = new LRUMapSizeLimited(maxKeySize);
			}

			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "set maxKeySize to: '" + maxKeySize + "'");
			}
		}
		else
		{
			keyHash = new HashMap<K, IndexedDiskElementDescriptor>();
			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "set maxKeySize to unlimited'");
			}
		}
	}

	@Override
	public void processDispose()
	{
		ICacheEvent<String> cacheEvent = createICacheEvent(getCacheName(), "none", ICacheEventWrapper.DISPOSE_EVENT);
		try
		{
			Runnable runner = new Runnable() {
				@Override
				public void run()
				{
					disposeInternal();
				}
			};
			Thread thread = new Thread(runner, "IndexedDiskCache-DisposalThread");
			thread.start();
			try
			{
				thread.join(60 * 1000);
			}
			catch (InterruptedException ex)
			{
				log.error(cacheLogger + "interrupted while waiting for disposal thread to finish.", ex);
			}
		}
		finally
		{
			cacheEventLogger(cacheEvent);
		}
	}

	protected void disposeInternal()
	{
		if (!isAlive())
		{
			log.error(cacheLogger + "no alive and dispose was called, filename: " + fileName);
			return;
		}

		setAlive(false);

		Thread optimizationThread = currentOptimizationThread;
		if (isRealTimeOptimizationEnabled && optimizationThread != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "in dispose, optimization already " + "in progress; waiting for completion.");
			}
			try
			{
				optimizationThread.join();
			}
			catch (InterruptedException e)
			{
				log.error(cacheLogger + "unable to join current optimization thread.", e);
			}
		}
		else if (isShutdownOptimizationEnabled && this.getBytesFree() > 0)
		{
			optimizeFile();
		}

		saveKeys();

		try
		{
			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "close files, base filename: " + fileName);
			}
			dataFile.close();
			dataFile = null;
			keyFile.close();
			keyFile = null;
		}
		catch (IOException e)
		{
			log.error(cacheLogger + "fail to close files in dispose, filename: " + fileName, e);
		}

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "shutdown complete.");
		}
	}

	/**
	 * 添加磁盘对象信息到回收站recycle中
	 *
	 * @param ded 无用的磁盘对象
	 */
	protected void addToRecycleBin(IndexedDiskElementDescriptor ded)
	{
		if (ded != null)
		{
			storageLock.readLock().lock();

			try
			{
				this.adjustBytesFree(ded, true);

				if (doRecycle)
				{
					recycle.add(ded);
					if (log.isDebugEnabled())
					{
						log.debug(cacheLogger + "recycled ded" + ded);
					}
				}
			}
			finally
			{
				storageLock.readLock().unlock();
			}
		}
	}

	/**
	 * 实时开始优化（优化磁盘存储）
	 */
	protected void doOptimizeRealTime()
	{
		if (isRealTimeOptimizationEnabled && !isOptimizing && removeCount++ >= cattr.getOptimizeAtRemoveCount())
		{
			isOptimizing = true;

			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "optimize file. removeCount [" + removeCount + "] OptimizeAtRemoveCount ["
						+ cattr.getOptimizeAtRemoveCount() + "]");
			}

			if (currentOptimizationThread == null)
			{
				storageLock.writeLock().lock();

				try
				{
					if (currentOptimizationThread == null)
					{
						currentOptimizationThread = new Thread(new Runnable() {
							@Override
							public void run()
							{
								optimizeFile();

								currentOptimizationThread = null;
							}
						}, "IndexedDiskCache-OptimizationThread");
					}
				}
				finally
				{
					storageLock.writeLock().unlock();
				}

				if (currentOptimizationThread != null)
				{
					currentOptimizationThread.start();
				}
			}
		}
	}

	/**
	 * 优化存储文件的空间利用率
	 */
	protected void optimizeFile()
	{
		ElapsedTimer timer = new ElapsedTimer();
		timesOptimized++;
		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "begin to optimize " + timesOptimized);
		}

		IndexedDiskElementDescriptor[] defragList = null;

		storageLock.writeLock().lock();

		try
		{
			queueInput = true;
			doRecycle = false;
			defragList = createPositionSortedDescriptorList();
		}
		finally
		{
			storageLock.writeLock().unlock();
		}

		long expectedNextPos = defragFile(defragList, 0);

		storageLock.writeLock().lock();

		try
		{
			try
			{
				// 如果在优化的开始后，storageLock加锁了，这期间有新的缓存被新添加进来了，则queuedPutList中是有索引信息的，需进行优化
				if (!queuedPutList.isEmpty())
				{
					defragList = queuedPutList.toArray(new IndexedDiskElementDescriptor[0]);

					expectedNextPos = defragFile(defragList, expectedNextPos);
				}
				dataFile.truncate(expectedNextPos);
			}
			catch (IOException e)
			{
				log.error(cacheLogger + "error occur in optimizing queued puts.", e);
			}

			removeCount = 0;
			resetBytesFree();
			initializeRecycleBin();
			queuedPutList.clear();
			queueInput = false;
			doRecycle = true;
			isOptimizing = false;
		}
		finally
		{
			storageLock.writeLock().unlock();
		}

		if (log.isInfoEnabled())
		{
			log.info(cacheLogger + "finished " + timesOptimized + " optimization took " + timer.getElapsedTimeString());
		}
	}

	/**
	 * 整理磁盘碎片
	 *
	 * @param defragList 需要被整理的磁盘空间所对应索引信息列表（必须是连续的）
	 * @param startingPos 开始整理的文件偏移地址
	 */
	private long defragFile(IndexedDiskElementDescriptor[] defragList, long startingPos)
	{
		ElapsedTimer timer = new ElapsedTimer();
		long preFileSize = 0;
		long postFileSize = 0;
		long expectedNextPos = 0;
		try
		{
			preFileSize = this.dataFile.length();
			expectedNextPos = startingPos;
			for (int i = 0; i < defragList.length; i++)
			{
				storageLock.writeLock().lock();
				try
				{
					// 当前磁盘索引不是期望的偏移地址，则表示有磁盘碎片，移动数据
					if (expectedNextPos != defragList[i].pos)
					{
						dataFile.move(defragList[i], expectedNextPos);
					}
					expectedNextPos = defragList[i].pos + IndexedDisk.HEADER_SIZE_BYTES + defragList[i].len;
				}
				finally
				{
					storageLock.writeLock().unlock();
				}
			}

			postFileSize = this.dataFile.length();

			return expectedNextPos;
		}
		catch (IOException e)
		{
			log.error(cacheLogger + "error occur in during defragmentation.", e);
		}
		finally
		{
			if (log.isInfoEnabled())
			{
				log.info(cacheLogger + "defragmentation took " + timer.getElapsedTimeString() + ". File Size (before="
						+ preFileSize + ") (after=" + postFileSize + ") (truncating to " + expectedNextPos + ")");
			}
		}

		return 0;
	}

	/**
	 * 返回所有的索引信息，根据每个索引所对应的数据的起始文件偏移位置排序
	 *
	 * @return 排序之后的索引信息
	 */
	private IndexedDiskElementDescriptor[] createPositionSortedDescriptorList()
	{
		IndexedDiskElementDescriptor[] defragList = new IndexedDiskElementDescriptor[keyHash.size()];
		Iterator<Map.Entry<K, IndexedDiskElementDescriptor>> iterator = keyHash.entrySet().iterator();
		for (int i = 0; iterator.hasNext(); i++)
		{
			Map.Entry<K, IndexedDiskElementDescriptor> next = iterator.next();
			defragList[i] = next.getValue();
		}

		Arrays.sort(defragList, new PositionComparator());

		return defragList;
	}

	@Override
	public int getSize()
	{
		return keyHash.size();
	}

	protected int getRecyleBinSize()
	{
		return this.recycle.size();
	}

	protected int getRecyleCount()
	{
		return this.recycleCnt;
	}

	protected long getBytesFree()
	{
		return this.bytesFree.get();
	}

	private void resetBytesFree()
	{
		this.bytesFree.set(0);
	}

	/**
	 * 调整磁盘碎片总空间（已经分配了但还没用的空间）的大小
	 *
	 * @param ded 下标信息
	 * @param add 是否增量
	 *               true： 增加磁盘碎片的总空间
	 *               false： 减少磁盘碎片总空间
	 */
	private void adjustBytesFree(IndexedDiskElementDescriptor ded, boolean add)
	{
		if (ded != null)
		{
			int amount = ded.len + IndexedDisk.HEADER_SIZE_BYTES;

			if (add)
			{
				this.bytesFree.addAndGet(amount);
			}
			else
			{
				this.bytesFree.addAndGet(-amount);
			}
		}
	}

	protected long getDataFileSize() throws IOException
	{
		long size = 0;

		storageLock.readLock().lock();

		try
		{
			if (dataFile != null)
			{
				size = dataFile.length();
			}
		}
		finally
		{
			storageLock.readLock().unlock();
		}

		return size;
	}

	public void dump()
	{
		dump(true);
	}

	public void dump(boolean dumpValues)
	{
		if (log.isDebugEnabled())
		{
			log.debug(cacheLogger + "[dump] number of keys: " + keyHash.size());

			for (Map.Entry<K, IndexedDiskElementDescriptor> e : keyHash.entrySet())
			{
				K key = e.getKey();
				IndexedDiskElementDescriptor ded = e.getValue();

				log.debug(cacheLogger + "[dump] disk element, key: " + key + ", pos: " + ded.pos + ", ded.len" + ded.len
						+ (dumpValues ? ", val: " + get(key) : ""));
			}
		}
	}

	@Override
	public KitCacheAttributes getKitCacheAttributes()
	{
		return this.cattr;
	}

	@Override
	public synchronized IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("Indexed Disk Cache");

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();

		elems.add(new StatElement<Boolean>("Is Alive", isAlive()));
		elems.add(new StatElement<Integer>("Key Map Size",
				this.keyHash != null ? this.keyHash.size() : -1));
		try
		{
			elems.add(new StatElement<Long>("Data File Length",
					this.dataFile != null ? this.dataFile.length() : -1L));
		}
		catch (IOException e)
		{
			log.error(e);
		}
		elems.add(new StatElement<Integer>("Max Key Size", this.maxKeySize));
		elems.add(new StatElement<AtomicInteger>("Hit Count", this.hitCount));
		elems.add(new StatElement<AtomicLong>("Bytes Free", this.bytesFree));
		elems.add(new StatElement<Integer>("Optimize Operation Count", this.removeCount));
		elems.add(new StatElement<Integer>("Times Optimized", this.timesOptimized));
		elems.add(new StatElement<Integer>("Recycle Count", this.recycleCnt));
		elems.add(new StatElement<Integer>("Recycle Bin Size", this.recycle.size()));
		elems.add(new StatElement<Integer>("Startup Size", this.startupSize));

		IStats sStats = super.getStatistics();
		elems.addAll(sStats.getStatElements());

		stats.setStatElements(elems);

		return stats;
	}

	protected int getTimesOptimized()
	{
		return timesOptimized;
	}

	@Override
	protected String getDiskLocation()
	{
		return dataFile.getFilePath();
	}

	protected static final class PositionComparator implements Comparator<IndexedDiskElementDescriptor>, Serializable
	{
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(IndexedDiskElementDescriptor ded1, IndexedDiskElementDescriptor ded2)
		{
			return Long.compare(ded1.pos, ded2.pos);
		}
	}

	/**
	 * 根据缓存数据的大小进行LRU算法的执行（单位：KB）
	 */
	public class LRUMapSizeLimited extends AbstractLRUMap<K, IndexedDiskElementDescriptor>
	{

		private AtomicInteger contentSize; // 当前存储的数据大小，向上取整 单位：kb
		private int maxSize; // 能存储的数据最大值 单位：kb

		public LRUMapSizeLimited()
		{
			this(-1);
		}

		public LRUMapSizeLimited(int maxKeySize)
		{
			super();
			this.maxSize = maxKeySize;
			this.contentSize = new AtomicInteger(0);
		}

		private void subLengthFromCacheSize(IndexedDiskElementDescriptor value)
		{
			contentSize.addAndGet((value.len + IndexedDisk.HEADER_SIZE_BYTES) / -1024 - 1);
		}

		private void addLengthToCacheSize(IndexedDiskElementDescriptor value)
		{
			contentSize.addAndGet((value.len + IndexedDisk.HEADER_SIZE_BYTES) / 1024 + 1);
		}

		@Override
		public IndexedDiskElementDescriptor put(K key, IndexedDiskElementDescriptor value)
		{
			IndexedDiskElementDescriptor oldValue = null;

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
		public IndexedDiskElementDescriptor remove(Object key)
		{
			IndexedDiskElementDescriptor value = null;

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
		protected void processRemovedLRU(K key, IndexedDiskElementDescriptor value)
		{
			if (value != null)
			{
				subLengthFromCacheSize(value);
			}

			addToRecycleBin(value);

			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "remove key: [" + key + "] from key store.");
				log.debug(cacheLogger + "key store size: [" + this.size() + "].");
			}

			doOptimizeRealTime();
		}

		@Override
		protected boolean shouldRemove()
		{
			return maxSize > 0 && contentSize.get() > maxSize && this.size() > 0;
		}
	}

	/**
	 * 根据缓存的数量进行LRU算法的执行
	 */
	public class LRUMapCountLimited extends LRUMap<K, IndexedDiskElementDescriptor> implements Serializable
	{

		private static final long serialVersionUID = 1L;

		public LRUMapCountLimited(int maxKeySize)
		{
			super(maxKeySize);
		}

		@Override
		protected void processRemovedLRU(K key, IndexedDiskElementDescriptor value)
		{
			addToRecycleBin(value);

			if (log.isDebugEnabled())
			{
				log.debug(cacheLogger + "remove key: [" + key + "] from key store.");
				log.debug(cacheLogger + "key store size: [" + this.size() + "].");
			}

			doOptimizeRealTime();
		}
	}
}
