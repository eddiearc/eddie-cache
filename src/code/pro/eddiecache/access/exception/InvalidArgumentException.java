package pro.eddiecache.access.exception;

public class InvalidArgumentException extends CacheException
{

	private static final long serialVersionUID = 1L;

	public InvalidArgumentException()
	{
		super();
	}

	public InvalidArgumentException(String message)
	{
		super(message);
	}
}
