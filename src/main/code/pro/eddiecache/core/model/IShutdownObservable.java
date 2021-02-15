package pro.eddiecache.core.model;

/*
 * JDK里提供的observer设计模式的实现由java.util.Observable类和 java.util.Observer接口组成。
 * 从名字上可以清楚的看出两者在Observer 设计模式中分别扮演的角色：
 * Observer是观察者角色，Observable是被观察目标(subject)角色。
 */
public interface IShutdownObservable
{

	void registerShutdownObserver(IShutdownObserver observer);

	void deregisterShutdownObserver(IShutdownObserver observer);

}
