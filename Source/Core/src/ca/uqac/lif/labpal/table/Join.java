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
public class Join extends Table
{
	/**
	 * The tables to join
	 */
	protected Table[] m_tables;
	
	/**
	 * The columns of the tables on which to perform a join
	 */
	protected String[] m_commonDimensions;
	
	/**
	 * Creates a new join table
	 * @param common_dimensions
	 * @param tables
	 */
	public Join(String[] common_dimensions, Table ... tables)
	{
		super();
		m_tables = tables;
		m_commonDimensions = common_dimensions;
		//m_otherDimensions = other_dimensions;
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
	
	@Override
	@SuppressWarnings("unchecked")
	public DataTable getConcreteTable(String ... ordering)
	{
		Class<? extends Comparable<?>>[] new_types = new Class[getColumnCount()];
		for (int i = 0; i < ordering.length; i++)
		{
			Class<? extends Comparable<?>> col_type = getColumnTypeFor(ordering[i]);
			new_types[i] = col_type;
		}
		DataTable mt = new DataTable(ordering);
		List<TableEntry> entries = new ArrayList<TableEntry>();
		List<TableEntry> keys = getRowKeys();
		for (TableEntry key : keys)
		{
			for (int table_pos = 0; table_pos < m_tables.length; table_pos++)
			{
				Table t = m_tables[table_pos];
				TableEntry t_entry = t.findEntry(key);
				TableEntry existing_e = findExistingEntry(t_entry, entries);
				existing_e.putAll(t_entry);
			}
		}
		mt.addAll(entries);
		return mt;
	}

	@Override
	public Comparable<?> get(int col, int row)
	{
		int num_join_cols = m_commonDimensions.length;
		List<TableEntry> row_keys = getRowKeys();
		if (row < 0 || row >= row_keys.size() || col < 0 || col >= getColumnCount())
		{
			// Out of bounds
			return null;
		}
		TableEntry e = row_keys.get(row);
		String name = getColumnName(col);
		if (col < num_join_cols)
		{
			Object o = e.get(name);
			return Table.castValue(o);
		}
		int pos = num_join_cols;
		for (Table t : m_tables)
		{
			if (col - pos < t.getColumnCount() - num_join_cols)
			{
				// We found the right table; now find the row with matching key
				String col_name = t.getColumnName(col - pos);
				TableEntry found_entry = t.findEntry(e);
				if (!found_entry.containsKey(col_name))
				{
					return null;
				}
				return castValue(found_entry.get(col_name));
			}
			pos += (t.getColumnCount() - num_join_cols);
		}
		return null;
	}

	@Override
	public int getColumnCount()
	{
		int col_cnt = m_commonDimensions.length;
		for (Table t : m_tables)
		{
			col_cnt += (t.getColumnCount() - m_commonDimensions.length);
		}
		return col_cnt;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Comparable<?>>[] getColumnTypes()
	{
		List<Class<? extends Comparable<?>>> names = new ArrayList<Class<? extends Comparable<?>>>();
		for (String name : m_commonDimensions)
		{
			names.add(getColumnTypeFor(name));
		}
		for (int table_index = 0; table_index < m_tables.length; table_index++)
		{
			String[] col_names = getOtherColumns(table_index);
			for (String col_name : col_names)
			{
				names.add(getColumnTypeFor(col_name));				
			}
		} 
		Class<? extends Comparable<?>>[] a_names = new Class[names.size()];
		int i = 0;
		for (Class<? extends Comparable<?>> name : names)
		{
			a_names[i] = name;
			i++;
		}
		return a_names;
	}

	@Override
	public int getRowCount()
	{
		return getRowKeys().size();
	}
	
	/**
	 * Gets the list of column names for a given table, excluding the join
	 * columns
	 * @param table_pos The index of the table
	 * @return An array of column names
	 */
	protected String[] getOtherColumns(int table_pos)
	{
		Table t = m_tables[table_pos];
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

	@Override
	public Class<? extends Comparable<?>> getColumnTypeFor(String name)
	{
		if (isJoinColumn(name))
		{
			// All the tables contain that column
			return m_tables[0].getColumnTypeFor(name);
		}
		for (int table_index = 0; table_index < m_tables.length; table_index++)
		{
			String[] col_names = getOtherColumns(table_index);
			for (String col_name : col_names)
			{
				if (col_name.compareTo(name) == 0)
				{
					return m_tables[table_index].getColumnTypeFor(name);
				}
			}
		} 
		return null;
	}

	@Override
	public String getColumnName(int col)
	{
		int num_join_cols = m_commonDimensions.length;
		if (col < 0 || col >= getColumnCount())
		{
			return null;
		}
		if (col < num_join_cols)
		{
			return m_commonDimensions[col];
		}
		int pos = num_join_cols;
		for (Table t : m_tables)
		{
			if (col - pos < t.getColumnCount() - num_join_cols)
			{
				return t.getColumnName(col - pos);
			}
			pos += (t.getColumnCount() - num_join_cols);
		}
		return null;
	}

	@Override
	public int getColumnPosition(String name)
	{
		int pos = 0;
		for (String col_name : m_commonDimensions)
		{
			if (col_name.compareTo(name) == 0)
			{
				return pos;
			}
			pos++;
		}
		for (int table_index = 0; table_index < m_tables.length; table_index++)
		{
			String[] col_names = getOtherColumns(table_index);
			for (String col_name : col_names)
			{
				if (col_name.compareTo(name) == 0)
				{
					return pos;
				}
				pos++;				
			}
		} 
		return -1;
	}

	@Override
	public String[] getColumnNames()
	{
		List<String> names = new ArrayList<String>();
		for (String name : m_commonDimensions)
		{
			names.add(name);
		}
		for (int table_index = 0; table_index < m_tables.length; table_index++)
		{
			String[] col_names = getOtherColumns(table_index);
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
	
	protected List<TableEntry> getRowKeys()
	{
		List<TableEntry> keys = new ArrayList<TableEntry>();
		for (Table t : m_tables)
		{
			DataTable dt = t.getConcreteTable(t.getColumnNames());
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

	@Override
	public TableEntry findEntry(TableEntry e)
	{
		// TODO Implement this
		throw new UnsupportedOperationException();
	}

	@Override
	public DataTable getConcreteTable()
	{
		return getConcreteTable(getColumnNames());
	}

}
