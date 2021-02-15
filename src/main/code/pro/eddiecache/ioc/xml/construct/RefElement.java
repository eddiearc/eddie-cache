package pro.eddiecache.ioc.xml.construct;

public class RefElement implements ParamElement
{

	private Object value;

	public RefElement(Object value)
	{
		this.value = value;
	}

	@Override
	public String getType()
	{
		return "ref";
	}

	@Override
	public Object getValue()
	{
		return this.value;
	}

}
