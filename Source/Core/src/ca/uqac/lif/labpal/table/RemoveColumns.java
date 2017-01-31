/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

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
 * Removes columns from a table
 * @author Sylvain Hallé
 */
public class RemoveColumns implements TableTransformation 
{
	/**
	 * The names of the columns to remove
	 */
	protected final String[] m_namesToRemove;
	
	/**
	 * Creates a new instance of this table transformation
	 * @param names_to_remove The names of the columns to remove from the
	 * original table
	 */
	public RemoveColumns(String ... names_to_remove)
	{
		super();
		m_namesToRemove = names_to_remove;
	}
	
	/**
	 * Creates a new instance of this table transformation
	 * @param names_to_remove The names of the columns to remove from the
	 * original table
	 * @return The transformation
	 */
	public static RemoveColumns get(String ... names_to_remove)
	{
		return new RemoveColumns(names_to_remove);
	}

	@Override
	public TempTable transform(TempTable... tables)
	{
		TempTable table = tables[0];
		List<String> cols = new ArrayList<String>();
		String[] col_names = table.getColumnNames();
		for (String name : col_names)
		{
			cols.add(name.intern());
		}
		for (String name : m_namesToRemove)
		{
			cols.remove(name.intern());
		}
		String[] new_col_names = new String[cols.size()];
		int i = 0;
		for (String name : cols)
		{
			new_col_names[i] = name;
			i++;
		}
		return table.getDataTable(new_col_names);
	}
	
	
}
