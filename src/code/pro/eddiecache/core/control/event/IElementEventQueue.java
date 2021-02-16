package pro.eddiecache.core.control.event;

import java.io.IOException;

/**
 * 事件处理器，事件，两者结对，存储到事件处理队列
 */
public interface IElementEventQueue
{
	<T> void addElementEvent(IElementEventHandler hand, IElementEvent<T> event) throws IOException;

	void dispose();
}
