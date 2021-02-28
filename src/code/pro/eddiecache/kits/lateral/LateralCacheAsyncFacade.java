package pro.eddiecache.kits.lateral;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.stats.IStatElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.StatElement;
import pro.eddiecache.core.stats.Stats;
import pro.eddiecache.kits.AbstractKitCache;
import pro.eddiecache.kits.KitCache;
import pro.eddiecache.kits.KitCacheAttributes;
import pro.eddiecache.kits.lateral.tcp.TCPLateralCacheAttributes;

/**
 * @author eddie
 */
public class LateralCacheAsyncFacade<K, V> extends AbstractKitCache<K, V>
{
	private static final Log log = LogFactory.getLog(LateralCacheAsyncFacade.class);

	/**
	 * 用于存放远程实例，本类的方法都间接调用了async的update get remove等操作
	 */
	public LateralCacheAsync<K, V>[] asyncs;

	private final String cacheName;

	private ILateralCacheListener<K, V> listener;

	private final ILateralCacheAttributes lateralCacheAttributes;

	private boolean disposed = false;

	public LateralCacheAsyncFacade(ILateralCacheListener<K, V> listener, LateralCacheAsync<K, V>[] asyncs,
			ILateralCacheAttributes cattr)
	{
		this.listener = listener;
		this.asyncs = asyncs;
		this.cacheName = cattr.getCacheName();
		this.lateralCacheAttributes = cattr;
	}

	public boolean containsAsync(LateralCacheAsync<K, V> async)
	{
		for (LateralCacheAsync<K, V> kvLateralCacheAsync : asyncs) {
//			if (async.equals(kvLateralCacheAsync)) {
//				return true;
//			}

			TCPLateralCacheAttributes attr1 = (TCPLateralCacheAttributes) async.getKitCacheAttributes();

			TCPLateralCacheAttributes attr2 = (TCPLateralCacheAttributes) kvLateralCacheAsync.getKitCacheAttributes();

			if (attr1.getTcpServer().equals(attr2.getTcpServer())) {
				return true;
			}

		}
		return false;
	}

	public boolean containsAsync(String tcpServer)
	{
		for (LateralCacheAsync<K, V> async : asyncs) {

			TCPLateralCacheAttributes attr = (TCPLateralCacheAttributes) async.getKitCacheAttributes();

			if (tcpServer.equals(attr.getTcpServer())) {
				return true;
			}

		}
		return false;
	}

	public synchronized boolean addAsync(LateralCacheAsync<K, V> async)
	{
		if (async == null)
		{
			return false;
		}

		if (containsAsync(async))
		{
			if (log.isDebugEnabled())
			{
				log.debug("Async already contained, [" + async + "]");
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		LateralCacheAsync<K, V>[] newArray = new LateralCacheAsync[asyncs.length + 1];

		System.arraycopy(asyncs, 0, newArray, 0, asyncs.length);

		newArray[asyncs.length] = async;

		asyncs = newArray;

		log.debug("Async length is [" + asyncs.length + "]");

		return true;
	}

	public synchronized boolean removeAsync(LateralCacheAsync<K, V> async)
	{
		if (async == null)
		{
			return false;
		}

		int position = -1;
		for (int i = 0; i < asyncs.length; i++)
		{
			//			if (async.equals(asyncs[i]))
			//			{
			//				position = i;
			//				break;
			//			}

			TCPLateralCacheAttributes attr1 = (TCPLateralCacheAttributes) async.getKitCacheAttributes();

			TCPLateralCacheAttributes attr2 = (TCPLateralCacheAttributes) asyncs[i].getKitCacheAttributes();

			if (attr1.getTcpServer().equals(attr2.getTcpServer()))
			{
				position = i;
				break;
			}

		}

		if (position == -1)
		{
			return false;
		}

		@SuppressWarnings("unchecked")
		LateralCacheAsync<K, V>[] newArray = new LateralCacheAsync[asyncs.length - 1];

		System.arraycopy(asyncs, 0, newArray, 0, position);
		if (asyncs.length != position)
		{
			System.arraycopy(asyncs, position + 1, newArray, position, asyncs.length - position - 1);
		}
		asyncs = newArray;

		return true;
	}

	public synchronized boolean removeAsync(String tcpServer)
	{
		if (tcpServer == null)
		{
			return false;
		}

		int position = -1;
		for (int i = 0; i < asyncs.length; i++)
		{

			TCPLateralCacheAttributes attr = (TCPLateralCacheAttributes) asyncs[i].getKitCacheAttributes();

			if (tcpServer.equals(attr.getTcpServer()))
			{
				position = i;
				break;
			}

		}

		if (position == -1)
		{
			return false;
		}

		@SuppressWarnings("unchecked")
		LateralCacheAsync<K, V>[] newArray = new LateralCacheAsync[asyncs.length - 1];

		System.arraycopy(asyncs, 0, newArray, 0, position);
		if (asyncs.length != position)
		{
			System.arraycopy(asyncs, position + 1, newArray, position, asyncs.length - position - 1);
		}
		asyncs = newArray;

		return true;
	}

	@Override
	public void update(ICacheElement<K, V> ce)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Updating through lateral cache facade, asyncs length = " + asyncs.length);
		}
		try
		{
			for (LateralCacheAsync<K, V> async : asyncs) {
				async.update(ce);
			}
		}
		catch (Exception ex)
		{
			log.error(ex);
		}
	}

	@Override
	public ICacheElement<K, V> get(K key)
	{
		for (LateralCacheAsync<K, V> async : asyncs) {
			try {
				ICacheElement<K, V> obj = async.get(key);

				if (obj != null) {
					return obj;
				}
			} catch (Exception ex) {
				log.error("Fail to get", ex);
			}
		}
		return null;
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys)
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		if (keys != null && !keys.isEmpty())
		{
			for (K key : keys)
			{
				ICacheElement<K, V> element = get(key);

				if (element != null)
				{
					elements.put(key, element);
				}
			}
		}

		return elements;
	}

	@Override
	public Map<K, ICacheElement<K, V>> getMatching(String pattern)
	{
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();
		for (LateralCacheAsync<K, V> async : asyncs) {
			try {
				elements.putAll(async.getMatching(pattern));
			} catch (Exception ex) {
				log.error("Fail to get", ex);
			}
		}
		return elements;
	}

	@Override
	public Set<K> getKeySet() throws IOException
	{
		HashSet<K> allKeys = new HashSet<K>();
		for (KitCache<K, V> kit : asyncs) {
			if (kit != null) {
				Set<K> keys = kit.getKeySet();
				if (keys != null) {
					allKeys.addAll(keys);
				}
			}
		}
		return allKeys;
	}

	@Override
	public boolean remove(K key)
	{
		try
		{
			for (LateralCacheAsync<K, V> async : asyncs) {
				async.remove(key);
			}
		}
		catch (Exception ex)
		{
			log.error(ex);
		}
		return false;
	}

	@Override
	public void removeAll()
	{
		try
		{
			for (LateralCacheAsync<K, V> async : asyncs) {
				async.removeAll();
			}
		}
		catch (Exception ex)
		{
			log.error(ex);
		}
	}

	@Override
	public void dispose()
	{
		try
		{
			if (listener != null)
			{
				listener.dispose();
				listener = null;
			}

			for (LateralCacheAsync<K, V> async : asyncs) {
				async.dispose();
			}
		}
		catch (Exception ex)
		{
			log.error(ex);
		}
		finally
		{
			disposed = true;
		}
	}

	@Override
	public int getSize()
	{
		return 0;
	}

	@Override
	public CacheType getCacheType()
	{
		return CacheType.LATERAL_CACHE;
	}

	@Override
	public String getCacheName()
	{
		return cacheName;

	}

	@Override
	public CacheStatus getStatus()
	{
		if (disposed)
		{
			return CacheStatus.DISPOSED;
		}

		if (asyncs.length == 0 || listener != null)
		{
			return CacheStatus.ALIVE;
		}

		CacheStatus[] status = new CacheStatus[asyncs.length];
		for (int i = 0; i < asyncs.length; i++)
		{
			status[i] = asyncs[i].getStatus();
		}
		for (int i = 0; i < asyncs.length; i++)
		{
			if (status[i] == CacheStatus.ALIVE)
			{
				return CacheStatus.ALIVE;
			}
		}
		for (int i = 0; i < asyncs.length; i++)
		{
			if (status[i] == CacheStatus.ERROR)
			{
				return CacheStatus.ERROR;
			}
		}

		return CacheStatus.DISPOSED;
	}

	@Override
	public KitCacheAttributes getKitCacheAttributes()
	{
		return this.lateralCacheAttributes;
	}

	@Override
	public String toString()
	{
		return "LateralCacheAsyncFacade: " + cacheName;
	}

	@Override
	public String getEventLoggerExtraInfo()
	{
		return "Lateral Cache Async";
	}

	@Override
	public String getStats()
	{
		return getStatistics().toString();
	}

	@Override
	public IStats getStatistics()
	{
		IStats stats = new Stats();
		stats.setTypeName("Lateral Cache Async Facade");

		ArrayList<IStatElement<?>> elems = new ArrayList<IStatElement<?>>();

		if (asyncs != null)
		{
			elems.add(new StatElement<Integer>("Number of Async", asyncs.length));

			for (LateralCacheAsync<K, V> async : asyncs)
			{
				if (async != null)
				{
					IStats sStats = async.getStatistics();
					elems.addAll(sStats.getStatElements());
				}
			}
		}

		stats.setStatElements(elems);

		return stats;
	}
}
