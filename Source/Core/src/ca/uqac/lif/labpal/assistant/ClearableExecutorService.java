package ca.uqac.lif.labpal.assistant;

import java.util.concurrent.ExecutorService;

public interface ClearableExecutorService extends ExecutorService
{
	public void clearWaiting();
}
