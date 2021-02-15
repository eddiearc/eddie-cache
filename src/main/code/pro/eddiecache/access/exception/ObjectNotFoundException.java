package pro.eddiecache.access.exception;

public class ObjectNotFoundException extends CacheException
{

	private static final long serialVersionUID = 1L;

	public ObjectNotFoundException()
	{
		super();
	}

	public ObjectNotFoundException(String message)
	{
		super(message);
	}

}
