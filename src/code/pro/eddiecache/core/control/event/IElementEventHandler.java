package pro.eddiecache.core.control.event;

/**
 * 事件处理器
 */
public interface IElementEventHandler
{
	<T> void handleElementEvent(IElementEvent<T> event);
}
