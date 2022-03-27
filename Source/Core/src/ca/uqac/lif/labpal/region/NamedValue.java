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
 * A value associated with a displayable name. This class is useful when
 * creating a region where some of its dimensions contain non-scalar values
 * such as arbitrary objects. In such a case, one can encase each of these
 * objects into a named value, so that a value can be associated with a
 * printable string. It is hence possible to use objects that have no
 * {@link #toString()} method.
 * <p>
 * Uniqueness of named values rests on the uniqueness of their name only.
 * That is, two named values with the same name will report as being equal
 * regardless of the actual "value" each of them contains. One must therefore
 * be careful to define domains where each named value is unique.
 * 
 * @since 3.0
 * 
 * @author Sylvain Hallé
 */
public class NamedValue implements Comparable<NamedValue>
{
	/**
	 * The name of this value.
	 */
	/*@ non_null @*/ protected final String m_name;
	
	/**
	 * The value (!) of this value.
	 */
	/*@ null @*/ protected final Object m_value;
	
	public NamedValue(/*@ non_null @*/ String name, /*@ null @*/ Object value)
	{
		super();
		m_name = name;
		m_value = value;
	}
	
	/*@ pure non_null @*/ public String getName()
	{
		return m_name;
	}
	
	/*@ pure null @*/ public Object getValue()
	{
		return m_value;
	}
	
	@Override
	public String toString()
	{
		return m_name;
	}

	@Override
	public int compareTo(NamedValue nv) 
	{
		return m_name.compareTo(nv.m_name);
	}
	
	@Override
	public int hashCode()
	{
		return m_name.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof NamedValue))
		{
			return false;
		}
		NamedValue nv = (NamedValue) o;
		return nv.m_name.equals(m_name);
	}
}
