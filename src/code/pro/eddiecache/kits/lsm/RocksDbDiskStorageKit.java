package pro.eddiecache.kits.lsm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import pro.eddiecache.core.CacheElement;
import pro.eddiecache.core.CacheElementSerialized;
import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.ICacheElementSerialized;
import pro.eddiecache.core.model.IElementSerializer;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.kits.AbstractKitCacheEvent;
import pro.eddiecache.kits.KitCacheAttributes;
import pro.eddiecache.utils.serialization.SerializationConversionUtil;
import pro.eddiecache.utils.serialization.StandardSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author eddie
 * @create 2021/9/17 09:57
 */
public class RocksDbDiskStorageKit<K extends Comparable<K>, V> extends AbstractKitCacheEvent<K, V> {

    private static final Log log = LogFactory.getLog(RocksDbDiskStorageKit.class);

    private final IRocksDbAttributes attr;

    private final IElementSerializer serializer;

    private final RocksDB rocksDB;

    public RocksDbDiskStorageKit(IRocksDbAttributes attr) throws RocksDBException {
        this(attr, new StandardSerializer());
    }

    public RocksDbDiskStorageKit(IRocksDbAttributes attr, IElementSerializer serializer) throws RocksDBException {
        super(attr.getCacheName());
        this.rocksDB = RocksDB.open(attr.getOptions(), attr.getPath());
        this.attr = attr;
        this.serializer = serializer;
    }


    @Override
    public int getSize() {
        return -1;
    }

    @Override
    public CacheStatus getStatus() {
        return null;
    }

    @Override
    public String getStats() {
        return null;
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.ROCKSDB;
    }

    @Override
    public String getEventLoggerExtraInfo() {
        return null;
    }

    @Override
    protected void processUpdate(ICacheElement<K, V> cacheElement) throws IOException {
        ICacheElementSerialized serializedCacheElement = SerializationConversionUtil.serializeCacheElement(cacheElement, serializer);
        try {
            rocksDB.put(serializedCacheElement.getSerializedValue(), serializedCacheElement.getSerializedValue());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ICacheElement<K, V> processGet(K key) throws IOException {
        try {
            byte[] serializedKey = SerializationConversionUtil.serializeSingleField(key);
            byte[] serializedValue = rocksDB.get(serializedKey);

            return SerializationConversionUtil.deSerializeCacheElement(
                    getCacheName(),
                    new CacheElementSerialized(serializedKey, serializedValue)
            );
        } catch (RocksDBException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Map<K, ICacheElement<K, V>> processGetMultiple(Set<K> keys) throws IOException {
        Map<K, ICacheElement<K, V>> kvPairs = new HashMap<>(keys.size());

        for (K key : keys) {
            kvPairs.put(key, processGet(key));
        }

        return kvPairs;
    }

    @Override
    protected Map<K, ICacheElement<K, V>> processGetMatching(String pattern) throws IOException {
        Map<K, ICacheElement<K, V>> kvPairs = new HashMap<>();

        try (RocksIterator iterator = rocksDB.newIterator()) {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                K key = SerializationConversionUtil.deSerializeSingleField(iterator.key());
                if (key instanceof String && ((String) key).matches(pattern)) {
                    V val = SerializationConversionUtil.deSerializeSingleField(iterator.key());
                    ICacheElement<K, V> cacheElement = new CacheElement<>(getCacheName(), key, val);
                    kvPairs.put(key, cacheElement);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return kvPairs;
    }

    @Override
    protected boolean processRemove(K key) throws IOException {
        try {
            rocksDB.delete(SerializationConversionUtil.serializeSingleField(key));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void processRemoveAll() throws IOException {
        // delete all
    }

    @Override
    protected void processDispose() throws IOException {
        rocksDB.close();
    }

    @Override
    public Set<K> getKeySet() throws IOException {
        return null;
    }

    @Override
    public IStats getStatistics() {
        return null;
    }

    @Override
    public KitCacheAttributes getKitCacheAttributes() {
        return null;
    }
}
