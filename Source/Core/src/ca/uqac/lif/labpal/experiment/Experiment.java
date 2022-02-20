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
package ca.uqac.lif.labpal.experiment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A runnable object that takes input parameters and produces output parameters
 * according to a lifecycle.
 * 
 * @since 1.0
 * 
 * @author Sylvain Hallé
 */
public class Experiment implements Runnable
{
	public enum Status {UNINITIALIZED, RUNNING, RUNNING_PREREQ, READY, DONE, TIMEOUT, CANCELLED, FAILED}

	private static transient int s_idCounter = 0;

	private int m_id;

	private float m_progression;

	private long m_timeout;

	private float m_timeRatio;

	private long m_startTime;

	private long m_prereqTime;

	private long m_endTime;

	/*@ non_null @*/ private Status m_status;

	/*@ non_null @*/ private transient Lock m_statusLock;

	/*@ non_null @*/ private transient Lock m_progressionLock;

	/*@ non_null @*/ private Map<String,Object> m_inputParameters;

	/*@ non_null @*/ private Map<String,Object> m_outputParameters;

	/*@ non_null @*/ private transient Lock m_inputParametersLock;

	/*@ non_null @*/ private transient Lock m_outputParametersLock;

	/*@ non_null @*/ private transient Map<String,String> m_parameterDescriptions;

	/*@ non_null @*/ private Set<Integer> m_dependencies;

	public Experiment()
	{
		super();
		m_id = s_idCounter++;
		m_progressionLock = new ReentrantLock();
		m_statusLock = new ReentrantLock();
		m_inputParametersLock = new ReentrantLock();
		m_outputParametersLock = new ReentrantLock();
		m_inputParameters = new HashMap<String,Object>();
		m_outputParameters = new HashMap<String,Object>();
		m_parameterDescriptions = new HashMap<String,String>();
		m_dependencies = new HashSet<Integer>();
		m_timeRatio = 0;
		m_startTime = 0;
		m_prereqTime = 0;
		m_endTime = 0;
		m_status = Status.UNINITIALIZED;
	}

	/*@ pure non_null @*/ public final Set<Integer> dependsOn()
	{
		return m_dependencies;
	}

	/*@ non_null @*/ public final Experiment dependsOn(int id)
	{
		m_dependencies.add(id);
		return this;
	}

	/*@ non_null @*/ public final Experiment dependsOn(Experiment e)
	{
		if (e != null)
		{
			m_dependencies.add(e.getId());
		}
		return this;
	}

	/*@ non_null @*/ public final Experiment setTimeRatio(float ratio)
	{
		m_timeRatio = ratio;
		return this;
	}

	/*@ pure @*/ public final float getTimeRatio()
	{
		return m_timeRatio;
	}

	/*@ pure @*/ public final int getId()
	{
		return m_id;
	}

	/*@ non_null @*/ public final Experiment writeInput(String key, Object value)
	{
		m_inputParametersLock.lock();
		m_inputParameters.put(key, value);
		m_inputParametersLock.unlock();
		return this;
	}

	/*@ non_null @*/ public final Experiment writeOutput(String key, Object value)
	{
		m_outputParametersLock.lock();
		m_outputParameters.put(key, value);
		m_outputParametersLock.unlock();
		return this;
	}

	/*@ non_null @*/ public final Experiment setDescription(String key, String description)
	{
		m_parameterDescriptions.put(key, description);
		return this;
	}

	/*@ non_null @*/ public final String getDescription(String key)
	{
		if (m_parameterDescriptions.containsKey(key))
		{
			return m_parameterDescriptions.get(key);
		}
		return "";
	}

	/*@ null @*/ public final Object read(String key)
	{
		boolean found = false;
		Object o = null;
		m_inputParametersLock.lock();
		if (m_inputParameters.containsKey(key))
		{
			found = true;
			o = m_inputParameters.get(key);
		}
		m_inputParametersLock.unlock();
		if (!found)
		{
			m_outputParametersLock.lock();
			if (m_outputParameters.containsKey(key))
			{
				o = m_outputParameters.get(key);
			}
			m_outputParametersLock.unlock();
		}
		return o;
	}

	/*@ non_null @*/ public final Experiment setTimeout(long timeout)
	{
		m_timeout = timeout;
		return this;
	}

	/*@ pure @*/ public final long getStartTime()
	{
		return m_startTime;
	}

	/*@ pure @*/ public final long getPrerequisitesTime()
	{
		return m_prereqTime;
	}

	/*@ pure @*/ public final long getEndTime()
	{
		return m_endTime;
	}

	public final long getTotalDuration()
	{
		if (m_startTime > 0 && m_endTime > 0)
		{
			return m_endTime - m_startTime;
		}
		return 0;
	}

	public final long getTimeout()
	{
		return m_timeout;
	}

	public final float getProgression()
	{
		float p = 0;
		m_progressionLock.lock();
		p = m_progression;
		m_progressionLock.unlock();
		return p;
	}

	/*@ non_null @*/ protected final Experiment setProgression(float x)
	{
		m_progressionLock.lock();
		m_progression = x;
		m_progressionLock.unlock();
		return this;
	}

	@Override
	public final void run()
	{
		if (isFinished())
		{
			// Experiment has already completed, do not run again
			return;
		}
		m_startTime = System.currentTimeMillis();
		if (m_status == Status.UNINITIALIZED)
		{
			if (!prerequisitesFulfilled())
			{
				try
				{
					setStatus(Status.RUNNING_PREREQ);
					fulfillPrerequisites();
				}
				catch (ExperimentException e)
				{
					setStatus(Status.FAILED);
					return;
				}
				catch (InterruptedException e)
				{
					setStatus(Status.CANCELLED);
					return;
				}
			}
			m_prereqTime = System.currentTimeMillis();
			setStatus(Status.READY);
		}
		else
		{
			// If no prerequisites are handled, force this lap time to be equal
			// to the start time (avoids meaningless delays of a few ms)
			m_prereqTime = m_startTime;
		}
		boolean success = true;
		try
		{
			setStatus(Status.RUNNING);
			execute();
		}
		catch (ExperimentException e)
		{
			setStatus(Status.FAILED);
			success = false;
			return;
		}
		catch (InterruptedException e)
		{
			//System.out.println("Interrupted");
			setStatus(Status.CANCELLED);
			success = false;
		}
		m_endTime = System.currentTimeMillis();
		if (success && m_status == Status.RUNNING)
		{
			// Can only move to Done from Running (traps the case where the thread
			// set it to Timeout or Cancelled)
			setStatus(Status.DONE);
		}
	}

	/*@ pure non_null @*/ public final Status getStatus()
	{
		Status s;
		m_statusLock.lock();
		s = m_status;
		m_statusLock.unlock();
		if (s == Status.UNINITIALIZED && prerequisitesFulfilled())
		{
			setStatus(Status.READY);
			return Status.READY;
		}
		return s;
	}

	/*@ pure non_null @*/ public final Experiment setStatus(Status s)
	{
		m_statusLock.lock();
		m_status = s;
		m_statusLock.unlock();
		return this;
	}
	
	public final void reset()
	{
		setStatus(Status.UNINITIALIZED);
		m_outputParameters.clear();
		m_startTime = -1;
		m_prereqTime = -1;
		m_endTime = -1;
	}

	public void execute() throws ExperimentException, InterruptedException
	{
		// Nothing to do
	}

	public boolean prerequisitesFulfilled()
	{
		return true;
	}

	public void fulfillPrerequisites() throws ExperimentException, InterruptedException
	{
		// Nothing to do
	}

	public void cleanPrerequisites()
	{
		// Nothing to do
	}

	/*@ pure @*/ protected final boolean isFinished()
	{
		Status s = getStatus();
		return s == Status.DONE || s == Status.TIMEOUT || s == Status.CANCELLED || s == Status.FAILED;
	}	
}
