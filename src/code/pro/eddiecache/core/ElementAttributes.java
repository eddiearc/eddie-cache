package pro.eddiecache.core;

import java.util.ArrayList;
import java.util.List;

import pro.eddiecache.core.control.event.IElementEventHandler;
import pro.eddiecache.core.model.IElementAttributes;

public class ElementAttributes implements IElementAttributes
{

	private static final long serialVersionUID = 1L;

	private boolean IS_SPOOL = true;

	private boolean IS_LATERAL = true;

	private boolean IS_REMOTE = true;

	private boolean IS_ETERNAL = true;

	private long maxLife = -1;

	private long maxIdleTime = -1;

	private int size = 0;

	private long createTime = 0;

	private long lastAccessTime = 0;

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

	public void setCreateTime()
	{
		createTime = System.currentTimeMillis();
	}

	@Override
	public long getIdleTime()
	{
		return this.maxIdleTime;
	}

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
		StringBuilder sb = new StringBuilder();

		sb.append("[ IS_LATERAL = ").append(IS_LATERAL);
		sb.append(", IS_SPOOL = ").append(IS_SPOOL);
		sb.append(", IS_REMOTE = ").append(IS_REMOTE);
		sb.append(", IS_ETERNAL = ").append(IS_ETERNAL);
		sb.append(", MaxLifeSeconds = ").append(this.getMaxLife());
		sb.append(", IdleTime = ").append(this.getIdleTime());
		sb.append(", CreateTime = ").append(this.getCreateTime());
		sb.append(", LastAccessTime = ").append(this.getLastAccessTime());
		sb.append(", getTimeToLiveSeconds() = ").append(String.valueOf(getTimeToLiveSeconds()));
		sb.append(", createTime = ").append(String.valueOf(createTime)).append(" ]");

		return sb.toString();
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
