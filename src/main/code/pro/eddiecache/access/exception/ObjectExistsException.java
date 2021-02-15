package pro.eddiecache.access.exception;

public class ObjectExistsException extends CacheException
{

	private static final long serialVersionUID = 1L;

	public ObjectExistsException()
	{
		super();
	}

	public ObjectExistsException(String message)
	{
		super(message);
	}

}
