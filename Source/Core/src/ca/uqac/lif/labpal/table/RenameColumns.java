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

/**
 * Table built by renaming the columns of another table
 * @author Sylvain Hallé
 */
public class RenameColumns implements TableTransformation
{
	/**
	 * The new names to be given to the original table's columns
	 */
	protected final String[] m_names;
	
	/**
	 * Creates a new table by renaming the columns of an existing table
	 * @param new_names The new names to be given to the original 
	 *    table's columns
	 */
	public RenameColumns(String ... new_names)
	{
		super();
		m_names = new_names;
	}

	@Override
	public TempTable transform(TempTable ... tables)
	{
		TempTable table = tables[0];
		String[] ordering = table.getColumnNames();
		String[] new_ordering = new String[ordering.length];
		for (int i = 0; i < ordering.length; i++)
		{
			int pos = table.getColumnPosition(ordering[i]);
			String col_name = table.getColumnName(pos);
			new_ordering[i] = col_name;
		}
		TempTable tt = new TempTable(-4, new_ordering);
		tt.addAll(table.getDataTable(true, new_ordering).getEntries());
		return tt;

	}
}
