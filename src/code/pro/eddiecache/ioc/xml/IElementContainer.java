package pro.eddiecache.ioc.xml;

import java.util.Collection;

import org.dom4j.Document;
import org.dom4j.Element;

public interface IElementContainer
{
	void addElements(Document doc);

	Element getElement(String id);

	Collection<Element> getElements();
}
