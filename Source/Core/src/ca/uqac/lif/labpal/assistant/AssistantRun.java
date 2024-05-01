package ca.uqac.lif.labpal.assistant;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ca.uqac.lif.labpal.claim.Condition;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentGroup;
import ca.uqac.lif.units.si.Millisecond;

public class AssistantRun extends ExperimentGroup implements Runnable
{
	/**
	 * A counter for run IDs
	 */
	protected static int s_idCounter = 1;

	/**
	 * The executor service used to dispatch the experiments in this run
	 * into threads.
	 */
	protected final ExecutorService m_executor;

	/**
	 * The start time of the run.
	 */
	protected long m_startTime;

	/**
	 * The end time of the run.
	 */
	protected long m_endTime;

	/**
	 * An optional condition used to determine if each experiment in the run
	 * should be executed.
	 */
	/*@ null @*/ protected Condition m_condition;

	public AssistantRun(/*@ non_null @*/ List<Experiment> experiments, /*@ non_null @*/ ExecutorService executor, /*@ null @*/ Condition c)
	{
		super("Run " + (s_idCounter + 1));
		m_id = s_idCounter++;
		m_objects.addAll(experiments);
		m_executor = executor;
		m_condition = c;
		m_startTime = -1;
		m_endTime = -1;
	}
	
	public boolean isRunning()
	{
		return !m_executor.isTerminated();
	}

	/**
	 * Gets the list of experiments concerned by this run.
	 * @return The list of experiments
	 */
	@Override
	/*@ pure non_null @*/ public List<Experiment> dependsOn()
	{
		return m_objects;
	}

	/**
	 * Gets the progression fraction of this run. The progression is taken as
	 * the average of the progression fraction of the individual experiments
	 * contained within the run. Hence if experiments report a progression of 1
	 * when they are done and 0 in other cases, the computed ratio corresponds
	 * to the fraction of experiments in the run that are completed.
	 * @return The progression fraction
	 */
	@Override
	/*@ pure @*/ public float getProgression()
	{
		float p = 0;
		float t = 0;
		for (Experiment e : m_objects)
		{
			p += e.getProgression();
			t++;
		}
		if (t == 0)
		{
			return 0;
		}
		return p / t;
	}

	@Override
	public void reset()
	{
		for (Experiment e : m_objects)
		{
			e.reset();
		}
	}

	/**
	 * Gets a unique ID string that represents this assistant's run.
	 * @return The ID
	 */
	/*@ non_null @*/ public int getId()
	{
		return m_id;
	}

	/**
	 * Determines if an experiment is waiting to be executed.
	 * @param e The experiment
	 * @return <tt>true</tt> if the experiment is queued, <tt>false</tt>
	 * otherwise
	 */
	public boolean isQueued(Experiment e)
	{
		if (!m_objects.contains(e))
		{
			return false;
		}
		Experiment.Status s = e.getStatus();
		return s == Status.READY || s == Status.UNINITIALIZED;
	}

	@Override
	public void run()
	{
		m_startTime = System.currentTimeMillis();
		for (Experiment e : m_objects) 
		{
			Future<?> future = m_executor.submit(e, m_condition);
			Millisecond ms = new Millisecond(e.getTimeout());
			if (ms.get().intValue() <= 0)
			{
				// No timeout set, proceed to next experiment
				continue;
			}
			ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(1);
			// Schedule a task to cancel the original task after a timeout
			timeoutExecutor.schedule(() -> {
				// This will interrupt the task if it's running by setting the interrupt flag
				if (!future.isDone()) {
					future.cancel(true);
					System.out.println("Task was cancelled due to timeout");
				}
			}, ms.get().intValue(), TimeUnit.MILLISECONDS);
			timeoutExecutor.shutdown();
		}
		m_executor.shutdown();
		try
		{
			m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		m_endTime = System.currentTimeMillis();
	}

	/**
	 * Waits for the run to stop running before exiting.
	 */
	public void join()
	{
		try
		{
			m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			// Do nothing
		}
	}

	public long getExecutionTime()
	{
		if (m_endTime <= 0)
		{
			return System.currentTimeMillis() - m_startTime;
		}
		return m_endTime - m_startTime;
	}

	public void stop(boolean now)
	{
		try
		{
			if (now)
			{
				m_executor.shutdownNow();
				m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			}
			else
			{
				m_executor.shutdown();
				m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			}
		}
		catch (InterruptedException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		m_endTime = System.currentTimeMillis();
	}

	/**
	 * Shuts down the executor service used to run the experiments in this run.
	 * @param now If set to {@code true}, currently running experiments will be
	 * forcibly interrupted and the executor will be shut down immediately. If
	 * set to {@code false}, the executor will wait for all currently running
	 * experiments to finish before shutting down, but will not allow any new
	 * experiments to start.
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown(boolean now) throws InterruptedException
	{
		if (now)
		{
			m_executor.shutdownNow();
			m_executor.awaitTermination(0, TimeUnit.MILLISECONDS);	
		}
		else
		{
			m_executor.shutdown();
			m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);	
		}	
	}

}
