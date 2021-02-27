package pro.eddiecache.core.control.event;

/**
 * @author eddie
 * 事件处理器
 */
public interface IElementEventHandler
{
	/**
	 * 对事件进行处理
	 * 可以通过事件得到对应的eventType与源缓存数据
	 *
	 * @param event 事件
	 */
	<T> void handleElementEvent(IElementEvent<T> event);
}
