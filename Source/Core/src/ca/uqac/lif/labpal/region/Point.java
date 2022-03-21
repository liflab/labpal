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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * A set of associations between a dimension name and a value. Points can
 * be ordered by comparing the values of their respective dimensinos.
 * 
 * @since 3.0
 */
public class Point implements Comparable<Point>
{
	/**
	 * A map keeping the association between dimension names and their
	 * corresponding values.
	 */
	/*@ non_null @*/ protected final Map<String,Object> m_values;
	
	/**
	 * A list keeping the names of all dimensions of this point in sorted order.
	 */
	/*@ non_null @*/ protected final List<String> m_dimensions;

	/**
	 * Creates a new empty point.
	 */
	public Point()
	{
		super();
		m_values = new HashMap<String,Object>();
		m_dimensions = new ArrayList<String>();
	}
	
	/**
	 * Creates a point by copying the contents of another point.
	 * @param p The other point
	 */
	public Point(/*@ non_null @*/ Point p)
	{
		this();
		m_values.putAll(p.m_values);
		m_dimensions.addAll(p.m_dimensions);
	}
	
	/**
	 * Gets the names of all the dimensions for which a value is defined in
	 * this point.
	 * @return The dimension names
	 */
	public String[] getDimensions()
	{
		Set<String> keys = m_values.keySet();
		String[] dims = new String[keys.size()];
		int i = 0;
		for (String d : keys)
		{
			dims[i++] = d;
		}
		return dims;
	}

	/**
	 * Sets the value associated to a dimension of the point.
	 * @param name The name of the dimension
	 * @param value The value
	 * @return This point
	 */
	public Point set(String name, Object value)
	{
		m_values.put(name, value);
		if (!m_dimensions.contains(name))
		{
			m_dimensions.add(name);
			Collections.sort(m_dimensions);
		}
		return this;
	}
	
	/**
	 * Gets the value associated to a dimension of the point.
	 * @param name The name of the dimension
	 * @return The value, or <tt>null</tt> if no dimension exists with that
	 * name in the point
	 */
	public Object get(String name)
	{
		if (m_values.containsKey(name))
		{
			Object o = m_values.get(name);
			if (o instanceof NamedValue)
			{
				return ((NamedValue) o).getValue();
			}
			return m_values.get(name);
		}
		return null;
	}
	
	public int getInt(String name)
	{
		Object o = get(name);
		if (!(o instanceof Number))
		{
			return 0;
		}
		Number n = (Number) o;
		return n.intValue();
	}
	
	public String getString(String name)
	{
		Object o = get(name);
		if (o == null)
		{
			return null;
		}
		return o.toString();
	}
	
	/**
	 * Gets the value associated to a dimension of the point.
	 * @param name The name of the dimension
	 * @return The value, or <tt>null</tt> if no dimension exists with that
	 * name in the point
	 */
	public Object getName(String name)
	{
		if (m_values.containsKey(name))
		{
			Object o = m_values.get(name);
			if (o instanceof NamedValue)
			{
				return ((NamedValue) o).getName();
			}
			return m_values.get(name);
		}
		return null;
	}

	@Override
	public int hashCode()
	{
		int h = 0;
		for (Object o : m_values.values())
		{
			if (o != null)
			{
				h += o.hashCode();
			}
		}
		return h;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Point))
		{
			return false;
		}
		Point p = (Point) o;
		for (Map.Entry<String,Object> e : m_values.entrySet())
		{
			String key = e.getKey();
			if (!p.m_values.containsKey(key))
			{
				return false;
			}
			if (!same(p.m_values.get(key), e.getValue()))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		String[] dims = getDimensions();
		for (int i = 0; i < dims.length; i++)
		{
			if (i > 0)
			{
				out.append(",");
			}
			out.append(dims[i]).append("=").append(m_values.get(dims[i]));
		}
		return out.toString();
	}

	/**
	 * Determines if two objects are equal, taking into account the fact
	 * that they can be null.
	 * @param o1 The first object
	 * @param o2 The second object
	 * @return <tt>true</tt> if the objects are considered equal,
	 * <tt>false</tt> otherwise
	 */
	protected static boolean same(Object o1, Object o2)
	{
		boolean o1_null = o1 == null;
		boolean o2_null = o2 == null;
		if (o1_null != o2_null)
		{
			return false;
		}
		if (o1 == null)
		{
			return true; // Since o2 == null also
		}
		if (o1 instanceof Number && o2 instanceof Number)
		{
			return ((Number) o1).floatValue() == ((Number) o2).floatValue();
		}
		return o1.equals(o2);
	}

	@Override
	public int compareTo(Point p)
	{
		for (String d : m_dimensions)
		{
			if (!p.m_dimensions.contains(d))
			{
				// Cannot compare
				return 0;
			}
			int value = Spreadsheet.compare(m_values.get(d), p.m_values.get(d));
			if (value != 0)
			{
				return value;
			}
		}
		return 0;
	}
}