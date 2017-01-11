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
import java.util.List;

/**
 * Joins multiple tables on the values of specific columns.
 * This table performs roughly what the "join" operator does in relational
 * algebra.
 * @author Sylvain Hallé
 */
public class Join implements TableTransformation
{
	/**
	 * The columns of the tables on which to perform a join
	 */
	protected String[] m_commonDimensions;
	
	/**
	 * Creates a new join table
	 * @param common_dimensions
	 */
	public Join(String[] common_dimensions)
	{
		super();
		m_commonDimensions = common_dimensions;
	}
	
	/**
	 * Finds an entry with the same values of common dimensions
	 * @param e The entry to find
	 * @param entries The set of entries
	 * @return The entry
	 */
	protected TableEntry findExistingEntry(TableEntry e, List<TableEntry> entries)
	{
		for (TableEntry ent : entries)
		{
			boolean found = true;
			for (String key : m_commonDimensions)
			{
				if (!ent.get(key).equals(e.get(key)))
				{
					found = false;
					break;
				}
			}
			if (found)
			{
				return ent;
			}
		}
		entries.add(e);
		return e;
	}
	
	protected int getColumnCount(DataTable ... tables)
	{
		int col_cnt = m_commonDimensions.length;
		for (DataTable t : tables)
		{
			col_cnt += (t.getColumnCount() - m_commonDimensions.length);
		}
		return col_cnt;
	}
	
	/**
	 * Gets the list of column names for a given table, excluding the join
	 * columns
	 * @param table_pos The index of the table
	 * @return An array of column names
	 */
	protected String[] getOtherColumns(int table_pos, DataTable ... tables)
	{
		DataTable t = tables[table_pos];
		String[] t_names = t.getColumnNames();
		String[] other_names = new String[t_names.length - m_commonDimensions.length];
		int pos = 0;
		for (int i = 0; i < t_names.length; i++)
		{
			if (isJoinColumn(t_names[i]))
			{
				continue;
			}
			other_names[pos] = t_names[i];
			pos++;
		}
		return other_names;
	}
	
	protected boolean isJoinColumn(String name)
	{
		for (String col_name : m_commonDimensions)
		{
			if (col_name.compareTo(name) == 0)
			{
				return true;
			}
		}
		return false;
	}

	protected Class<? extends Comparable<?>> getColumnTypeFor(String name, DataTable ... tables)
	{
		if (isJoinColumn(name))
		{
			// All the tables contain that column
			return tables[0].getColumnTypeFor(name);
		}
		for (int table_index = 0; table_index < tables.length; table_index++)
		{
			String[] col_names = getOtherColumns(table_index, tables);
			for (String col_name : col_names)
			{
				if (col_name.compareTo(name) == 0)
				{
					return tables[table_index].getColumnTypeFor(name);
				}
			}
		} 
		return null;
	}

	protected String[] getColumnNames(DataTable ... tables)
	{
		List<String> names = new ArrayList<String>();
		for (String name : m_commonDimensions)
		{
			names.add(name);
		}
		for (int table_index = 0; table_index < tables.length; table_index++)
		{
			String[] col_names = getOtherColumns(table_index, tables);
			for (String col_name : col_names)
			{
				names.add(col_name);				
			}
		} 
		String[] a_names = new String[names.size()];
		int i = 0;
		for (String name : names)
		{
			a_names[i] = name;
			i++;
		}
		return a_names;
	}
	
	protected List<TableEntry> getRowKeys(DataTable ... tables)
	{
		List<TableEntry> keys = new ArrayList<TableEntry>();
		for (DataTable t : tables)
		{
			DataTable dt = t.getDataTable(t.getColumnNames());
			for (TableEntry e : dt.getEntries())
			{
				TableEntry new_e = new TableEntry();
				for (String k : m_commonDimensions)
				{
					new_e.put(k, e.get(k));
				}
				keys.add(new_e);
			}
		}
		return keys;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataTable transform(DataTable ... tables)
	{
		String[] ordering = getColumnNames(tables);
		Class<? extends Comparable<?>>[] new_types = new Class[getColumnCount(tables)];
		for (int i = 0; i < ordering.length; i++)
		{
			Class<? extends Comparable<?>> col_type = getColumnTypeFor(ordering[i], tables);
			new_types[i] = col_type;
		}
		DataTable mt = new DataTable(ordering);
		List<TableEntry> entries = new ArrayList<TableEntry>();
		List<TableEntry> keys = getRowKeys();
		for (TableEntry key : keys)
		{
			for (int table_pos = 0; table_pos < tables.length; table_pos++)
			{
				DataTable t = tables[table_pos];
				TableEntry t_entry = t.findEntry(key);
				TableEntry existing_e = findExistingEntry(t_entry, entries);
				existing_e.putAll(t_entry);
			}
		}
		mt.addAll(entries);
		return mt;
	}

}
