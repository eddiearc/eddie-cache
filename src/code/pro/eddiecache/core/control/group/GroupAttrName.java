package pro.eddiecache.core.control.group;

import java.io.Serializable;

/**
 * 
 */
public class GroupAttrName<T> implements Serializable
{

	private static final long serialVersionUID = 1L;

	public final GroupId groupId;

	public final T attrName;

	private String toString;

	public GroupAttrName(GroupId groupId, T attrName)
	{
		this.groupId = groupId;
		this.attrName = attrName;

		if (groupId == null)
		{
			throw new IllegalArgumentException("groupId must not be null.");
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof GroupAttrName))
		{
			return false;
		}
		GroupAttrName<?> to = (GroupAttrName<?>) obj;

		if (groupId.equals(to.groupId))
		{
			if (attrName == null && to.attrName == null)
			{
				return true;
			}
			else if (attrName == null || to.attrName == null)
			{
				return false;
			}

			return attrName.equals(to.attrName);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		if (attrName == null)
		{
			return groupId.hashCode();
		}

		return groupId.hashCode() ^ attrName.hashCode();
	}

	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "[GroupAttrName: groupId=" + groupId + ", attrName=" + attrName + "]";
		}

		return toString;
	}

}
