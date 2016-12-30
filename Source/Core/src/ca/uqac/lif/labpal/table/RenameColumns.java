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

import java.util.Map.Entry;

/**
 * Table built by renaming the columns of another table
 * @author Sylvain Hallé
 */
public class RenameColumns extends Table
{
	/**
	 * The table from which we take the values 
	 */
	protected final Table m_table;
	
	/**
	 * The new names to be given to the original table's columns
	 */
	protected final String[] m_names;
	
	/**
	 * Creates a new table by renaming the columns of an existing table
	 * @param t The original table
	 * @param new_names The new names to be given to the original 
	 *    table's columns
	 */
	public RenameColumns(Table t, String ... new_names)
	{
		super();
		m_table = t;
		m_names = new_names;
		assert new_names.length == m_table.getColumnCount();
	}

	@Override
	public Comparable<?> get(int col, int row)
	{
		return m_table.get(col, row);
	}

	@Override
	public int getColumnCount()
	{
		return m_table.getColumnCount();
	}

	@Override
	public Class<? extends Comparable<?>>[] getColumnTypes()
	{
		return m_table.getColumnTypes();
	}

	@Override
	public int getRowCount()
	{
		return m_table.getRowCount();
	}

	@Override
	public DataTable getConcreteTable(String[] ordering)
	{
		String[] new_ordering = new String[ordering.length];
		for (int i = 0; i < ordering.length; i++)
		{
			int pos = m_table.getColumnPosition(ordering[i]);
			String col_name = m_table.getColumnName(pos);
			new_ordering[i] = col_name;
		}
		return m_table.getConcreteTable(new_ordering);
	}
	
	@Override
	public Class<? extends Comparable<?>> getColumnTypeFor(String col_name)
	{
		int position = getColumnPosition(col_name);
		return m_table.getColumnTypeFor(position);
	}

	@Override
	public String getColumnName(int col)
	{
		if (col < 0 || col >= m_names.length)
		{
			return null;
		}
		return m_names[col];
	}

	@Override
	public int getColumnPosition(String name)
	{
		for (int i = 0; i < m_names.length; i++)
		{
			if (m_names[i].compareTo(name) == 0)
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public String[] getColumnNames()
	{
		return m_names;
	}

	@Override
	public TableEntry findEntry(TableEntry e)
	{
		// Create new entry by renaming columns to that of inner table
		TableEntry new_e = new TableEntry();
		for (Entry<String,Object> entry : e.entrySet())
		{
			int pos = getColumnPosition(entry.getKey());
			String old_name = m_table.getColumnName(pos);
			new_e.put(old_name, entry.getValue());
		}
		TableEntry found_entry = m_table.findEntry(new_e);
		if (found_entry == null)
		{
			return null;
		}
		// Now do the opposite for the found entry
		TableEntry renamed_e = new TableEntry();
		for (Entry<String,Object> entry : found_entry.entrySet())
		{
			int pos = m_table.getColumnPosition(entry.getKey());
			String new_name = getColumnName(pos);
			renamed_e.put(new_name, entry.getValue());
		}
		return renamed_e;
	}

	@Override
	public DataTable getConcreteTable()
	{
		return getConcreteTable(m_names);
	}
}
