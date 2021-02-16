package pro.eddiecache.core.model;

import java.util.concurrent.ScheduledExecutorService;

public interface IRequireScheduler
{
	void setScheduledExecutorService(ScheduledExecutorService scheduledExecutor);
}
