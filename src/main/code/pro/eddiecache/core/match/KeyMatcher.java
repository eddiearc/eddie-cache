package pro.eddiecache.core.match;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyMatcher<K> implements IKeyMatcher<K>
{

	private static final long serialVersionUID = 1L;

	@Override
	public Set<K> getMatchingKeysFromArray(String pattern, Set<K> keyArray)
	{
		Pattern compiledPattern = Pattern.compile(pattern);

		Set<K> matchingKeys = new HashSet<K>();

		for (K key : keyArray)
		{
			if (key instanceof String)
			{
				Matcher matcher = compiledPattern.matcher((String) key);
				if (matcher.matches())
				{
					matchingKeys.add(key);
				}
			}
		}

		return matchingKeys;
	}
}
