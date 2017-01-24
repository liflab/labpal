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

import java.util.List;

/**
 * Data table only used temporarily. Typically, such a stable is a
 * single-use object to render the contents of another table.
 * 
 * @author Sylvain Hallé
 */
public class TempTable extends DataTable 
{
	/**
	 * Creates a new data table
	 * @param ordering The ordering of the columns in this table. This array
	 * should contain column names
	 */
	public TempTable(int id, String ... ordering)
	{
		super(id, null, ordering);	
	}

	public TempTable(int id, List<TableEntry> entries, String[] preferredOrdering)
	{
		super(id, entries, preferredOrdering);
	}
	
	@Override
	public boolean isTemporary()
	{
		return true;
	}
	
	/**
	 * Resets the ID for this table
	 * @param id
	 */
	public void setId(int id)
	{
		m_id = id;
	}
	
}
