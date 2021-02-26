package pro.eddiecache.core;

import java.util.ArrayList;
import java.util.List;

import pro.eddiecache.core.control.event.IElementEventHandler;
import pro.eddiecache.core.model.IElementAttributes;

public class ElementAttributes implements IElementAttributes
{

	private static final long serialVersionUID = 1L;

	/**
	 * 是否持久化至磁盘中
	 */
	private boolean IS_SPOOL = true;

	/**
	 * 是否是线性组件
	 */
	private boolean IS_LATERAL = true;

	/**
	 * 是否远程
	 */
	private boolean IS_REMOTE = true;

	/**
	 * 节点是否持久性的，true： false：有超时时间（maxLife）
	 */
	private boolean IS_ETERNAL = true;

	/**
	 * 这个缓存对象的生命周期时长
	 */
	private long maxLife = -1;

	/**
	 * 这个缓存对象的最大的空闲时间（没有被访问的时间的允许最大值）
	 */
	private long maxIdleTime = -1;

	/**
	 * 缓存占用对象的大小
	 */
	private int size = 0;

	/**
	 * 创建缓存的时间
	 */
	private long createTime = 0;

	/**
	 * 最后一次访问时间
	 */
	private long lastAccessTime = 0;

	/**
	 * 这个缓存对象的执行器列表
	 * transient：不会被序列号
	 */
	private transient ArrayList<IElementEventHandler> eventHandlers;

	private long timeFactor = 1000;

	public ElementAttributes()
	{
		this.createTime = System.currentTimeMillis();
		this.lastAccessTime = this.createTime;
	}

	protected ElementAttributes(ElementAttributes attr)
	{
		IS_ETERNAL = attr.IS_ETERNAL;
		IS_SPOOL = attr.IS_SPOOL;
		IS_LATERAL = attr.IS_LATERAL;
		IS_REMOTE = attr.IS_REMOTE;
		maxLife = attr.maxLife;
		maxIdleTime = attr.maxIdleTime;
		size = attr.size;
	}

	/**
	 * 设置对象的过期时间
	 *
	 * @param mls 单位 秒
	 */
	@Override
	public void setMaxLife(long mls)
	{
		this.maxLife = mls;
	}

	@Override
	public long getMaxLife()
	{
		return this.maxLife;
	}

	@Override
	public void setIdleTime(long idle)
	{
		this.maxIdleTime = idle;
	}

	@Override
	public void setSize(int size)
	{
		this.size = size;
	}

	@Override
	public int getSize()
	{
		return size;
	}

	@Override
	public long getCreateTime()
	{
		return createTime;
	}

	/**
	 * 将对象的新增时间戳设置为当前时间
	 */
	public void setCreateTime()
	{
		createTime = System.currentTimeMillis();
	}

	@Override
	public long getIdleTime()
	{
		return this.maxIdleTime;
	}

	/**
	 * 计算剩余存活时间
	 *
	 * @return 剩余存活时间  单位 秒
	 */
	@Override
	public long getTimeToLiveSeconds()
	{
		final long now = System.currentTimeMillis();
		final long timeFactorForMilliseconds = getTimeFactorForMilliseconds();
		return (this.getCreateTime() + this.getMaxLife() * timeFactorForMilliseconds - now) / 1000;
	}

	@Override
	public long getLastAccessTime()
	{
		return this.lastAccessTime;
	}

	@Override
	public void setLastAccessTimeNow()
	{
		this.lastAccessTime = System.currentTimeMillis();
	}

	public void setLastAccessTime(long time)
	{
		this.lastAccessTime = time;
	}

	@Override
	public boolean getIsSpool()
	{
		return this.IS_SPOOL;
	}

	@Override
	public void setIsSpool(boolean val)
	{
		this.IS_SPOOL = val;
	}

	@Override
	public boolean getIsLateral()
	{
		return this.IS_LATERAL;
	}

	@Override
	public void setIsLateral(boolean val)
	{
		this.IS_LATERAL = val;
	}

	@Override
	public boolean getIsRemote()
	{
		return this.IS_REMOTE;
	}

	@Override
	public void setIsRemote(boolean val)
	{
		this.IS_REMOTE = val;
	}

	@Override
	public boolean getIsEternal()
	{
		return this.IS_ETERNAL;
	}

	@Override
	public void setIsEternal(boolean val)
	{
		this.IS_ETERNAL = val;
	}

	@Override
	public void addElementEventHandler(IElementEventHandler eventHandler)
	{
		if (this.eventHandlers == null)
		{
			this.eventHandlers = new ArrayList<IElementEventHandler>();
		}
		this.eventHandlers.add(eventHandler);
	}

	@Override
	public void addElementEventHandlers(List<IElementEventHandler> eventHandlers)
	{
		if (eventHandlers == null)
		{
			return;
		}

		for (IElementEventHandler handler : eventHandlers)
		{
			addElementEventHandler(handler);
		}
	}

	@Override
	public long getTimeFactorForMilliseconds()
	{
		return timeFactor;
	}

	@Override
	public void setTimeFactorForMilliseconds(long factor)
	{
		this.timeFactor = factor;
	}

	@Override
	public ArrayList<IElementEventHandler> getElementEventHandlers()
	{
		return this.eventHandlers;
	}

	@Override
	public String toString()
	{

		return "[ IS_LATERAL = " + IS_LATERAL +
				", IS_SPOOL = " + IS_SPOOL +
				", IS_REMOTE = " + IS_REMOTE +
				", IS_ETERNAL = " + IS_ETERNAL +
				", MaxLifeSeconds = " + this.getMaxLife() +
				", IdleTime = " + this.getIdleTime() +
				", CreateTime = " + this.getCreateTime() +
				", LastAccessTime = " + this.getLastAccessTime() +
				", getTimeToLiveSeconds() = " + String.valueOf(getTimeToLiveSeconds()) +
				", createTime = " + String.valueOf(createTime) + " ]";
	}

	@Override
	public IElementAttributes clone()
	{
		try
		{
			ElementAttributes attr = (ElementAttributes) super.clone();
			attr.setCreateTime();
			return attr;
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("Clone not supported. This should never happen.", e);
		}
	}
}
