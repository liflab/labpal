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

import ca.uqac.lif.labpal.assistant.Assistant.RunRunnable;
import ca.uqac.lif.labpal.experiment.Experiment;

public class AssistantRun 
{
	/**
	 * A counter for run IDs
	 */
	protected static int s_idCounter = 0;
	
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
