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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonPath;
import ca.uqac.lif.json.JsonString;

/**
 * An experiment is a runnable process that takes input parameters and
 * produces output parameters.
 * 
 * @author Sylvain Hallé
 */
public abstract class Experiment implements Runnable
{
	/**
	 * The status of the experiment
	 */
	public static enum Status {DUNNO, PREREQ_NOK, PREREQ_OK, PREREQ_F, RUNNING, DONE, FAILED};

	/**
	 * The input parameters given to this experiment
	 */
	private JsonMap m_inputParameters;
	
	/**
	 * The output parameters that this experiment generates
	 */
	private JsonMap m_outputParameters;
	
	/**
	 * The current status of the experiment
	 */
	private Status m_status;
	
	/**
	 * A numerical value that uniquely identifies each experiment in a lab
	 */
	private int m_id;
	
	/**
	 * Association of experiment parameters with a short textual description 
	 */
	private transient Map<String,String> m_parameterDescriptions;
	
	/**
	 * The start time of the experiment
	 */
	private long m_startTime = -1;

	/**
	 * The end time of the experiment
	 */
	private long m_endTime = -1;
	
	/**
	 * If the experiment fails, the error message associated with the failure
	 */
	private String m_errorMessage;
	
	/**
	 * A short description for this experiment
	 */
	private String m_description = "";
	
	/**
	 * The name of the lab assistant that ran the experiment (if any)
	 */
	private String m_runBy;
	
	public Experiment()
	{
		super();
		m_inputParameters = new JsonMap();
		m_outputParameters = new JsonMap();
		m_parameterDescriptions = new HashMap<String,String>();
		m_runBy = "";
		m_status = Status.DUNNO;
		m_errorMessage = "";
	}
	
	public Experiment(JsonMap params)
	{
		this();
		m_inputParameters = params;
	}
	
	/**
	 * Sets the experiment's ID
	 * @param id The ID
	 * @return This experiment
	 */
	public Experiment setId(int id)
	{
		m_id = id;
		return this;
	}
	
	/**
	 * Checks if the prerequisites for running this experiment are currently
	 * fulfilled. Override this method if your experiment must do some form of
	 * setup before starting (e.g. generating files, etc.). Do <em>not</em>
	 * satisfy the prerequisites here: rather use
	 * {@link #fulfillPrerequisites(JsonMap)}.
	 * 
	 * @return true if the prerequisites are fulfilled, false otherwise
	 */
	public boolean prerequisitesFulfilled()
	{
		return true;
	}
	
	/**
	 * Generates the prerequisites for running this experiment.
	 * Override this method if your experiment must do some form of
	 * setup before starting (e.g. generating files, etc.). Obviously, there
	 * should be some form of coherence between this method and 
	 * {@link #prerequisitesFulfilled(JsonMap)}.
	 * 
	 * @return true if the prerequisites have been successfully generated,
	 *   false otherwise
	 */
	public boolean fulfillPrerequisites()
	{
		return true;
	}
	
	/**
	 * Cleans any prerequisites this experiment may have generated.
	 * For example: deleting files that were generated, etc.
	 * @see {@link #fulfillPrerequisites(JsonMap)}
	 */
	public void cleanPrerequisites()
	{
		return;
	}
	
	/**
	 * Sets the description for this experiment
	 * @param d The description
	 * @return This experiment
	 */
	public final Experiment setDescription(String d)
	{
		if (d != null)
			m_description = d;
		return this;
	}
	
	/**
	 * Gets the description for this experiment
	 * @return The description
	 */
	public final String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Executes the experiment.
	 * @return The status of the experiment once it has finished. This should
	 *   normally be either <tt>DONE</tt> or <tt>FAILED</tt>.
	 */
	public abstract Status execute();
	
	/**
	 * Reads an output parameter for this experiment
	 * @param key The path leading to the parameter
	 * @return The parameter
	 */
	public final int readInt(String key)
	{
		JsonElement e = read(key);
		if (e != null && e instanceof JsonNumber)
		{
			return ((JsonNumber) e).numberValue().intValue();
		}
		return 0;
	}
	
	/**
	 * Reads an output parameter for this experiment
	 * @param key The path leading to the parameter
	 * @return The parameter
	 */
	public final float readFloat(String key)
	{
		JsonElement e = read(key);
		if (e != null && e instanceof JsonNumber)
		{
			return ((JsonNumber) e).numberValue().intValue();
		}
		return 0;
	}

	/**
	 * Reads an output parameter for this experiment
	 * @param key The path leading to the parameter
	 * @return The parameter
	 */
	public final String readString(String key)
	{
		JsonElement e = read(key);
		if (e == null)
		{
			return null;
		}
		if (e instanceof JsonString)
		{
			return ((JsonString) e).stringValue();
		}
		return e.toString();
	}
	
	/**
	 * Reads an output parameter for this experiment
	 * @param key The path leading to the parameter
	 * @return The parameter
	 */
	public final JsonMap readMap(String key)
	{
		JsonElement e = read(key);
		if (e != null && e instanceof JsonMap)
		{
			return (JsonMap) e;
		}
		return null;
	}
	
	/**
	 * Reads an output parameter for this experiment
	 * @param key The path leading to the parameter
	 * @return The parameter
	 */
	public final JsonList readList(String key)
	{
		JsonElement e = read(key);
		if (e != null && e instanceof JsonList)
		{
			return (JsonList) e;
		}
		return null;
	}
	
	/**
	 * Sets an output parameter for this experiment
	 * @param key The key for this parameter
	 * @param value The parameter's value
	 * @return This experiment
	 */
	public final Experiment write(String key, JsonElement value)
	{
		m_outputParameters.put(key, value);
		return this;
	}
	
	/**
	 * Sets an output parameter for this experiment
	 * @param key The key for this parameter
	 * @param value The parameter's value
	 * @return This experiment
	 */
	public final Experiment write(String key, Number value)
	{
		m_outputParameters.put(key, value);
		return this;
	}
	
	/**
	 * Sets an output parameter for this experiment
	 * @param key The key for this parameter
	 * @param value The parameter's value
	 * @return This experiment
	 */
	public final Experiment write(String key, String value)
	{
		m_outputParameters.put(key, value);
		return this;
	}

	
	/**
	 * Sets an input parameter for this experiment
	 * @param key The key for this parameter
	 * @param value The parameter's value
	 * @return This experiment
	 */
	public final Experiment setInput(String key, String value)
	{
		m_inputParameters.put(key, value);
		return this;
	}
	
	/**
	 * Sets an input parameter for this experiment
	 * @param key The key for this parameter
	 * @param value The parameter's value
	 * @return This experiment
	 */
	public final Experiment setInput(String key, Number value)
	{
		m_inputParameters.put(key, value);
		return this;
	}
	
	/**
	 * Sets an input parameter for this experiment
	 * @param key The key for this parameter
	 * @param value The parameter's value
	 * @return This experiment
	 */
	public final Experiment setInput(String key, JsonElement value)
	{
		m_inputParameters.put(key, value);
		return this;
	}
	
	/**
	 * Gets the ID of this experiment. Note that the ID is controlled by the
	 * laboratory, and should not be used for anything meaningful.
	 * @return The ID
	 */
	public final int getId()
	{
		return m_id;
	}
	
	/**
	 * Gets the start time of the experiment
	 * @return The difference, measured in milliseconds,
	 *   between the start time and midnight, January 1, 1970 UTC. The
	 *   value is -1 if the experiment has not started yet.
	 */
	public final long getStartTime()
	{
		return m_startTime;
	}

	/**
	 * Gets the end time of the experiment
	 * @return The difference, measured in milliseconds,
	 *   between the end time and midnight, January 1, 1970 UTC. The
	 *   value is -1 if the experiment has not started yet or is still
	 *   running.
	 */
	public final long getEndTime()
	{
		return m_endTime;
	}
	
	/**
	 * Gets the name of the lab assistant that ran the experiment
	 * @return The name, or the empty string if the experiment has not run yet
	 */
	public final String getWhoRan()
	{
		return m_runBy;
	}
	
	/**
	 * Sets the name of the lab assistant that ran the experiment
	 * @param name The name of the assistant that ran the experiment
	 * @return This experiment
	 */
	public final Experiment setWhoRan(String name)
	{
		m_runBy = name;
		return this;
	}
	
	/**
	 * Resets the experiment. This puts the experiment in the same state as if
	 * it were not run. However, if prerequisites were generated, they are not
	 * deleted. Use {@link #clean()} to do so.
	 */
	public synchronized final void reset()
	{
		m_outputParameters.clear();
		m_startTime = -1;
		m_endTime = -1;
		m_runBy = "";
		m_status = Status.DUNNO;
		m_errorMessage = "";
	}
	
	/**
	 * Resets the experiment and clears any prerequisites it may have generated.
	 * This puts the experiment in the same state as if
	 * it were not run.
	 */
	public final void clean()
	{
		cleanPrerequisites();
		reset();
	}
	
	/**
	 * Gets the current status of this experiment
	 * @return The status
	 */
	public synchronized final Status getStatus()
	{
		if (m_status == Status.DUNNO)
		{
			if (prerequisitesFulfilled())
			{
				m_status = Status.PREREQ_OK;
			}
			else
			{
				m_status = Status.PREREQ_NOK;
			}
		}
		return m_status;
	}

	@Override
	public final void run()
	{
		m_startTime = System.currentTimeMillis();
		if (!prerequisitesFulfilled())
		{
			m_status = Status.PREREQ_F;
			if (!fulfillPrerequisites())
			{
				m_status = Status.FAILED;
				if (m_errorMessage.isEmpty())
				{
					m_errorMessage = "Failed while generating prerequisites";
				}
				return;
			}
		}
		m_status = Status.RUNNING;
		m_status = execute();
		m_endTime = System.currentTimeMillis();

	}
	
	/**
	 * Returns the (eventual) error message produced by the execution of this
	 * experiment.
	 * @return The message, or the empty string if nothing
	 */
	public final String getErrorMessage()
	{
		return m_errorMessage;
	}
	
	/**
	 * Sets an error message produced by the execution of the experiment. The
	 * message is intended to explain why the experiment has failed, and should
	 * be left empty if the experiment has not run yet or has finished
	 * successfully.
	 * @param message The message
	 */
	public final void setErrorMessage(String message)
	{
		m_errorMessage = message;
	}
	
	/**
	 * Provides an estimate of the time this experiment is supposed to take.
	 * This value is used in the user interface to provide a rough measure
	 * of when a set of experiments is supposed to finish.
	 * @param factor A scaling factor, provided by the lab. The same set of
	 * experiments running in different environments (e.g. different computers)
	 * may take a different time. This factor can be used to scale the
	 * estimate. A higher value means a slower environment.
	 * 
	 * @return An estimate of the time, in seconds
	 */
	public float getDurationEstimate(float factor)
	{
		return 0f;
	}
	
	/**
	 * Waits for some time
	 * @param duration The waiting interval, in milliseconds
	 */
	public static final void wait(int duration)
	{
		try
		{
			Thread.sleep(duration);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieves a value, either from the input or the output parameters.
	 * The method starts by fetching it from the input parameters; if this
	 * fails, it fetches it from the output parameters.
	 * @param path The path to the element.
	 * @return The element; null if the path cannot be found in either the
	 *   input or the output parameters
	 */
	public final JsonElement read(String path)
	{
		JsonElement e = null;
		e = JsonPath.get(m_inputParameters, path);
		if (e == null)
		{
			e = JsonPath.get(m_outputParameters, path);
		}
		return e;
	}

	/**
	 * Returns the set of all parameters of this experiment. This is effectively
	 * a merging between the input and the output parameter maps.
	 * @return The parameters
	 */
	public final JsonMap getAllParameters()
	{
		JsonMap out = new JsonMap();
		for (String s : m_inputParameters.keySet())
		{
			out.put(s, m_inputParameters.get(s));
		}
		for (String s : m_outputParameters.keySet())
		{
			out.put(s, m_outputParameters.get(s));
		}
		return out;
	}
	
	@Override
	public String toString()
	{
		return m_inputParameters.toString();
	}
	
	/**
	 * Gets the set of all input parameter <em>names</em>
	 * @return The set of names
	 */
	public final Set<String> getInputKeys()
	{
		return m_inputParameters.keySet();
	}
	
	/**
	 * Sets a textual description for a parameter of the experiment
	 * @param path The XPath corresponding to the parameter's location
	 *   in the JSON structure
	 * @param text The textual description
	 * @return This experiment
	 */
	public final Experiment describe(String path, String text)
	{
		m_parameterDescriptions.put(path, text);
		return this;
	}
	
	/**
	 * Gets the textual description for a parameter of the experiment
	 * @param path The XPath corresponding to the parameter's location
	 *   in the JSON structure
	 * @return The textual description
	 */
	public final String getDescription(String path)
	{
		if (!m_parameterDescriptions.containsKey(path))
		{
			return "";
		}
		return m_parameterDescriptions.get(path);
	}
	
	/**
	 * Interrupts the experiment
	 * @return This experiment
	 */
	public final Experiment interrupt()
	{
		m_status = Status.FAILED;
		m_errorMessage = "The experiment was manually interrupted";
		return this;
	}
}
