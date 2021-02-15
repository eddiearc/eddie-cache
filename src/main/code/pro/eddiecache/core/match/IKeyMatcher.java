package pro.eddiecache.core.match;

import java.io.Serializable;
import java.util.Set;

/**
 * 找到符合某种模式的key集合
 */
public interface IKeyMatcher<K> extends Serializable
{
	Set<K> getMatchingKeysFromArray(String pattern, Set<K> keyArray);
}
