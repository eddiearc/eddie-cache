package pro.eddiecache.utils.config;

public class PropertySetterException extends Exception
{

	private static final long serialVersionUID = 1L;

	private final Throwable rootCause;

	public PropertySetterException(String msg)
	{
		super(msg);
		this.rootCause = null;
	}

	public PropertySetterException(Throwable rootCause)
	{
		super();
		this.rootCause = rootCause;
	}

	@Override
	public String getMessage()
	{
		String msg = super.getMessage();
		if (msg == null && rootCause != null)
		{
			msg = rootCause.getMessage();
		}
		return msg;
	}
}
