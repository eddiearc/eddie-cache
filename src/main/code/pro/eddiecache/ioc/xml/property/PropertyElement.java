package pro.eddiecache.ioc.xml.property;

import pro.eddiecache.ioc.xml.construct.ParamElement;

public class PropertyElement
{

	private String name;

	private ParamElement paramElement;

	public PropertyElement(String name, ParamElement paramElement)
	{
		this.name = name;
		this.paramElement = paramElement;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ParamElement getParamElement()
	{
		return paramElement;
	}

	public void setParamElement(ParamElement paramElement)
	{
		this.paramElement = paramElement;
	}

}
