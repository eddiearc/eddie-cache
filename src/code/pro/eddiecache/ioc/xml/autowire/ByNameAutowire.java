package pro.eddiecache.ioc.xml.autowire;

public class ByNameAutowire implements IAutowire
{
	private String value;

	public ByNameAutowire(String value)
	{
		this.value = value;
	}

	@Override
	public String getValue()
	{
		return value;
	}
}
