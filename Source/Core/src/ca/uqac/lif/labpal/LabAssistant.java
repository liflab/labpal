/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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
package ca.uqac.lif.labpal;

import java.util.List;

/**
 * A lab assistant is in charge of running a set of experiments according
 * to a queue.
 * <p>
 * Typically, you add new experiments with {@link #queue(Experiment)} and
 * remove them from the queue with {@link #unqueue(Experiment)}. You can
 * instruct the lab assistant to start running the experiments in its queue
 * with {@link #run()}, and can interrupt its operation with {@link #stop()}.
 * Calling {@link #clear()} will clear the current queue.
 * 
 * @author Sylvain Hallé
 */
public abstract class LabAssistant implements Runnable
{
	/**
	 * The assistant's name
	 */
	protected String m_name;
	
	/**
	 * The moment the assistant was started for the last time
	 */
	protected long m_startTime = -1;
	
	/**
	 * The moment the assistant was stopped for the last time
	 */
	protected long m_stopTime = -1;
	
	/**
	 * The total time the assistant has been running since the program was
	 * started (in milliseconds)
	 */
	protected long m_runningTime = 0;
	
	/**
	 * Creates a new lab assistant
	 */
	public LabAssistant()
	{
		super();
		m_name = "George Jetson";
	}
	
	/**
	 * Gets the total time the assistant has been running since the program
	 * was started (in milliseconds)
	 * @return The time
	 */
	public long getRunningTime()
	{
		long time = m_runningTime;
		if (isRunning())
		{
			long duration = System.currentTimeMillis() - getStartTime();
			time += duration;
		}
		return time;
	}
	
	/**
	 * Removes an experiment from the queue
	 * @param e The experiment
	 * @return This assistant
	 */
	public abstract LabAssistant unqueue(Experiment e);
	
	/**
	 * Removes an experiment from the queue
	 * @param id The experiment's ID
	 * @return This assistant
	 */
	public abstract LabAssistant unqueue(int id);
	
	/**
	 * Adds an experiment at the end of the queue
	 * @param e The experiment
	 * @return This assistant
	 */
	public abstract LabAssistant queue(Experiment e);
	
	/**
	 * Checks whether an experiment is queued
	 * @param e The experiment
	 * @return True if the experiment is waiting in the queue, false otherwise
	 */
	public abstract boolean isQueued(Experiment e);
	
	/**
	 * Checks whether an experiment is queued
	 * @param id The id of the experiment
	 * @return True if the experiment is waiting in the queue, false otherwise
	 */
	public abstract boolean isQueued(int id);
	
	/**
	 * Gets the list of experiments that are currently queued
	 * @return The list of experiment IDs
	 */
	public abstract List<Integer> getCurrentQueue(); 
	
	/**
	 * Interrupts the execution of the current experiment. If experiments
	 * are still in the queue, the assistant does not execute them
	 * @return This assistant
	 */
	public abstract LabAssistant stop();
	
	/**
	 * Gives a name to this lab assistant
	 * @param name The name
	 * @return This assistant
	 */
	public final LabAssistant setName(String name)
	{
		m_name = name;
		return this;
	}
	
	/**
	 * Gets the name of this lab assistant
	 * @return The name
	 */
	public final String getName()
	{
		return m_name;
	}
	
	/**
	 * Gets the last time the assistant was started
	 * @return The time, in milliseconds
	 */
	public final long getStartTime()
	{
		return m_startTime;
	}
	
	/**
	 * Gets the last time the assistant was stopped. Note that
	 * if the assistant is running, this moment comes before the last start
	 * time.
	 * @return The time, in milliseconds
	 */
	public final long getStopTime()
	{
		return m_stopTime;
	}
	
	/**
	 * Gives an estimate of the time it should take to complete all the
	 * experiments still in the queue. It is up to each assistant to
	 * decide how to compute this.
	 * 
	 * @return The estimated time, in seconds
	 */
	public abstract float getTimeEstimate();
	
	/**
	 * Clears the experiment queue for this assistant. Note that this also
	 * has for effect of stopping the assistant in case it is running.
	 * @return This assistant
	 */
	public abstract LabAssistant clear();
	
	/**
	 * Determines if the assistant is currently doing experiments
	 * @return true if the assistant is running, false otherwise
	 */
	public abstract boolean isRunning();
}
