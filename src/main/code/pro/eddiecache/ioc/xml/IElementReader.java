package pro.eddiecache.ioc.xml;

import java.util.List;

import pro.eddiecache.ioc.xml.autowire.IAutowire;
import pro.eddiecache.ioc.xml.construct.ParamElement;
import pro.eddiecache.ioc.xml.property.PropertyElement;
import org.dom4j.Element;

public interface IElementReader
{

	boolean isLazy(Element element);

	List<Element> getConstructorElements(Element element);

	String getAttribute(Element element, String name);

	boolean isSingleton(Element element);

	List<Element> getPropertyElements(Element element);

	IAutowire getAutowire(Element element);

	List<ParamElement> getConstructorValue(Element element);

	List<PropertyElement> getPropertyValue(Element element);
}
