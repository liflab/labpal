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

import java.util.HashSet;
import java.util.Set;

/**
 * A domain defined as an explicit enumeration of its values.
 * @author Sylvain Hallé
 */
public class ExtensionDomain<T> implements Domain<T>
{
	/**
	 * The set of values in the domain.
	 */
	/*@ non_null @*/ protected final Set<T> m_values;

	/**
	 * The name of the dimension for that domain.
	 */
	/*@ non_null @*/ protected final String m_name;

	/**
	 * Creates a new domain by extension.
	 * @param name The name of the domain
	 * @param values The list of values contained in the domain
	 */
	@SafeVarargs
	public ExtensionDomain(String name, T ... values)
	{
		super();
		m_name = name;
		m_values = new HashSet<T>(values.length);
		for (T t : values)
		{
			m_values.add(t);
		}
	}

	@Override
	/*@ pure non_null @*/ public String getName()
	{
		return m_name;
	}

	@Override
	public int size() 
	{
		return m_values.size();
	}

	@Override
	public ResettableIterator<T> getValues() 
	{
		return new ResettableIterator<T>(m_values);
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append(m_name).append("\u21a6").append("{");
		boolean first = true;
		for (T v : m_values)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				out.append(",");
			}
			out.append(v);
		}
		return out.toString();
	}
}
