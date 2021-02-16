package pro.eddiecache.core.control.group;

import java.io.Serializable;

public class GroupId implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String groupName;

	public final String cacheName;

	private String toString;

	public GroupId(String cacheName, String groupName)
	{
		this.cacheName = cacheName;
		this.groupName = groupName;

		if (cacheName == null)
		{
			throw new IllegalArgumentException("CacheName must not be null.");
		}
		if (groupName == null)
		{
			throw new IllegalArgumentException("GroupName must not be null.");
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof GroupId))
		{
			return false;
		}
		GroupId g = (GroupId) obj;
		return cacheName.equals(g.cacheName) && groupName.equals(g.groupName);
	}

	@Override
	public int hashCode()
	{
		return cacheName.hashCode() + groupName.hashCode();
	}

	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "[groupId=" + cacheName + ", " + groupName + ']';
		}

		return toString;
	}
}
