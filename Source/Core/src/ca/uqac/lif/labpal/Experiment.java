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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonPath;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.provenance.DataOwner;
import ca.uqac.lif.labpal.provenance.ExperimentValue;
import ca.uqac.lif.labpal.provenance.NodeFunction;

/**
 * An experiment is a runnable process that takes input parameters and
 * produces output parameters.
 * 
 * @author Sylvain Hallé
 */
public abstract class Experiment implements Runnable, DataOwner
{
	/**
	 * The status of the experiment
	 */
	public static enum Status {DUNNO, PREREQ_NOK, PREREQ_OK, PREREQ_F, RUNNING, DONE, DONE_WARNING, FAILED, KILLED};

	/**
	 * The input parameters given to this experiment
	 */
	private JsonMap m_inputParameters;
	
	/**
	 * The output parameters that this experiment generates
	 */
	private JsonMap m_outputParameters;
	
	/**
	 * The set of parameter names to show in the experiments table
	 */
	private transient Set<String> m_keysToHide;
	
	/**
	 * The current status of the experiment
	 */
	private Status m_status;
	
	/**
	 * A numerical value that uniquely identifies each experiment in a lab
	 */
	private int m_id;
	
	/**
	 * The maximum duration for this experiment (in milliseconds).
	 * If the experiment lasts longer than this duration, the lab assistant
	 * can interrupt it. A negative value indicates that no timeout
	 * applies.
	 */
	private long m_maxDuration = -1;
	
	/**
	 * A list of exceptions that the experiment does not throw, but
	 * rather adds to a list
	 */
	protected transient List<ExperimentException> m_warnings;
	
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
	 * An approximate measurement of the experiment's progression
	 */
	private transient float m_progression = 0;
	
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
	
	/**
	 * A random number generator associated with this experiment
	 */
	transient ca.uqac.lif.labpal.Random m_random;
	
	public Experiment(Status status)
	{
		super();
		m_inputParameters = new JsonMap();
		m_outputParameters = new JsonMap();
		m_parameterDescriptions = new HashMap<String,String>();
		m_keysToHide = new HashSet<String>();
		m_warnings = new ArrayList<ExperimentException>();
		m_runBy = "";
		m_status = status;
		m_errorMessage = "";
		m_random = null;
	}
	
	public Experiment()
	{
		this(Status.DUNNO);
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
	 * {@link #fulfillPrerequisites()}.
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
	 * {@link #prerequisitesFulfilled()}.
	 * 
	 * @throws ExperimentException If the prerequisites have not been
	 *   successfully generated
	 */
	public void fulfillPrerequisites() throws ExperimentException
	{
		return;
	}
	
	/**
	 * Cleans any prerequisites this experiment may have generated.
	 * For example: deleting files that were generated, etc.
	 */
	public void cleanPrerequisites()
	{
		return;
	}
	
	/**
	 * Adds the name of a key to hide from the experiment list
	 * @param key The key
	 */
	public void addKeyToHide(String key)
	{
		m_keysToHide.add(key);
	}
	
	/**
	 * Adds a warning for this experiment
	 * @param ex The warning
	 * @return This experiment
	 */
	public Experiment addWarning(ExperimentException ex)
	{
		m_warnings.add(ex);
		return this;
	}
	
	/**
	 * Adds a warning for this experiment, enclosing it in a
	 * generic {@link ExperimentException} object
	 * @param ex The message
	 * @return This experiment
	 */
	public Experiment addWarning(String message)
	{
		m_warnings.add(new ExperimentException(message));
		return this;
	}
	
	/**
	 * Gets the warnings associated to this experiment
	 * @return A list of warnings
	 */
	public List<ExperimentException> getWarnings()
	{
		return m_warnings;
	}
	
	/**
	 * Checks if this experiment has warnings associated to it
	 * @return {@code true} if there are warnings, {@code false} otherwise
	 */
	public boolean hasWarnings()
	{
		return !m_warnings.isEmpty();
	}
	
	/**
	 * Determines if this parameter should be hidden from the experiment list
	 * @param key The key
	 * @return true if the parameter should be hidden; false otherwise
	 */
	public boolean isHidden(String key)
	{
		return m_keysToHide.contains(key);
	}
	
	/**
	 * Sets the description for this experiment
	 * @param d The description. It must be valid HTML.
	 * @return This experiment
	 */
	public Experiment setDescription(String d)
	{
		if (d != null)
			m_description = d;
		return this;
	}
	
	/**
	 * Gets the description for this experiment
	 * @return The description. If you override this method, make sure it
	 * outputs valid HTML. Its contents are <em>not</em> escaped by the
	 * server.
	 */
	public String getDescription()
	{
		return m_description;
	}
	
	public static String getClassText()
	{
		return "A generic experiment";
	}
	
	/**
	 * Executes the experiment.
	 * @throws ExperimentException Used to signal the abnormal termination
	 *   of the experiment. If this method ends without throwing an exception,
	 *   it is assumed it has completed successfully.
	 */
	public abstract void execute() throws ExperimentException;
	
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
	public final long readLong(String key)
	{
		JsonElement e = read(key);
		if (e != null && e instanceof JsonNumber)
		{
			return ((JsonNumber) e).numberValue().longValue();
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
	public final Experiment writePrimitive(String key, String value)
	{
		int x;
		float f;
		try
		{
			// Is it an int
			x = Integer.parseInt(value);
		}
		catch (NumberFormatException e1)
		{
			try
			{
				// No; is it a float?
				f = Float.parseFloat(value);
			}
			catch (NumberFormatException e2)
			{
				// Then it's a string
				return write(key, value);
			}
			return write(key, (float) f);
		}
		return write(key, (int) x);
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
	 * Sets an input parameter for this experiment by copying those from
	 * an existing map
	 * @param params The input parameters
	 * @return This experiment
	 */
	public final Experiment setInput(JsonMap params)
	{
		for (String key : params.keySet())
		{
			setInput(key, params.get(key));
		}
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
	 * Sets an input parameter for this experiment, by trying to cast
	 * its string value into a primitive number type (int or float)
	 * if possible
	 * @param key The key for this parameter
	 * @param value The parameter's value. If the parameter cannot be cast
	 * as a number, it will
	 * @return This experiment
	 */
	public final Experiment setInputPrimitive(String key, String value)
	{
		int x;
		float f;
		try
		{
			// Is it an int
			x = Integer.parseInt(value);
		}
		catch (NumberFormatException e1)
		{
			try
			{
				// No; is it a float?
				f = Float.parseFloat(value);
			}
			catch (NumberFormatException e2)
			{
				// Then it's a string
				return setInput(key, value);
			}
			return setInput(key, (float) f);
		}
		return setInput(key, (int) x);
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
		m_warnings.clear();
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
		if (m_status == Status.DUNNO || m_status == Status.PREREQ_NOK || m_status == Status.PREREQ_OK)
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
			try
			{
				fulfillPrerequisites();
			}
			catch (Exception e)
			{
				// If the call throws anything, we consider it a failure
				m_status = Status.PREREQ_F;
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				setErrorMessage(sw.toString());
				return;
			}
		}
		m_status = Status.PREREQ_OK;
		m_status = Status.RUNNING;
		try
		{
			execute();
			m_status = Status.DONE;
		}
		catch (Exception e)
		{
			// If the experiment throws anything, we consider it a failure
			m_status = Status.FAILED;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			setErrorMessage(sw.toString());
		}
		m_endTime = System.currentTimeMillis();
		validate();
		if (hasWarnings() && m_status == Status.DONE)
		{
			m_status = Status.DONE_WARNING;
		}
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
	 * Outputs a description of this experiment as a short character string.
	 * This is intended to be displayed in small text windows, such as in
	 * LabPal's text interface. By default, this method does the same thing
	 * as {@link #toString()}, but it can be overridden to produce a better
	 * display.
	 * @param width The suggested width of this string. You can use this
	 *   value to adjust the verbosity of the display to the available space
	 * @return The string
	 */
	public String toShortString(int width)
	{
		return toString();
	}
	
	/**
	 * Gets the set of all input parameter <em>names</em>
	 * @param exclude_hidden Set to true to exclude keys that have been
	 *   marked as hidden by {@link #addKeyToHide(String)}
	 * @return The set of names
	 */
	public final Set<String> getInputKeys(boolean exclude_hidden)
	{
		if (exclude_hidden == false)
		{
			return m_inputParameters.keySet();
		}
		Set<String> input_keys = new HashSet<String>();
		for (String key : m_inputParameters.keySet())
		{
			if (!m_keysToHide.contains(key))
			{
				input_keys.add(key);
			}
		}
		return input_keys;
	}
	
	/**
	 * Gets the set of all input parameter <em>names</em>
	 * @return The set of names
	 */
	public final Set<String> getInputKeys()
	{
		return getInputKeys(false);
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
		m_endTime = System.currentTimeMillis();
		return this;
	}
	
	/**
	 * Gets the random number generator for this experiment
	 * @return The generator
	 */
	public final ca.uqac.lif.labpal.Random getRandom()
	{
		return m_random;
	}
	
	/**
	 * Sets the current progression of the execution of the
	 * experiment
	 * @param p The current progression. This should be a value between
	 *   0 (not done at all) and 1 (finished or failed)
	 * @return This experiment
	 */
	public synchronized final Experiment setProgression(float p)
	{
		if (p >= 0 && p <= 1)
			m_progression = p;
		return this;
	}
	
	/**
	 * Gets the current progression of the execution of the
	 * experiment
	 * @return The current progression. This should be a value between
	 *   0 (not done at all) and 1 (finished or failed)
	 */
	public synchronized final float getProgression()
	{
		Status s = getStatus();
		if (s == Status.DONE)
		{
			return 1;
		}
		if (s == Status.PREREQ_F || s == Status.PREREQ_NOK || s == Status.FAILED)
		{
			return 0;
		}
		return m_progression;
	}
	
	/**
	 * Gets the maximum duration for this experiment
	 * @return The duration
	 */
	public final long getMaxDuration()
	{
		return m_maxDuration;
	}
	
	/**
	 * Sets the maximum duration for this experiment.
	 * If the experiment lasts longer than this duration, the lab assistant
	 * can interrupt it.
	 * @return The duration, in milliseconds. A negative value indicates
	 * that no timeout applies.
	 */
	public final Experiment setMaxDuration(long duration)
	{
		m_maxDuration = duration;
		return this;
	}
	
	/**
	 * Interrupts the current experiment
	 * @return This experiment
	 */
	public final Experiment kill()
	{
		m_status = Status.KILLED;
		m_errorMessage = "The experiment was interrupted by the lab assistant because it was taking too long";
		m_endTime = System.currentTimeMillis();
		return this;
	}
	
	/**
	 * Checks if an experiment has a parameter of a given name
	 * @param name The name
	 * @return {@code true} if the parameter exists, {@code false} otherwise
	 */
	public boolean hasParameter(String name)
	{
		return m_inputParameters.containsKey(name) || m_outputParameters.containsKey(name);
	}
	
	/**
	 * Gets the number of "data points" generated by this experiment
	 * @return The number of points
	 */
	public int countDataPoints()
	{
		return m_outputParameters.keySet().size();
	}
	
	/**
	 * Checks if the data generated by this experiment is considered
	 * valid. Normally, the purpose of this method is to add warnings
	 * to the experiment after it has executed, if anything "strange"
	 * has happened.
	 * 
	 * @see #addWarning(ExperimentException)
	 */
	public void validate()
	{
		// Do nothing
	}
		
	@Override
	public final NodeFunction dependsOn(String id)
	{
		return ExperimentValue.dependsOn(this, id);
	}
	
	public final NodeFunction dependsOnKey(String key)
	{
		return new ExperimentValue(this, key);
	}
	
	public final NodeFunction dependsOnCell(String key, int position)
	{
		return new ExperimentValue(this, key, position);
	}
	
	@Override
	public Experiment getOwner()
	{
		return this;
	}	
}
