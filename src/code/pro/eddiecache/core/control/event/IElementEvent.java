package pro.eddiecache.core.control.event;

import java.io.Serializable;

/**
 * 缓存事件
 */
public interface IElementEvent<T> extends Serializable
{
	ElementEventType getElementEvent();

	T getSource();
}
