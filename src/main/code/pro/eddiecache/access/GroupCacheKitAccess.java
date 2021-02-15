package pro.eddiecache.access;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import pro.eddiecache.access.exception.CacheException;
import pro.eddiecache.access.exception.InvalidArgumentException;
import pro.eddiecache.core.CacheElement;
import pro.eddiecache.core.control.ContextCache;
import pro.eddiecache.core.control.group.GroupAttrName;
import pro.eddiecache.core.control.group.GroupId;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.model.IElementAttributes;

public class GroupCacheKitAccess<K, V> extends AbstractCacheKitAccess<GroupAttrName<K>, V>
		implements IGroupCacheKitAccess<K, V>
{
	public GroupCacheKitAccess(ContextCache<GroupAttrName<K>, V> cacheControl)
	{
		super(cacheControl);
	}

	@Override
	public V getFromGroup(K name, String group)
	{
		ICacheElement<GroupAttrName<K>, V> element = this.getCacheControl().get(getGroupAttrName(group, name));
		return (element != null) ? element.getVal() : null;
	}

	private GroupAttrName<K> getGroupAttrName(String group, K name)
	{
		GroupId gid = new GroupId(this.getCacheControl().getCacheName(), group);
		return new GroupAttrName<K>(gid, name);
	}

	@Override
	public void putInGroup(K name, String groupName, V value) throws CacheException
	{
		putInGroup(name, groupName, value, null);
	}

	@Override
	public void putInGroup(K name, String groupName, V value, IElementAttributes attr) throws CacheException
	{
		if (name == null)
		{
			throw new InvalidArgumentException("Key must not be null");
		}

		if (value == null)
		{
			throw new InvalidArgumentException("Value must not be null");
		}

		try
		{
			GroupAttrName<K> key = getGroupAttrName(groupName, name);
			CacheElement<GroupAttrName<K>, V> ce = new CacheElement<GroupAttrName<K>, V>(
					this.getCacheControl().getCacheName(), key, value);

			IElementAttributes attributes = (attr == null) ? this.getCacheControl().getElementAttributes() : attr;
			ce.setElementAttributes(attributes);

			this.getCacheControl().update(ce);
		}
		catch (IOException e)
		{
			throw new CacheException(e);
		}

	}

	@Override
	public void removeFromGroup(K name, String group)
	{
		GroupAttrName<K> key = getGroupAttrName(group, name);
		this.getCacheControl().remove(key);
	}

	@Override
	public Set<K> getGroupKeys(String group)
	{
		Set<K> groupKeys = new HashSet<K>();
		GroupId groupId = new GroupId(this.getCacheControl().getCacheName(), group);

		for (GroupAttrName<K> gan : this.getCacheControl().getKeySet())
		{
			if (gan.groupId.equals(groupId))
			{
				groupKeys.add(gan.attrName);
			}
		}

		return groupKeys;
	}

	public Set<String> getGroupNames()
	{
		HashSet<String> names = new HashSet<String>();
		for (GroupAttrName<K> gan : this.getCacheControl().getKeySet())
		{
			names.add(gan.groupId.groupName);
		}
		return names;
	}

	@Override
	public void invalidateGroup(String group)
	{
		this.getCacheControl().remove(getGroupAttrName(group, null));
	}
}
