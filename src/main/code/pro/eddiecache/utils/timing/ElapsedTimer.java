package pro.eddiecache.utils.timing;

public class ElapsedTimer
{
	private static final String SUFFIX = "ms.";

	private long timeStamp = System.currentTimeMillis();

	public long getElapsedTime()
	{
		long now = System.currentTimeMillis();
		long elapsed = now - timeStamp;
		timeStamp = now;
		return elapsed;
	}

	public String getElapsedTimeString()
	{
		return String.valueOf(getElapsedTime()) + SUFFIX;
	}
}
