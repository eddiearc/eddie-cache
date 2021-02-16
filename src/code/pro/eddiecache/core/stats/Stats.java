package pro.eddiecache.core.stats;

import java.util.List;

public class Stats implements IStats
{
	private static final long serialVersionUID = 1L;

	private List<IStatElement<?>> stats = null;

	private String typeName = null;

	@Override
	public List<IStatElement<?>> getStatElements()
	{
		return stats;
	}

	@Override
	public void setStatElements(List<IStatElement<?>> stats)
	{
		this.stats = stats;
	}

	@Override
	public String getTypeName()
	{
		return typeName;
	}

	@Override
	public void setTypeName(String name)
	{
		typeName = name;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(typeName);

		if (stats != null)
		{
			for (Object stat : stats)
			{
				sb.append("\n");
				sb.append(stat);
			}
		}

		return sb.toString();
	}
}
