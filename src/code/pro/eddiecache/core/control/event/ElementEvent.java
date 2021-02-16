package pro.eddiecache.core.control.event;

import java.util.EventObject;

public class ElementEvent<T> extends EventObject implements IElementEvent<T>
{
	private static final long serialVersionUID = 1L;

	private ElementEventType elementEvent = ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND;

	public ElementEvent(T source, ElementEventType elementEvent)
	{
		super(source);
		this.elementEvent = elementEvent;
	}

	@Override
	public ElementEventType getElementEvent()
	{
		return elementEvent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getSource()
	{
		return (T) super.getSource();

	}
}
