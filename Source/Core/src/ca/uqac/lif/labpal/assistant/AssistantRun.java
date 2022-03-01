/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.labpal.assistant;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.assistant.Assistant.RunRunnable;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * An object allowing control and monitoring over the execution of a batch
 * of experiments. Assistant runs are objects returned by an {@link Assistant}
 * on a call to {@link Assistant#enqueue(Experiment...) enqueue()}. Using an
 * assistant run, it is possible to:
 * <ul>
 * <li>monitor the progress on the execution of experiments with
 * {@link #getProgression()} and {@link #getExecutionTime()}</li>
 * <li>get the set of currently running experiments with
 * {@link #getRunningExperiments()}</li>
 * <li>cancel the execution of a run using {@link #stop(boolean) stop()} or
 * cancel the queueing of individual experiments in the run with
 * {@link #cancel(Experiment...) cancel()}</li>
 * </ul>
 * Note that an assistant run can only be stopped, but not started. It is the
 * <em>assistant</em>'s job to enqueue and manage the execution of individual
 * runs. Also note that the contents of a run (i.e. the experiments it
 * contains) cannot be modified or reordered. 
 * 
 * @since 3.0
 * 
 * @author Sylvain Hallé
 */
public class AssistantRun implements Stateful, Dependent<Experiment>
{
	/**
	 * A counter for run IDs
	 */
	protected static int s_idCounter = 1;
	
	protected RunRunnable m_runnable;
	
	protected Future<?> m_future;
	
	private final int m_id;
	
	public AssistantRun(RunRunnable runnable, Future<?> future)
	{
		super();
		m_runnable = runnable;
		m_future = future;
		m_id = s_idCounter++;
	}
	
	public AssistantRun cancel(Experiment ... experiments)
	{
		return cancel(Arrays.asList(experiments));
	}
	
	public AssistantRun cancel(List<Experiment> experiments)
	{
		return this;
	}
	
	public long getExecutionTime()
	{
		if (m_runnable.getEndTime() <= 0)
		{
			return System.currentTimeMillis() - m_runnable.getStartTime();
		}
		return m_runnable.getEndTime() - m_runnable.getStartTime();
	}
	
	/**
	 * Determines if an experiment is waiting to be executed.
	 * @param e The experiment
	 * @return <tt>true</tt> if the experiment is queued, <tt>false</tt>
	 * otherwise
	 */
	public boolean isQueued(Experiment e)
	{
		return m_runnable.isQueued(e);
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
		for (Experiment e : m_runnable.getExperiments())
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
	/*@ pure non_null @*/ public Status getStatus()
	{
		return Stateful.getLowestStatus(m_runnable.getExperiments());
	}
	
	/**
	 * Prevents the assistant from starting any new experiment in this run,
	 * and optionally interrupts any currently running experiments.
	 * @param now Set to <tt>true</tt> to immediately interrupt running
	 * experiments; set to <tt>false</tt> to wait for these experiments to
	 * finish.
	 */
	public void stop(boolean now)
	{
		try
		{
			m_runnable.shutdown(now);
		}
		catch (InterruptedException e)
		{
			// Do nothing
		}
	}
	
	/**
	 * Gets the list of experiments concerned by this run.
	 * @return The list of experiments
	 */
	@Override
	/*@ pure non_null @*/ public List<Experiment> dependsOn()
	{
		return m_runnable.getExperiments();
	}
	
	@Override
	public void reset()
	{
		for (Experiment e : m_runnable.getExperiments())
		{
			e.reset();
		}
	}
	
	/**
	 * Checks if this assistant run is still running.
	 * @return <tt>true</tt> if it is running, <tt>false</tt> otherwise
	 */
	public boolean isRunning()
	{
		return !(m_future.isCancelled() || m_future.isDone());
	}
	
	/**
	 * Waits for the run to stop running before exiting.
	 */
	public void join()
	{
		try 
		{
			m_future.get();
		}
		catch (InterruptedException | ExecutionException e) 
		{
			// Do nothing
		}
	}

	/**
	 * Gets the experiments that are currently being run by the assistant.
	 * @return The set of running experiments
	 */
	/*@ non_null @*/ public Set<Experiment> getRunningExperiments()
	{
		return m_runnable.getRunning();
	}
	
	/**
	 * Gets a unique ID string that represents this assistant's run.
	 * @return The ID
	 */
	/*@ non_null @*/ public String getId()
	{
		return Integer.toString(m_id);
	}

}
