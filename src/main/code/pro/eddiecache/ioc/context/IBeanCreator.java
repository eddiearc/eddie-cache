package pro.eddiecache.ioc.context;

import java.util.List;

public interface IBeanCreator
{

	Object createBeanUseDefaultConstruct(String className);

	Object createBeanUseDefineConstruce(String className, List<Object> args);

}
