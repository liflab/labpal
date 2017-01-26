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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.provenance.DirectValue;

/**
 * Transforms a table by expanding the values of one column as column
 * headers.
 * <p>
 * Take for example this table:
 * <table border="1">
 * <tr><th>Browser</th><th>Market</th><th>Share</th></tr>
 * <tr><td>Firefox</td><td>video</td><td>20</td></tr>
 * <tr><td>Firefox</td><td>audio</td><td>23</td></tr>
 * <tr><td>IE</td><td>video</td><td>10</td></tr>
 * <tr><td>IE</td><td>audio</td><td>13</td></tr>
 * </table>
 * <p>
 * One can replace column "Market", and create as many columns as there are
 * values for this attribute. We can set the value to put in each column
 * as that of the corresponding value of "Share". This will yield the
 * following table:
 * <table border="1">
 * <tr><th>Browser</th><th>video</th><th>audio</th></tr>
 * <tr><td>Firefox</td><td>20</td><td>23</td></tr>
 * <tr><td>IE</td><td>10</td><td>13</td></tr>
 * </table>
 * Producing this transformation is done by instantiating this class
 * as follows:
 * <pre>
 * TableTransformation t = new ExpandAsColumns("Market", "Share");
 * </pre>
 * @author Sylvain Hallé
 *
 */
public class ExpandAsColumns implements TableTransformation
{
	protected final String m_columnKey;
	
	protected final String m_valueKey;
	
	public ExpandAsColumns(String column_key, String value_key)
	{
		super();
		m_columnKey = column_key;
		m_valueKey = value_key;
	}
	
	@Override
	public TempTable transform(TempTable ... tables)
	{
		TempTable table = tables[0];
		Set<String> new_keys = new HashSet<String>();
		for (TableEntry te : table.getEntries())
		{
			if (!te.containsKey(m_columnKey))
			{
				continue;
			}
			Object o = te.get(m_columnKey);
			if (o == null)
			{
				continue;
			}
			if (o instanceof JsonString)
			{
				new_keys.add(((JsonString) o).stringValue());
			}
			else if (o instanceof String)
			{
				new_keys.add((String) o);
			}
			else
			{
				new_keys.add(o.toString());
			}
		}
		if (new_keys.isEmpty())
		{
			// Nothing to do
			return table;
		}
		String[] old_names = table.getColumnNames();
		String[] new_names = new String[old_names.length - 2 + new_keys.size()];
		int pos = 0;
		for (String name : old_names)
		{
			if (name.compareTo(m_columnKey) != 0 && name.compareTo(m_valueKey) != 0)
			{
				new_names[pos] = name;
				pos++;
			}
		}
		for (String name : new_keys)
		{
			new_names[pos] = name;
			pos++;
		}
		TempTable new_table = new TempTable(table.getId(), new_names);
		List<TableEntry> entries = new ArrayList<TableEntry>();
		int value_pos = table.getColumnPosition(m_valueKey);
		int column_pos = table.getColumnPosition(m_columnKey);
		for (TableEntry te : table.getEntries())
		{
			TableEntry existing_entry = findEntry(te, entries);
			if (te.containsKey(m_columnKey))
			{
				
				String s = getString(te.get(m_columnKey));
				existing_entry.put(s, te.get(m_valueKey));
				DirectValue dv = new DirectValue();
				dv.add(new TableCellNode(table, te.getRowIndex(), value_pos));
				dv.add(new TableCellNode(table, te.getRowIndex(), column_pos));
				existing_entry.addDependency(s, dv);
			}
		}
		new_table.addAll(entries);
		return new_table;
	}
	
	protected TableEntry findEntry(TableEntry e, List<TableEntry> list)
	{
		Set<Entry<String,Object>> entry_set = e.entrySet();
		for (TableEntry te : list)
		{
			boolean found = true;
			for (Entry<String,Object> entry : entry_set)
			{
				if (entry.getKey().compareTo(m_columnKey) == 0 || entry.getKey().compareTo(m_valueKey) == 0)
				{
					// Ignore key
					continue;
				}
				if (!te.containsKey(entry.getKey()))
					continue;
				if (!te.get(entry.getKey()).equals(entry.getValue()))
				{
					found = false;
					break;
				}
			}
			if (found)
			{
				return te;
			}
		}
		TableEntry new_e = new TableEntry(e);
		list.add(new_e);
		return new_e;
	}
	
	protected static String getString(Object o)
	{
		if (o instanceof JsonString)
		{
			return ((JsonString) o).stringValue();
		}
		else if (o instanceof String)
		{
			return (String) o;
		}
		return o.toString();
	}
}
