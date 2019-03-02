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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.labpal.Experiment.QueueStatus;
import ca.uqac.lif.labpal.Experiment.Status;

/**
 * Lab assistant that executes the experiments one by one, in the order
 * they have been put in its queue.
 * 
 * @author Sylvain Hallé
 */
public class LinearAssistant extends LabAssistant
{
	/**
	 * Internal flag used to stop the execution of the queue
	 */
	private transient boolean m_stop;

	/**
	 * The thread that runs the experiments
	 */
	private transient ExperimentThread m_experimentThread;
	
	/**
	 * The queue of experiments to run
	 */
	private transient ArrayList<Experiment> m_queue;
	
	/**
	 * A lock to control concurrent accesses to the queue
	 */
	private transient final Lock m_queueLock = new ReentrantLock();
	
	/**
	 * The time (in ms) to wait before checking the state of the
	 * thread again
	 */
	private transient int m_sleepInterval = 100;

	/**
	 * Creates a new assistant
	 */
	public LinearAssistant(Laboratory lab)
	{
		super(lab);
		m_stop = true;
		m_queue = new ArrayList<Experiment>();
	}
	
	/**
	 * Creates a new assistant
	 */
	public LinearAssistant()
	{
		this(null);
	}
	
	/**
	 * Sets the time (in ms) to wait before checking the state of the
	 * thread again
	 * @param interval The time
	 */
	public void setSleepInterval(int interval)
	{
		m_sleepInterval = interval;
	}

	@Override
	public void run()
	{
		m_startTime = System.currentTimeMillis();
		if (m_experimentThread != null && m_experimentThread.isAlive())
		{
			// If for some reason another thread is still running, interrupt it
			m_experimentThread.interrupt();
		}
		m_stop = false;
		m_lab.getReporter().start();
		while (!m_stop)
		{
			m_queueLock.lock();
			if (m_queue.isEmpty())
			{
				m_stop = true;
				m_stopTime = System.currentTimeMillis();
				if (m_startTime > 0)
				{
					m_runningTime += m_stopTime - m_startTime;
				}
				m_startTime = -1;
				m_queueLock.unlock();
				break;
			}
			Experiment e = m_queue.get(0);
			m_queueLock.unlock();
			Status s = e.getStatus();
			if (s != Status.RUNNING && s != Status.DONE && s != Status.DONE_WARNING && s != Status.FAILED)
			{
				// Experiment not started: start
				m_experimentThread = new ExperimentThread(e);
				e.setWhoRan(m_name);
				m_experimentThread.start();					
				while (m_experimentThread.isAlive() && !m_stop)
				{
					try
					{
						// Wait .5 s
						Thread.sleep(m_sleepInterval);
					} 
					catch (InterruptedException e1)
					{
						// If something bad happens, stop the loop
						e1.printStackTrace();
						break;
					}
					Status s1  = e.getStatus();
					if (s1 == Status.DONE || s == Status.FAILED || s1 == Status.DONE_WARNING)
						break; // Move on to next experiment
					long duration = System.currentTimeMillis() - e.getStartTime();
					long max_duration = e.getMaxDuration();
					if (max_duration > 0 && duration > max_duration)
					{
						// Experiment takes too long: kill it
						m_experimentThread.kill();
						m_queueLock.lock();
						m_queue.remove(0);
						m_queueLock.unlock();
						m_experimentThread = null;
						break;
					}
				}
			}
			else
			{
				// Experiment is finished: remove from queue
				m_queueLock.lock();
				m_queue.remove(0);
				m_queueLock.unlock();
			}
		}
		// If some experiment is running, interrupt it
		if (m_experimentThread != null && m_experimentThread.isAlive())
		{
			m_experimentThread.interrupt();
		}
		m_lab.getReporter().stop();
	}

	@Override
	public LabAssistant stop()
	{
		m_stop = true;
		m_stopTime = System.currentTimeMillis();
		if (m_startTime > 0)
		{
			m_runningTime += m_stopTime - m_startTime;
		}
		m_startTime = -1;
		return this;
	}
	
	@Override
	public LabAssistant unqueue(Experiment e)
	{
		return unqueue(e.getId());
	}
	
	@Override
	public LabAssistant unqueue(int id)
	{
		m_queueLock.lock();
		for (int i = 0; i < m_queue.size(); i++)
		{
			if (m_queue.get(i).getId() == id)
			{
				m_queue.get(i).setQueueStatus(QueueStatus.NOT_QUEUED);
				m_queue.remove(i);
				break;
			}
		}
		m_queueLock.unlock();
		reportResults();
		return this;
	}
	
	@Override
	public LabAssistant queue(Experiment e)
	{
		m_queueLock.lock();
		m_queue.add(e);
		m_queueLock.unlock();
		e.setWhoRan(m_name);
		e.setQueueStatus(QueueStatus.QUEUED);
		reportResults();
		return this;
	}
	
	@Override
	public boolean isQueued(Experiment e)
	{
		m_queueLock.lock();
		boolean b = m_queue.contains(e);
		m_queueLock.unlock();
		return b;
	}
	
	@Override
	public boolean isQueued(int id)
	{
		boolean b = false;
		m_queueLock.lock();
		for (int i = 0; i < m_queue.size(); i++)
		{
			if (m_queue.get(i).getId() == id)
			{
				b = true;
				break;
			}
		}
		m_queueLock.unlock();
		return b;
	}
	
	@Override
	public LabAssistant clear()
	{
		stop();
		m_queueLock.lock();
		m_queue.clear();
		m_queueLock.unlock();
		return this;
	}

	@Override
	public float getTimeEstimate()
	{
		float time = 0f;
		float factor = Laboratory.s_parkMips;
		m_queueLock.lock();
		for (Experiment e : m_queue)
		{
			time += e.getDurationEstimate(factor);
			time += ((float) m_sleepInterval / 1000);
		}
		m_queueLock.unlock();
		return time;
	}

	@Override
	public boolean isRunning()
	{
		return !m_stop;
	}

	@Override
	public List<Integer> getCurrentQueue()
	{
		m_queueLock.lock();
		List<Integer> out = new LinkedList<Integer>();
		for (Experiment e : m_queue)
		{
			out.add(e.getId());
		}
		m_queueLock.unlock();
		return out;
	}
	
	@Override
	public Set<Experiment> getRunningExperiments()
	{
	  Set<Experiment> set = new HashSet<Experiment>();
	  if (m_experimentThread != null)
	  {
	    set.add(m_experimentThread.m_experiment);
	  }
	  return set;
	}
}
