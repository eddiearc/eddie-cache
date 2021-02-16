package pro.eddiecache.ioc.xml.construct;

public class ValueElement implements ParamElement
{

	private Object value;

	public ValueElement(Object value)
	{
		this.value = value;
	}

	@Override
	public String getType()
	{
		return "value";
	}

	@Override
	public Object getValue()
	{
		return this.value;
	}

}
