package pro.eddiecache.ioc.xml.autowire;

public class NoAutowire implements IAutowire
{

	private String value;

	public NoAutowire(String value)
	{
		this.value = value;
	}

	@Override
	public String getValue()
	{
		return "no";
	}

}
