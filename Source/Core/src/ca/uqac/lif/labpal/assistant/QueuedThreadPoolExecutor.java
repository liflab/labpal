package ca.uqac.lif.labpal.assistant;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class QueuedThreadPoolExecutor extends ScheduledThreadPoolExecutor implements ClearableExecutorService
{
	public QueuedThreadPoolExecutor(int num_threads)
	{
		super(num_threads);
	}

	@Override
	public void clearWaiting()
	{
		getQueue().clear();
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		getQueue().clear();
		return super.shutdownNow();
	}
}