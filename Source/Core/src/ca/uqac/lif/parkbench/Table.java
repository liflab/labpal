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
import java.util.Vector;

public class Table
{
	protected Map<Float,Map<String,Float>> m_values;
	
	public Table(Vector<String> series, Vector<Float> x_values)
	{
		super();
		m_values = new HashMap<Float,Map<String,Float>>();
		for (float v : x_values)
		{
			Map<String,Float> values = new HashMap<String ,Float>();
			for (String s : series)
			{
				values.put(s, null);
			}
			m_values.put(v, values);
		}
	}
	
	public void put(String series, float x, float y)
	{
		Map<String,Float> m = m_values.get(x);
		m.put(series, y);
	}
	
	/**
	 * Returns the contents of the table as a CSV string.
	 * @param series The data series in the table
	 * @param x_values A <em>sorted</em> list of all the x-values
	 *   occurring in the table
	 * @return A CSV string
	 */
	public String toCsv(Vector<String> series, Vector<Float> x_values)
	{
		StringBuilder out = new StringBuilder();
		for (float x : x_values)
		{
			out.append(x);
			Map<String,Float> m = m_values.get(x);
			for (String s : series)
			{
				out.append(",");
				if (m.get(s) != null)
				{
					out.append(m.get(s));
				}
				else
				{
					out.append("?");
				}
			}
			out.append("\n");
		}
		return out.toString();
	}
}
