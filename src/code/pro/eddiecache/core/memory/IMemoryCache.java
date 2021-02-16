package pro.eddiecache.core.memory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IContextCacheAttributes;
import pro.eddiecache.core.stats.IStats;

public interface IMemoryCache<K, V>
{
	//生命周期：开始
	void initialize(ContextCache<K, V> cache);

	//生命周期：结束
	void dispose() throws IOException;

	//统计相关
	int getSize();

	IStats getStatistics();

	Set<K> getKeySet();

	//读写操作
	ICacheElement<K, V> get(K key) throws IOException;

	Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys) throws IOException;

	ICacheElement<K, V> getQuiet(K key) throws IOException;

	void update(ICacheElement<K, V> ce) throws IOException;

	void waterfal(ICacheElement<K, V> ce) throws IOException;

	boolean remove(K key) throws IOException;

	void removeAll() throws IOException;

	int freeElements(int numberToFree) throws IOException;

	//属性设置
	IContextCacheAttributes getCacheAttributes();

	void setCacheAttributes(IContextCacheAttributes cattr);

	ContextCache<K, V> getContextCache();
}
