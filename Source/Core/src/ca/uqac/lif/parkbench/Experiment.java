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

import ca.uqac.lif.json.JsonMap;

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
	 * The name of the lab assistant that ran the experiment (if any)
	 */
	private String m_runBy;
	
	public Experiment()
	{
		super();
		m_inputParameters = new JsonMap();
		m_outputParameters = new JsonMap();
		m_runBy = "";
		m_status = Status.DUNNO;
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
	 * @param params The input parameters for this experiment
	 * @return true if the prerequisites are fulfilled, false otherwise
	 */
	public boolean prerequisitesFulfilled(final JsonMap params)
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
	 * @param params The input parameters for this experiment
	 * @return true if the prerequisites have been successfully generated,
	 *   false otherwise
	 */
	public boolean fulfillPrerequisites(final JsonMap params)
	{
		return true;
	}
	
	/**
	 * Cleans any prerequisites this experiment may have generated.
	 * For example: deleting files that were generated, etc.
	 * @see {@link #fulfillPrerequisites(JsonMap)}
	 * @param params The input parameters for this experiment
	 */
	public void cleanPrerequisites(final JsonMap params)
	{
		return;
	}
	
	/**
	 * Executes the experiment.
	 * @param input The input parameters for this experiment
	 * @param output The output parameters for this experiment. Once the
	 *   experiment is over, it writes its results into this object.
	 * @return The status of the experiment once it has finished. This should
	 *   normally be either <tt>DONE</tt> or <tt>FAILED</tt>.
	 */
	public abstract Status execute(final JsonMap input, final JsonMap output);
	
	/**
	 * Gets the input parameters for this experiment
	 * @return The parameters
	 */
	public final JsonMap getInputParameters()
	{
		return m_inputParameters;
	}
	
	/**
	 * Sets the input parameters for this experiment
	 * @param params The parameters
	 * @return This experiment
	 */
	public final Experiment setInputParameters(JsonMap params)
	{
		m_inputParameters = params;
		return this;
	}

	/**
	 * Gets the output parameters for this experiment
	 * @return The parameters
	 */
	public final JsonMap getOutputParameters()
	{
		return m_outputParameters;
	}
	
	/**
	 * Sets the output parameters for this experiment
	 * @param params The parameters
	 * @return This experiment
	 */
	public final Experiment setOutputParameters(JsonMap params)
	{
		m_outputParameters = params;
		return this;
	}
	
	/**
	 * Gets the ID of this experiment. Note that the ID is controlled by the
	 * laboratory, and should not be used for anything meaningful.
	 * @return The ID
	 */
	final int getId()
	{
		return m_id;
	}
	
	/**
	 * Gets the start time of the experiment
	 * @return The difference, measured in milliseconds,
	 *   between the start time and midnight, January 1, 1970 UTC. The
	 *   value is -1 if the experiment has not started yet.
	 */
	final long getStartTime()
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
	final long getEndTime()
	{
		return m_endTime;
	}
	
	/**
	 * Gets the name of the lab assistant that ran the experiment
	 * @return The name, or the empty string if the experiment has not run yet
	 */
	final String getWhoRan()
	{
		return m_runBy;
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
	}
	
	/**
	 * Resets the experiment and clears any prerequisites it may have generated.
	 * This puts the experiment in the same state as if
	 * it were not run.
	 */
	public final void clean()
	{
		cleanPrerequisites(m_inputParameters);
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
			if (prerequisitesFulfilled(m_inputParameters))
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
		if (!prerequisitesFulfilled(m_inputParameters))
		{
			m_status = Status.PREREQ_F;
			fulfillPrerequisites(m_inputParameters);
		}
		m_status = Status.RUNNING;
		m_status = execute(m_inputParameters, m_outputParameters);
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
}
