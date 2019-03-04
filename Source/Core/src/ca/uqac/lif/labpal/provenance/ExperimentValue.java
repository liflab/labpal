/*
  LabPal, a versatile benchmark environment
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
package ca.uqac.lif.labpal.provenance;

import java.util.regex.Pattern;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.OwnershipManager;

/**
 * Node function indicating a dependency to a value computed by an experiment.
 * @author Sylvain Hallé
 */
public class ExperimentValue implements NodeFunction 
{
  /**
   * The experiment this function refers to
   */
	protected final Experiment m_experiment;
	
	/**
	 * The parameter name in the experiment this function refers to
	 */
	protected final String m_parameter;
	
	/**
	 * If the parameter is an array, the position in the array this function
	 * refers to. The value -1 indicates that the position is not
	 * relevant.
	 */
	protected final int m_position;
	
	/**
	 * Creates a new experiment value function.
	 * @param e The experiment this function refers to
	 * @param key The parameter name in the experiment this function
	 * refers to
	 */
	public ExperimentValue(Experiment e, String key)
	{
		this(e, key, -1);
	}
	
	/**
   * Creates a new experiment value function.
   * @param e The experiment this function refers to
   * @param key The parameter name in the experiment this function
   * refers to
   * @param position If the parameter is an array, the position in the array this function
   * refers to. The value -1 indicates that the position is not
   * relevant.
   */
	public ExperimentValue(Experiment e, String key, int position)
	{
		super();
		m_experiment = e;
		m_parameter = key;
		m_position = position;
	}
	
	@Override
	public String toString()
	{
		if (m_position < 0)
		{
			return "Value of " + m_parameter + " in Experiment #" + m_experiment.getId();
		}
		else
		{
			return "Value of " + m_parameter + "[" + m_position + "] in Experiment #" + m_experiment.getId();
		}
	}
	
	@Override
	public String getDataPointId()
	{
		if (m_position < 0)
		{
			return "E" + m_experiment.getId() + s_separator + m_parameter;
		}
		else
		{
			return "E" + m_experiment.getId() + s_separator + m_parameter + s_separator + m_position;
		}
	}
	
	public Experiment getOwner()
	{
		return m_experiment;
	}
	
	public static NodeFunction dependsOn(Experiment e, String datapoint_id)
	{
		// Parse the datapoint ID and call the experiment on the extracted values
		String[] parts = datapoint_id.split(Pattern.quote(NodeFunction.s_separator));
		if (parts.length >= 2)
		{
			int id = Integer.parseInt(parts[0].substring(1).trim());
			if (id != e.getId())
			{
				// Wrong experiment
				return null;
			}
			String param = parts[1].trim();
			if (parts.length == 2)
			{
				return e.dependsOnKey(param);
			}
			if (parts.length == 3)
			{
				int pos = Integer.parseInt(parts[2].trim());
				return e.dependsOnCell(param, pos);
			}
		}
		return null;
	}
	
	/**
	 * Gets the owner of a datapoint
	 * @param lab The lab in which to look for the data point
	 * @param datapoint_id The LDI
	 * @return The owner, or {@code null} if this object could not
	 * find the owner
	 */
	/*@ pure null @*/ public static Experiment getOwner(/*@ non_null @*/ OwnershipManager lab, /*@ non_null @*/ String datapoint_id)
	{
		if (!datapoint_id.startsWith("E"))
			return null;
		String[] parts = datapoint_id.split(Pattern.quote(NodeFunction.s_separator));
		return (Experiment) lab.getObjectWithId(parts[0]);
	}

	@Override
	public NodeFunction dependsOn() 
	{
		// Depends on nothing
		return this;
	}
	
	@Override
	public int hashCode()
	{
		return m_experiment.hashCode() + m_parameter.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof ExperimentValue))
		{
			return false;
		}
		ExperimentValue ev = (ExperimentValue) o;
		return ev.m_experiment.getId() == m_experiment.getId() &&
				ev.m_parameter.compareTo(m_parameter) == 0 &&
				ev.m_position == m_position;
 	}
}
