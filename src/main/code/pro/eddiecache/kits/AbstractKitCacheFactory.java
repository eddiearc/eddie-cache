package pro.eddiecache.kits;

/**
 * 缓存插件抽象工厂类
 */

public abstract class AbstractKitCacheFactory implements KitCacheFactory
{
	private String name = this.getClass().getSimpleName();

	@Override
	public void initialize()
	{
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}
}
