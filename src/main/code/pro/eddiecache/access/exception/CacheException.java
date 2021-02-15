package pro.eddiecache.access.exception;

public class CacheException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public CacheException()
	{
		super();
	}

	public CacheException(Throwable nested)
	{
		super(nested);
	}

	public CacheException(String message)
	{
		super(message);
	}

	public CacheException(String message, Throwable nested)
	{
		super(message, nested);
	}
}
