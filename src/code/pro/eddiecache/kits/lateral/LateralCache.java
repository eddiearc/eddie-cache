package pro.eddiecache.kits.lateral;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.core.CacheInfo;
import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.DaemonCacheServiceRemote;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheServiceRemote;
import pro.eddiecache.core.model.IDaemon;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.core.stats.Stats;
import pro.eddiecache.kits.AbstractKitCacheEvent;
import pro.eddiecache.kits.KitCacheAttributes;

public class LateralCache<K, V> extends AbstractKitCacheEvent<K, V>
{
	private static final Log log = LogFactory.getLog(LateralCache.class);

	private final ILateralCacheAttributes lateralCacheAttributes;


	private ICacheServiceRemote<K, V> lateralCacheService;

	private LateralCacheMonitor monitor;

	public LateralCache(ILateralCacheAttributes cattr, ICacheServiceRemote<K, V> lateral, LateralCacheMonitor monitor) {
		super(cattr.getCacheName());
		this.lateralCacheAttributes = cattr;
		this.lateralCacheService = lateral;
		this.monitor = monitor;
	}

	public LateralCache(ILateralCacheAttributes cattr) {
		super(cattr.getCacheName());
		this.lateralCacheAttributes = cattr;
	}

	@Override
	protected void processUpdate(ICacheElement<K, V> ce) throws IOException {
		try {
			if (ce != null) {
				if (log.isDebugEnabled()) {
					log.debug("Update: lateral = [" + lateralCacheService + "], " + "CacheInfo.listenerId = "
							+ CacheInfo.listenerId);
				}
				lateralCacheService.update(ce, CacheInfo.listenerId);
			}
		} catch (IOException ex) {
			handleException(ex,
					"Fail to put [" + ce.getKey() + "] to " + ce.getCacheName() + "@" + lateralCacheAttributes);
		}
	}

	@Override
	protected ICacheElement<K, V> processGet(K key) throws IOException {
		ICacheElement<K, V> obj = null;

		if (this.lateralCacheAttributes.getPutOnlyMode()) {
			return null;
		}
		try {
			obj = lateralCacheService.get(getCacheName(), key);
		} catch (Exception e) {
			log.error(e);
			handleException(e, "Fail to get [" + key + "] from " + lateralCacheAttributes.getCacheName() + "@"
					+ lateralCacheAttributes);
		}
		return obj;
	}

	@Override
	protected Map<K, ICacheElement<K, V>> processGetMatching(String pattern) throws IOException {
		if (this.lateralCacheAttributes.getPutOnlyMode()) {
			return Collections.emptyMap();
		}
		try {
			return lateralCacheService.getMatching(getCacheName(), pattern);
		} catch (IOException e) {
			log.error(e);
			handleException(e, "Fail to getMatching [" + pattern + "] from " + lateralCacheAttributes.getCacheName()
					+ "@" + lateralCacheAttributes);
			return Collections.emptyMap();
		}
	}

	@Override
	protected Map<K, ICacheElement<K, V>> processGetMultiple(Set<K> keys) throws IOException {
		Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

		if (keys != null && !keys.isEmpty()) {
			for (K key : keys) {
				ICacheElement<K, V> element = get(key);

				if (element != null) {
					elements.put(key, element);
				}
			}
		}

		return elements;
	}

	@Override
	public Set<K> getKeySet() throws IOException {
		try {
			return lateralCacheService.getKeySet(getCacheName());
		} catch (IOException ex) {
			handleException(ex,
					"Fail to get key set from " + lateralCacheAttributes.getCacheName() + "@" + lateralCacheAttributes);
		}
		return Collections.emptySet();
	}

	@Override
	protected boolean processRemove(K key) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Remove key:" + key);
		}

		try {
			lateralCacheService.remove(getCacheName(), key, CacheInfo.listenerId);
		} catch (IOException ex) {
			handleException(ex, "Fail to remove " + key + " from " + lateralCacheAttributes.getCacheName() + "@"
					+ lateralCacheAttributes);
		}
		return false;
	}

	@Override
	protected void processRemoveAll() throws IOException {
		try {
			lateralCacheService.removeAll(getCacheName(), CacheInfo.listenerId);
		} catch (IOException ex) {
			handleException(ex,
					"Fail to remove all from " + lateralCacheAttributes.getCacheName() + "@" + lateralCacheAttributes);
		}
	}

	@Override
	protected void processDispose() throws IOException {
		try {
			lateralCacheService.dispose(this.lateralCacheAttributes.getCacheName());
		} catch (IOException ex) {
			log.error("Couldn't dispose", ex);
			handleException(ex, "fail to dispose " + lateralCacheAttributes.getCacheName());
		}
	}

	@Override
	public CacheStatus getStatus() {
		return this.lateralCacheService instanceof IDaemon ? CacheStatus.ERROR : CacheStatus.ALIVE;
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

	private void handleException(Exception ex, String msg) throws IOException {
		lateralCacheService = new DaemonCacheServiceRemote<K, V>(lateralCacheAttributes.getDaemonQueueMaxSize());
		monitor.notifyError();

		if (ex instanceof IOException) {
			throw (IOException) ex;
		}
		throw new IOException(ex.getMessage());
	}

	public void fixCache(ICacheServiceRemote<K, V> restoredLateral) {
		if (this.lateralCacheService != null && this.lateralCacheService instanceof DaemonCacheServiceRemote) {
			DaemonCacheServiceRemote<K, V> daemon = (DaemonCacheServiceRemote<K, V>) this.lateralCacheService;
			this.lateralCacheService = restoredLateral;
			try {
				daemon.spreadEvents(restoredLateral);
			} catch (Exception e) {
				try {
					handleException(e,
							"Problem in spreading events from DaemonCacheServiceRemote to new Lateral Service.");
				} catch (IOException ignored) {
				}
			}
		} else {
			this.lateralCacheService = restoredLateral;
		}
	}

	@Override
	public String getStats() {
		return "";
	}

	@Override
	public KitCacheAttributes getKitCacheAttributes() {
		return lateralCacheAttributes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n LateralCache ")
				.append("\n Cache Name [").append(lateralCacheAttributes.getCacheName()).append("]")
				.append("\n cattr =  [").append(lateralCacheAttributes).append("]");

		return sb.toString();
	}

	@Override
	public String getEventLoggerExtraInfo() {
		return null;
	}

	@Override
	public IStats getStatistics() {
		IStats stats = new Stats();
		stats.setTypeName("LateralCache");
		return stats;
	}
}
