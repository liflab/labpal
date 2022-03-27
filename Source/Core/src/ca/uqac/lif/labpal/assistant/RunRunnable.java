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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.claim.Condition;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * A {@link Runnable} object in charge of coordinating the execution of an
 * assistant run.
 * @author Sylvain Hallé
 */
class RunRunnable implements Runnable
{
	protected long m_startTime;

	protected long m_endTime;

	protected List<Experiment> m_experiments;

	protected LabPalExecutorService m_executor;
	
	/**
	 * An optional condition used to determine if each experiment in the run
	 * should be executed.
	 */
	/*@ null @*/ protected Condition m_condition;

	public RunRunnable(/*@ non_null @*/ List<Experiment> experiments, /*@ non_null @*/ LabPalExecutorService executor, /*@ null @*/ Condition c)
	{
		super();
		m_experiments = experiments;
		m_executor = executor;
		m_condition = c;
		m_startTime = -1;
		m_endTime = -1;
	}

	/**
	 * Determines if an experiment is waiting to be executed.
	 * @param e The experiment
	 * @return <tt>true</tt> if the experiment is queued, <tt>false</tt>
	 * otherwise
	 */
	public boolean isQueued(Experiment e)
	{
		if (!m_experiments.contains(e))
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
		for (Experiment e : m_experiments) 
		{
			m_executor.submit(e, m_condition);
		}
		m_executor.shutdownAtEnd();
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

	public long getStartTime()
	{
		return m_startTime;
	}

	public long getEndTime()
	{
		return m_endTime;
	}

	public Set<Experiment> getRunning()
	{
		Set<Experiment> running = new HashSet<Experiment>();
		return running;
	}

	/**
	 * Gets the list of experiments that are included in this run.
	 * @return The list of experiments
	 */
	/*@ pure non_null @*/ public List<Experiment> getExperiments()
	{
		return m_experiments;
	}
}