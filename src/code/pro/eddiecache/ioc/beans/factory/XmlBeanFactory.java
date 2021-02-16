package pro.eddiecache.ioc.beans.factory;

import pro.eddiecache.ioc.context.AbstractAppContext;

public class XmlBeanFactory extends AbstractAppContext
{
	public XmlBeanFactory(String[] xmlPaths)
	{
		super.setUpElements(xmlPaths);
	}
}
