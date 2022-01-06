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
package ca.uqac.lif.labpal;

import ca.uqac.lif.petitpoucet.Part;

/**
 * A part that refers to a parameter value inside an experiment.
 * @author Sylvain Hallé
 */
public class ExperimentValue implements Part
{
	/**
	 * The name of the parameter pointed to by this part.
	 */
	/*@ non_null @*/ private final String m_parameter;
	
	/**
	 * Creates a new instance of the part.
	 * @param parameter The name of the parameter pointed to by this part
	 */
	public ExperimentValue(/*@ non_null @*/ String parameter)
	{
		super();
		m_parameter = parameter;
	}
	
	/*@ pure non_null @*/ public String getParameter()
	{
		return m_parameter;
	}
	
	@Override
	public boolean appliesTo(Object o)
	{
		return o instanceof Experiment;
	}

	@Override
	public Part head()
	{
		return this;
	}

	@Override
	public Part tail()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return m_parameter;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof ExperimentValue))
		{
			return false;
		}
		return m_parameter.compareTo(((ExperimentValue) o).m_parameter) == 0;
	}
	
	@Override
	public int hashCode()
	{
		if (m_parameter == null)
		{
			return 0;
		}
		return m_parameter.hashCode();
	}

}
