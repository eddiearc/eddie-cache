package pro.eddiecache.kits.lsm;

import org.rocksdb.Options;
import pro.eddiecache.kits.KitCacheAttributes;

/**
 * @author eddie
 * @create 2021/9/17 14:47
 */
public interface IRocksDbAttributes extends KitCacheAttributes {

    /**
     * @param options rocksdb options
     */
    void setOptions(Options options);

    /**
     * @return get rocksdb options
     */
    Options getOptions();

    /**
     * set rocksdb data path
     * @param path rocksdb path
     */
    void setPath(String path);

    /**
     * get rocksdb data path
     * @return rocksdb path
     */
    String getPath();
}
