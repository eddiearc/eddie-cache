package pro.eddiecache.core.model;

import java.util.concurrent.ScheduledExecutorService;

public interface IProvideScheduler
{
	ScheduledExecutorService getScheduledExecutorService();
}
