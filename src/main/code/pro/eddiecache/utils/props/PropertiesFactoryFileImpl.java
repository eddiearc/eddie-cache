package pro.eddiecache.utils.props;

import java.util.Properties;

public class PropertiesFactoryFileImpl implements PropertiesFactory
{
	@Override
	public Properties getProperties(String groupName)
	{
		return PropertyLoader.loadProperties(groupName);
	}
}
