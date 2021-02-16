package pro.eddiecache.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pro.eddiecache.core.control.event.IElementEventHandler;

public interface IElementAttributes extends Serializable, Cloneable
{
	//元素生命周期
	void setMaxLife(long mls);

	long getMaxLife();

	//元素空闲时间
	void setIdleTime(long idle);

	long getIdleTime();

	//元素个数
	void setSize(int size);

	int getSize();

	long getCreateTime();

	//最近访问时间
	long getLastAccessTime();

	void setLastAccessTimeNow();

	long getTimeToLiveSeconds();

	//是否刷盘
	boolean getIsSpool();

	void setIsSpool(boolean val);

	//是否线性
	boolean getIsLateral();

	void setIsLateral(boolean val);

	//是否主从
	boolean getIsRemote();

	void setIsRemote(boolean val);

	//是否永生
	boolean getIsEternal();

	void setIsEternal(boolean val);

	//处理器
	void addElementEventHandler(IElementEventHandler eventHandler);

	ArrayList<IElementEventHandler> getElementEventHandlers();

	void addElementEventHandlers(List<IElementEventHandler> eventHandlers);

	long getTimeFactorForMilliseconds();

	void setTimeFactorForMilliseconds(long factor);

	IElementAttributes clone();
}
