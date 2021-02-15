package pro.eddiecache.core.stats;

public class StatElement<V> implements IStatElement<V>
{

	private static final long serialVersionUID = 1L;

	private String name = null;

	private V data = null;

	public StatElement(String name, V data)
	{
		super();
		this.name = name;
		this.data = data;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public V getData()
	{
		return data;
	}

	@Override
	public void setData(V data)
	{
		this.data = data;
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		buf.append(name).append(" = ").append(data);
		return buf.toString();
	}
}
