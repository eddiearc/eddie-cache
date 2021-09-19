package pro.eddiecache.kits.lsm;

import org.rocksdb.Options;
import pro.eddiecache.kits.AbstractKitCacheAttributes;

/**
 * @author eddie
 * @create 2021/9/17 15:42
 */
public class RocksDbAttributes extends AbstractKitCacheAttributes implements IRocksDbAttributes {

    private String path;

    private Options options;

    @Override
    public void setOptions(Options options) {
        this.options = options;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
