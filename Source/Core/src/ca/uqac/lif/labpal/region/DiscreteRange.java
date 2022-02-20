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
package ca.uqac.lif.labpal.region;

/**
 * A numerical range defined as an iteration.
 * @author Sylvain Hallé
 */
public class DiscreteRange extends ExtensionDomain<Number>
{
	/**
	 * The start of the range.
	 */
	protected final double m_start;
	
	/**
	 * The end of the range.
	 */
	protected final double m_end;
	
	/**
	 * The increment for values in the range.
	 */
	protected final double m_step;
	
	/**
	 * Creates a new discrete range. This constructor corresponds to the set
	 * of all numerical values of the form <i>start</i> + <i>k</i> &times;
	 * <i>step</i> (for <i>k</i> &in; {0, 1, &hellip;}) lying in the closed
	 * interval [<i>start</i>,<i>end</i>].
	 * @param name The name of the domain
	 * @param start The start of the range
	 * @param end The end of the range
	 * @param step The increment for values in the range
	 */
	public DiscreteRange(String name, double start, double end, double step)
	{
		super(name);
		m_start = start;
		m_end = end;
		m_step = step;
		for (double x = start; x <= end; x += step)
		{
			m_values.add(x);
		}
	}
	
	@Override
	public String toString()
	{
		// We override toString to display as a range instead of a list of numbers
		return m_name + "\u21a6" + "[" + m_start + "," + m_end + "]:" + m_step;
	}
}
