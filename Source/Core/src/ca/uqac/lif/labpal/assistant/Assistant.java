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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentGroup;

/**
 * Coordinates the execution of batches of experiments from a given laboratory.
 * @since 2.0
 * @author Sylvain Hallé
 *
 */
public class Assistant
{
	/**
	 * The executor used to execute runs sequentially.
	 */
	/*@ non_null @*/ protected transient ExecutorService m_runExecutor;
	
	/**
	 * The executor used to execute experiments in each run.
	 */
	protected transient LabPalExecutorService m_executor;

	/**
	 * The list of runs produced by the assistant.
	 */
	/*@ non_null @*/ protected final transient List<AssistantRun> m_runs;
	
	/**
	 * A queue of experiments that have not yet been committed to a run.
	 */
	/*@ non_null @*/ protected final transient List<Experiment> m_queue;
	
	/**
	 * An ID given to the temporary experiment group corresponding to the
	 * assistant's queue. We deliberately use a large number that is unlikely
	 * to belong to an existing group in the lab.
	 */
	protected static final transient int s_queueGroupId = 999;

	/**
	 * Creates a new assistant with default settings.
	 */
	public Assistant()
	{
		super();
		m_runExecutor = Executors.newSingleThreadExecutor();
		m_runs = new ArrayList<AssistantRun>();
		m_queue = new ArrayList<Experiment>();
	}
	
	/**
	 * Creates a new assistant with default settings and sets its
	 * run executor.
	 * @param service The LabPal executor service used to run
	 * experiments
	 */
	public Assistant(/*@ non_null @*/ LabPalExecutorService service)
	{
		this();
		m_executor = service;
	}
	
	/**
	 * Produces a string identifying this assistant.
	 * @return The assistant's name
	 */
	/*@ pure non_null @*/ public String getName()
	{
		return m_executor.toString();
	}
	
	/**
	 * Gets the contents of the assistant's queue.
	 * @return The queue
	 */
	/*@ pure non_null @*/ public List<Experiment> getQueue()
	{
		return m_queue;
	}
	
	/**
	 * Adds a collection of experiments to the assistant's queue. If an
	 * experiment in the collection is already present in the queue, it is not
	 * added another time.
	 * @param experiments The experiments to add
	 * @return The number of experiments actually added to the queue
	 */
	/*@ non_null @*/ public int addToQueue(Collection<Experiment> experiments)
	{
		int added = 0;
		for (Experiment e : experiments)
		{
			if (!m_queue.contains(e))
			{
				m_queue.add(e);
				added++;
			}
		}
		return added;
	}
	
	/**
	 * Removes a collection of experiments from the assistant's queue.
	 * @param experiments The experiments to removed
	 * @return The number of experiments actually removed to the queue
	 */
	/*@ non_null @*/ public int removeFromQueue(Collection<Experiment> experiments)
	{
		int removed = 0;
		for (Experiment e : experiments)
		{
			if (m_queue.contains(e))
			{
				m_queue.remove(e);
				removed++;
			}
		}
		return removed;
	}
	
	/**
	 * Gets the contents of the assistant's queue, exposed as an experiment
	 * group.
	 * @return The group
	 */
	/*@ pure non_null @*/ public ExperimentGroup getQueueAsGroup()
	{
		ExperimentGroup g = new ExperimentGroup("");
		g.setId(s_queueGroupId);
		g.add(m_queue);
		return g;
	}

	/**
	 * Adds experiments to be eventually executed in the next run of the
	 * assistant.
	 * @param experiments The experiments to add
	 * @return The run corresponding to these experiments
	 */
	/*@ non_null @*/ public AssistantRun enqueue(Experiment ... experiments)
	{
		if (experiments == null || experiments.length == 0)
		{
			AssistantRun run = enqueue(m_queue);
			m_queue.clear();
			return run;
		}
		List<Experiment> ordered_list = Arrays.asList(experiments);
		RunRunnable rr = new RunRunnable(ordered_list, getExecutor());
		Future<?> future = m_runExecutor.submit(rr);
		AssistantRun run = new AssistantRun(rr, future);
		m_runs.add(run);
		return run;
	}	
	
	/**
	 * Adds experiments to be eventually executed in the next run of the
	 * assistant from the assistant's current queue.
	 * @return The run corresponding to these experiments
	 */
	/*@ non_null @*/ public AssistantRun enqueueCurrent()
	{
		AssistantRun run = enqueue(m_queue);
		m_queue.clear();
		return run;
	}

	/**
	 * Adds experiments to be eventually executed in the next run of the
	 * assistant.
	 * @param experiments The experiments to add
	 * @param The run instance corresponding to the execution of these
	 * experiments
	 * @return The run corresponding to these experiments
	 */
	/*@ non_null @*/ public AssistantRun enqueue(/*@ non_null @*/ Collection<Experiment> experiments)
	{
		List<Experiment> ordered_list = new ArrayList<Experiment>(experiments);
		RunRunnable rr = new RunRunnable(ordered_list, getExecutor());
		Future<?> future = m_runExecutor.submit(rr);
		AssistantRun run = new AssistantRun(rr, future);
		m_runs.add(run);
		return run;
	}
	
	/**
	 * Adds experiments to be eventually executed in the next run of the
	 * assistant.
	 * @param selector A list of experiment selectors
	 * @param The run instance corresponding to the execution of these
	 * experiments
	 */
	/*@ non_null @*/ public AssistantRun enqueue(ExperimentSelector ... selectors)
	{
		Set<Experiment> exps = new HashSet<Experiment>();
		for (ExperimentSelector sel : selectors)
		{
			exps.addAll(sel.select());
		}
		return enqueue(exps);
	}

	/**
	 * Sets the executor service that will be used to execute each run.
	 * @param p The provider, or <tt>null</tt> for a simple single-thread
	 * sequential executor.
	 * @return This assistant
	 */
	/*@ non_null @*/ public Assistant setExecutor(/*@ null @*/ LabPalExecutorService s)
	{
		m_executor = s;
		return this;
	}

	/**
	 * Gets the list of assistant runs generated by this assistant so far. This
	 * includes the runs that have terminated, the runs that are currently
	 * alive, and the runs that are scheduled for the future. Note that the
	 * list is intended for monitoring purposes only; modifying its contents
	 * (e.g. deleting or adding elements) has no effect on the actual operation
	 * of the assistant.
	 * @return The list of runs, in the order they have been generated by the
	 * assistant.
	 */
	/*@ pure non_null @*/ public List<AssistantRun> getRuns()
	{
		List<AssistantRun> runs = new ArrayList<AssistantRun>();
		runs.addAll(m_runs);
		return runs;
	}
	
	/**
	 * Gets the run with given ID, if it exists.
	 * @param id The ID
	 * @return The run, or <tt>null</tt> if no run with such ID exists
	 */
	/*@ pure null @*/ public AssistantRun getRun(int id) 
	{
		for (AssistantRun run : m_runs)
		{
			if (run.getId() == id)
			{
				return run;
			}
		}
		return null;
	}
	
	/**
	 * Interrupts and removes an assistant run.
	 * @param id The ID
	 * @return <tt>true</tt> if a run with such ID existed and was deleted,
	 * <tt>false</tt> otherwise
	 */
	public boolean deleteRun(int id)
	{
		boolean deleted = false;
		for (AssistantRun run : m_runs)
		{
			if (run.getId() == id)
			{
				run.stop(true);
				m_runs.remove(run);
				deleted = true;
				break;
			}
		}
		return deleted;
	}

	/**
	 * Gets the cumulative time this assistant has spent running experiments.
	 * This corresponds to "wall clock" time, and not cumulative time over
	 * multiple threads (in the case where the assistant runs multiple
	 * experiments in separate threads).
	 * @return The execution time in milliseconds
	 */
	public long getExecutionTime()
	{
		long time = 0;
		for (AssistantRun r : m_runs)
		{
			time += r.getExecutionTime();
		}
		return time;
	}
	
	/**
	 * Determines if an experiment is waiting to be executed.
	 * @param e The experiment
	 * @return <tt>true</tt> if the experiment is queued, <tt>false</tt>
	 * otherwise
	 */
	public boolean isQueued(Experiment e)
	{
		for (AssistantRun run : m_runs)
		{
			if (run.isQueued(e))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Asks a scheduler to reorder the list of experiments to be executed by
	 * the assistant.
	 * @param scheduler The scheduler that will reorder the experiments
	 * @return This assistant
	 */
	/*@ non_null @*/ public Assistant apply(/*@ non_null @*/ ExperimentScheduler scheduler)
	{
		List<Experiment> new_queue = scheduler.schedule(m_queue);
		m_queue.clear();
		m_queue.addAll(new_queue);
		return this;

	}
	
	/*@ non_null @*/ protected LabPalExecutorService getExecutor()
	{
		if (m_executor == null)
		{
			return new SingleThreadExecutor();
		}
		return m_executor.newInstance();
	}
}
