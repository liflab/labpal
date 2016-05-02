/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé
  
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
package ca.uqac.lif.parkbench;

import java.util.ArrayList;

import ca.uqac.lif.parkbench.Experiment.Status;

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
	private transient Thread m_experimentThread;
	
	/**
	 * The queue of experiments to run
	 */
	private transient ArrayList<Experiment> m_queue;
	
	/**
	 * The time (in ms) to wait before checking the state of the
	 * thread again
	 */
	private static final transient int s_sleepInterval = 333;

	/**
	 * Creates a new dispatcher
	 */
	public LinearAssistant()
	{
		super();
		m_stop = true;
		m_queue = new ArrayList<Experiment>();
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
		while (!m_stop)
		{
			if (m_queue.isEmpty())
			{
				m_stop = true;
				break;
			}
			Experiment e = m_queue.get(0);
			Status s = e.getStatus();
			if (s != Status.RUNNING && s != Status.DONE && s != Status.FAILED)
			{
				// Experiment not started: start
				m_experimentThread = new Thread(e);
				m_experimentThread.start();					
				while (m_experimentThread.isAlive() && !m_stop)
				{
					try
					{
						// Wait .5 s
						Thread.sleep(s_sleepInterval);
					} 
					catch (InterruptedException e1)
					{
						// If something bad happens, stop the loop
						e1.printStackTrace();
						break;
					}
					Status s1  = e.getStatus();
					if (s1 == Status.DONE || s == Status.FAILED)
						break; // Move on to next experiment
				}
			}
			else
			{
				// Experiment is finished: remove from queue
				m_queue.remove(0);
			}
		}
	}

	@Override
	public synchronized LabAssistant stop()
	{
		m_stop = true;
		m_stopTime = System.currentTimeMillis();
		return this;
	}
	
	@Override
	public synchronized LabAssistant unqueue(Experiment e)
	{
		return unqueue(e.getId());
	}
	
	@Override
	public synchronized LabAssistant unqueue(int id)
	{
		for (int i = 0; i < m_queue.size(); i++)
		{
			if (m_queue.get(i).getId() == id)
			{
				m_queue.remove(i);
				break;
			}
		}
		return this;
	}
	
	@Override
	public synchronized LabAssistant queue(Experiment e)
	{
		m_queue.add(e);
		return this;
	}
	
	@Override
	public synchronized boolean isQueued(Experiment e)
	{
		return m_queue.contains(e);
	}
	
	@Override
	public synchronized boolean isQueued(int id)
	{
		for (int i = 0; i < m_queue.size(); i++)
		{
			if (m_queue.get(i).getId() == id)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public synchronized LabAssistant clear()
	{
		stop();
		m_queue.clear();
		return this;
	}

	@Override
	public synchronized float getTimeEstimate()
	{
		float time = 0f;
		float factor = Laboratory.s_parkMips;
		for (Experiment e : m_queue)
		{
			time += e.getDurationEstimate(factor);
			time += ((float) s_sleepInterval / 1000);
		}
		return time;
	}

	@Override
	public boolean isRunning()
	{
		return !m_stop;
	}

}
