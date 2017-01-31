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
 * Selects columns from another table
 * @author Sylvain Hallé
 */
public class Select implements TableTransformation 
{
	/**
	 * The column names to select
	 */
	protected String[] m_columnNames;
	
	public Select(String ... column_names)
	{
		super();
		m_columnNames = column_names;
	}
	
	public static Select get(String ... column_names)
	{
		return new Select(column_names);
	}

	@Override
	public TempTable transform(TempTable... tables)
	{
		TempTable table = tables[0];
		return table.getDataTable(m_columnNames);
	}

}
