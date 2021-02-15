package pro.eddiecache.ioc.context;

public interface IAppContext
{
	Object getBean(String id);

	boolean containsBean(String id);

	boolean isSingleton(String id);

	Object getBeanIgnoreCreate(String id);
}
