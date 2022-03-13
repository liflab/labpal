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

import ca.uqac.lif.units.DimensionValue;
import ca.uqac.lif.units.functions.UnitAdd;

/**
 * A range of dimension values defined as an iteration.
 * @author Sylvain Hallé
 */
public class DimensionRange extends ExtensionDomain<DimensionValue>
{
	/**
	 * Creates a new discrete range.
	 * @param name The name of the domain
	 * @param start The start of the range
	 * @param end The end of the range
	 * @param step The increment for values in the range
	 * @return A new discrete range
	 */
	public static DimensionRange range(String name, DimensionValue start, DimensionValue end, DimensionValue step)
	{
		return new DimensionRange(name, start, end, step);
	}
	
	/**
	 * The start of the range.
	 */
	protected final DimensionValue m_start;
	
	/**
	 * The end of the range.
	 */
	protected final DimensionValue m_end;
	
	/**
	 * The increment for values in the range.
	 */
	protected final DimensionValue m_step;
	
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
	public DimensionRange(String name, DimensionValue start, DimensionValue end, DimensionValue step)
	{
		super(name);
		m_start = start;
		m_end = end;
		m_step = step;
		m_values.add(start);
		DimensionValue next = start;
		while (next.compareTo(end) <= 0)
		{
			m_values.add(next);
			next = UnitAdd.get(next, step);
		}
	}
	
	@Override
	public String toString()
	{
		// We override toString to display as a range instead of a list of numbers
		return m_name + "\u21a6" + "[" + m_start + "," + m_end + "]:" + m_step;
	}
}
