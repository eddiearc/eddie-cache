package pro.eddiecache.xml;

public class XmlParserException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public XmlParserException()
	{
		super();

	}

	public XmlParserException(String message)
	{
		super(message);
	}

	public XmlParserException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public XmlParserException(Throwable cause)
	{
		super(cause);
	}
}
