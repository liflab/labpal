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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.Identifiable;
import ca.uqac.lif.labpal.Persistent;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.units.Time;
import ca.uqac.lif.units.si.Second;

/**
 * A runnable object that takes input parameters and produces output parameters
 * according to a lifecycle.
 * 
 * @since 1.0
 * 
 * @author Sylvain Hallé
 */
public class Experiment implements Runnable, Comparable<Experiment>, Stateful, Identifiable, Persistent, Dependent<Experiment>
{
	/**
	 * Resets the ID counter for experiments.
	 */
	public static final void resetCounter()
	{
		s_idCounter = 1;
	}

	/**
	 * A duration of 0 seconds, used as the default timeout value.
	 * @see #m_timeout
	 */
	/*@ non_null @*/ protected static final transient Second s_zeroSeconds = new Second(0);

	/**
	 * A format used to print dates when sending them to a page of the web
	 * interface.
	 */
	/*@ non_null @*/ protected static final transient SimpleDateFormat s_dateFormat = new SimpleDateFormat();

	/**
	 * A counter used to keep track of the last assigned ID over all experiment
	 * instances. 
	 */
	private static transient int s_idCounter = 1;

	/**
	 * The numerical identifier (ID) given to an experiment instance.
	 * @see #getId()
	 * @see Identifiable
	 */
	private int m_id;

	/**
	 * A fraction between 0 and 1 representing the approximate progression of the
	 * execution of the experiment.
	 * @see #getProgression()
	 * @see Stateful
	 */
	private float m_progression;

	/**
	 * A time duration after which the experiment is expected to be interrupted
	 * if it has not done so yet.
	 */
	/*@ non_null @*/ private Time m_timeout;

	private float m_timeRatio;

	private long m_startTime;

	private long m_prereqTime;

	private long m_endTime;

	private boolean m_hasTimedOut;

	/**
	 * The current status of the experiment.
	 * @see #getStatus()
	 * @see Stateful
	 */
	/*@ non_null @*/ private Status m_status;

	/**
	 * A lock to enforce synchronized access to the experiment's status field.
	 */
	/*@ non_null @*/ private transient Lock m_statusLock;

	/**
	 * A lock to enforce synchronized access to the progression member field.
	 */
	/*@ non_null @*/ private transient Lock m_progressionLock;

	/**
	 * A map associating input parameter names to their value.
	 */
	/*@ non_null @*/ private Map<String,Object> m_inputParameters;

	/**
	 * A map associating output parameter names to their value.
	 */
	/*@ non_null @*/ private Map<String,Object> m_outputParameters;

	/**
	 * A lock to enforce synchronized access to the input parameter map.
	 */
	/*@ non_null @*/ private transient Lock m_inputParametersLock;

	/**
	 * A lock to enforce synchronized access to the output parameter map.
	 */
	/*@ non_null @*/ private transient Lock m_outputParametersLock;

	/**
	 * A map associating parameter names to a textual description of what each
	 * parameter represents.
	 */
	/*@ non_null @*/ private transient Map<String,String> m_parameterDescriptions;

	/**
	 * A list of experiments, which are the other instances this experiment
	 * directly depends on.
	 * @see #dependsOn()
	 * @see Dependent
	 */
	/*@ non_null @*/ private List<Experiment> m_dependencies;

	/**
	 * Creates a new empty experiment instance and prepares its internal
	 * state.
	 */
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
		m_dependencies = new ArrayList<Experiment>();
		m_timeRatio = 0;
		m_startTime = 0;
		m_prereqTime = 0;
		m_endTime = 0;
		m_status = Status.UNINITIALIZED;
		m_hasTimedOut = false;
		m_timeout = s_zeroSeconds;
	}

	/**
	 * Creates an experiment by writing all dimensions of a point as its
	 * input parameters.
	 * @param p The point
	 */
	public Experiment(/*@ non_null @*/ Point p)
	{
		this();
		for (String d : p.getDimensions())
		{
			writeInput(d, p.get(d));
		}
	}

	@Override
	/*@ pure non_null @*/ public final List<Experiment> dependsOn()
	{
		return m_dependencies;
	}

	/*@ non_null @*/ public final Experiment dependsOn(Experiment e)
	{
		if (e != null && !m_dependencies.contains(e))
		{
			m_dependencies.add(e);
			Collections.sort(m_dependencies);
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

	@Override
	/*@ pure @*/ public final int getId()
	{
		return m_id;
	}

	/**
	 * Returns a textual description of the experiment. This description is
	 * expected to contain valid HTML markup.
	 * 
	 * @return The description
	 */
	/*@ non_null @*/ public String getDescription()
	{
		return "";
	}

	/**
	 * Retrieves the map of input parameters associated to this experiment.
	 * @return The map between parameter names and parameter values
	 */
	/*@ pure non_null @*/ public Map<String,Object> getInputParameters()
	{
		Map<String,Object> map = new HashMap<String,Object>();
		m_inputParametersLock.lock();
		map.putAll(m_inputParameters);
		m_inputParametersLock.unlock();
		return map;
	}

	/**
	 * Retrieves the map of output parameters generated by this experiment.
	 * @return The map between parameter names and parameter values
	 */
	/*@ pure non_null @*/ public Map<String,Object> getOutputParameters()
	{
		Map<String,Object> map = new HashMap<String,Object>();
		m_outputParametersLock.lock();
		map.putAll(m_outputParameters);
		m_outputParametersLock.unlock();
		return map;
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

	/**
	 * Reads a parameter from the experiment.
	 * @param key The name of the parameter
	 * @return The object corresponding to that parameter's value, or
	 * <tt>null</tt> if no parameter has that name
	 */
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

	public String readString(String key)
	{
		Object o = read(key);
		if (o == null)
		{
			return null;
		}
		return o.toString();
	}

	/**
	 * Sets the time duration after which the experiment is expected to be
	 * interrupted if it has not done so yet. 
	 * @param t The time duration, or <tt>null</tt> to set no timeout
	 * @return This experiment
	 */
	/*@ non_null @*/ public final Experiment setTimeout(/*@ null @*/ Time t)
	{
		if (t == null)
		{
			m_timeout = s_zeroSeconds;
		}
		else
		{
			m_timeout = t;
		}
		return this;
	}

	/**
	 * Gets the system time when the experiment was started. 
	 * @return The system start time, in milliseconds, or 0 if the experiment
	 * was not started
	 * @see System#currentTimeMillis()
	 * @see Stateful
	 */
	/*@ pure @*/ public final long getStartTime()
	{
		return m_startTime;
	}

	/*@ pure non_null @*/ public final String getStartDate()
	{
		return formatDate(m_startTime);
	}

	/**
	 * Gets the system time when the experiment finished being in the
	 * <em>preparing</em> state. 
	 * @return The system time, in milliseconds, or 0 if the experiment
	 * has not exited that state yet
	 * @see System#currentTimeMillis()
	 * @see Stateful
	 */
	/*@ pure @*/ public final long getPrerequisitesTime()
	{
		return m_prereqTime;
	}

	/**
	 * Gets the system time when the experiment ended. 
	 * @return The system end time, in milliseconds, or 0 if the experiment
	 * is not finished
	 * @see System#currentTimeMillis()
	 * @see Stateful
	 */
	/*@ pure @*/ public final long getEndTime()
	{
		return m_endTime;
	}

	/*@ pure non_null @*/ public final String getEndDate()
	{
		return formatDate(m_endTime);
	}

	/*@ non_null @*/ public final Time getTotalDuration()
	{
		if (m_startTime > 0)
		{
			if (m_endTime > 0)
			{
				return new Second((float) (m_endTime - m_startTime) / 1000f);
			}
			else
			{
				return new Second((float) (System.currentTimeMillis() - m_startTime) / 1000f);
			}
		}
		return new Second(0);
	}

	/*@ pure non_null @*/ public final Time getTimeout()
	{
		return m_timeout;
	}

	/**
	 * Retrieves the total number of "data points" contained within this
	 * experiment. What counts as a "data point" is left to the author.
	 * @return The number of points
	 */
	public int countDataPoints()
	{
		return m_outputParameters.size();
	}

	@Override
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

	@SuppressWarnings("unchecked")
	@Override
	public void loadState(Object o_read) throws PersistenceException
	{
		if (!(o_read instanceof Map))
		{
			throw new PersistenceException("Unexpected data structure");
		}
		Map<?,?> map = (Map<?,?>) o_read;
		if (!map.containsKey("id") || !map.containsKey("input") || !map.containsKey("output") || !map.containsKey("start") || !map.containsKey("prereq") || !map.containsKey("end") || !map.containsKey("status") || !map.containsKey("timedout") || !map.containsKey("timeout") || !map.containsKey("progression"))
		{
			throw new PersistenceException("Unexpected data structure");
		}
		Object o_in = map.get("input");
		Object o_out = map.get("output");
		if (!(o_in instanceof Map) || !(o_out instanceof Map))
		{
			throw new PersistenceException("Unexpected data structure");
		}
		m_id = ((Number) map.get("id")).intValue();
		m_inputParameters = (Map<String,Object>) o_in;
		m_outputParameters = (Map<String,Object>) o_out;
		m_startTime = ((Number) map.get("start")).longValue();
		m_prereqTime = ((Number) map.get("prereq")).longValue();
		m_endTime = ((Number) map.get("end")).longValue();
		m_progression = ((Number) map.get("progression")).floatValue();
		m_status = Stateful.getStatus((String) map.get("status"));
		m_hasTimedOut = (Boolean) map.get("timedout");
		m_timeout = (Time) map.get("timeout");
	}

	@Override
	public Map<String,Object> saveState() throws PersistenceException
	{
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", m_id);
		params.put("input", m_inputParameters);
		params.put("output", m_outputParameters);
		params.put("status", getStatus().toString());
		params.put("start", m_startTime);
		params.put("prereq", m_prereqTime);
		params.put("end", m_endTime);
		params.put("timedout", m_hasTimedOut);
		params.put("timeout", m_timeout);
		params.put("progression", m_progression);
		return params;
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
					setStatus(Status.PREPARING);
					fulfillPrerequisites();
				}
				catch (ExperimentException e)
				{
					setStatus(Status.FAILED);
					return;
				}
				catch (InterruptedException e)
				{
					setStatus(Status.INTERRUPTED);
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
			setStatus(Status.INTERRUPTED);
			success = false;
		}
		m_endTime = System.currentTimeMillis();
		if (success && m_status == Status.RUNNING)
		{
			// Can only move to Done from Running (traps the case where the thread
			// set it to Timeout or Cancelled)
			setStatus(Status.DONE);
			m_progressionLock.lock();
			m_progression = 1; // Force progression to 100%
			m_progressionLock.unlock();
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

	@Override
	public final void reset()
	{
		if (prerequisitesFulfilled())
		{
			setStatus(Status.READY);
		}
		else
		{
			setStatus(Status.UNINITIALIZED);
			setProgression(0);
		}
		m_outputParameters.clear();
		m_startTime = 0;
		m_prereqTime = 0;
		m_endTime = 0;
		m_hasTimedOut = false;
		handleReset();
	}

	/**
	 * Resets the internal state of an experiment upon a call to
	 * {@link #reset()}. This method can be overridden so that a custom
	 * experiment cleans up the member fields that are not inherited from
	 * the {@link Experiment} class, if necessary. By default, all
	 * this method does is to set the progression to 0.
	 */
	protected void handleReset()
	{
		setProgression(0);
	}

	/**
	 * Tells the experiment that it has ended in a timeout. Since timeouts are
	 * handled by the thread that runs the experiment, this notification has to
	 * come from the outside.
	 */
	public final void declareTimeout()
	{
		m_hasTimedOut = true;
	}

	/**
	 * Determines if the experiment has been declared a timeout.
	 * @return <tt>true</tt> if the experiment timed out, <tt>false</tt>
	 * otherwise
	 */
	/*@ pure @*/ public final boolean hasTimedOut()
	{
		return m_hasTimedOut;
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

	@Override
	public int compareTo(Experiment e)
	{
		return m_id - e.getId();
	}

	@Override
	public int hashCode()
	{
		return m_id;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Experiment))
		{
			return false;
		}
		return ((Experiment) o).getId() == m_id;
	}

	/**
	 * Determines if an experiment is currently in one of its possible
	 * final states.
	 * @return <tt>true</tt> if the experiment is in a final state,
	 * <tt>false</tt> otherwise
	 */
	/*@ pure @*/ protected final boolean isFinished()
	{
		Status s = getStatus();
		return s == Status.DONE || s == Status.INTERRUPTED || s == Status.FAILED;
	}

	/**
	 * Formats the date
	 * 
	 * @param timestamp
	 *          A Unix timestamp
	 * @return A formatted date
	 */
	protected static String formatDate(long timestamp)
	{
		if (timestamp <= 0)
		{
			return "-";
		}
		return s_dateFormat.format(new Date(timestamp));
	}
}
