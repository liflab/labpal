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
package ca.uqac.lif.labpal.table;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.labpal.Formatter;

/**
 * Creates columns from values of two parameters in an existing table.
 * For example, consider this table:
 * 
 * <table border="1">
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * <tr><td>0</td><td>1</td><td>3</td></tr>
 * <tr><td>0</td><td>4</td><td>3</td></tr>
 * <tr><td>1</td><td>2</td><td>1</td></tr>
 * <tr><td>1</td><td>6</td><td>3</td></tr>
 * <tr><td>2</td><td>9</td><td>1</td></tr>
 * <tr><td>2</td><td>3</td><td>3</td></tr>
 * </table> 
 * 
 * Using the transfomration <code>t = new GroupInColumns("A", "B")</code>
 * will produce this table:
 * 
 * <table border="1">
 * <tr><th>0</th><th>1</th><th>2</th></tr>
 * <tr><td>1</td><td>2</td><td>9</td></tr>
 * <tr><td>4</td><td>6</td><td>3</td></tr>
 * </table> 
 * Columns are the values of "A", and values in each column are those
 * found in parameter "B" for rows with that particular value of "A".
 * <p>
 * A common usage for this transformation is to use a table in a
 * {@link ca.uqac.lif.labpal.plot.gral.BoxPlot BoxPlot}.
 * 
 * @author Sylvain Hallé
 *
 */
public class GroupInColumns implements TableTransformation
{
	protected final String m_parameter;

	protected final String m_value;

	public GroupInColumns(String parameter, String value)
	{
		super();
		m_parameter = parameter;
		m_value = value;
	}

	@Override
	public DataTable transform(DataTable... tables) 
	{
		DataTable table = tables[0];
		Map<String,List<Object>> values = new HashMap<String,List<Object>>();
		for (TableEntry te : table.getEntries())
		{
			if (!te.containsKey(m_parameter))
				continue;
			Object p = te.get(m_parameter);
			if (p != null)
			{
				Object v = te.get(m_value);
				if (v != null && !(v instanceof JsonNull))
				{
					String p_s = Formatter.asString(p);
					List<Object> l_values = new LinkedList<Object>();
					if (values.containsKey(p_s))
					{
						l_values = values.get(p_s);
					}
					l_values.add(v);
					values.put(p_s, l_values);
				}
			}
		}
		String[] a_headers = new String[values.keySet().size()];
		@SuppressWarnings("unchecked")
		List<Object>[] a_values = new List[values.keySet().size()];
		int i = 0;
		for (Map.Entry<String,List<Object>> map_entry : values.entrySet())
		{
			a_headers[i] = map_entry.getKey();
			a_values[i] = map_entry.getValue();
			i++;
		}
		DataTable new_table = new DataTable(a_headers);
		i = 0;
		boolean added = true;
		while (added)
		{
			added = false;
			TableEntry te = new TableEntry();
			for (int j = 0; j < a_headers.length; j++)
			{
				if (i < a_values[j].size())
				{
					te.put(a_headers[j], a_values[j].get(i));
					added = true;
				}
				else
				{
					te.put(a_headers[j], JsonNull.instance);
				}
			}
			if (added)
			{
				new_table.add(te);
			}
			i++;
		}
		return new_table;
	}

}
