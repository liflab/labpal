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

import java.util.regex.Pattern;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.provenance.DirectValue;
import ca.uqac.lif.labpal.provenance.NodeFunction;

/**
 * Provenance function that links to a whole table
 * @author Sylvain Hallé
 */
public class TableFunctionNode implements NodeFunction 
{
	protected final Table m_table;
	
	/**
	 * The number of rows in the table
	 */
	protected final int m_rows;
	
	/**
	 * The number of columns in the table
	 */
	protected final int m_cols;
	
	public TableFunctionNode(Table t, int rows, int cols)
	{
		super();
		m_table = t;
		m_rows = rows;
		m_cols = cols;
	}
	
	/**
	 * Gets the identifier of a table
	 * @param t The table
	 * @return The identifier
	 */
	public static String getDatapointId(Table t)
	{
		return "T" + t.getId();
	}
	
	@Override
	public String toString()
	{
		return "Table #" + m_table.getId();
	}
	
	@Override
	public String getDataPointId()
	{
		return "T" + m_table.getId();
	}
	
	@Override
	public NodeFunction dependsOn()
	{
		// Depends on all the cells
		DirectValue dv = new DirectValue();
		for (int r = 0; r < m_rows; r++)
		{
			for (int c = 0; c < m_cols; c++)
			{
				dv.add(new TableCellNode(m_table, r, c));
			}
		}
		return dv;
	}
	
	/**
	 * Gets the owner of a datapoint
	 * @param lab
	 * @param datapoint_id
	 * @return The owner, or {@code null} if this object could not
	 * find the owner
	 */
	public static Table getOwner(Laboratory lab, String datapoint_id)
	{
		if (!datapoint_id.startsWith("T"))
			return null;
		String[] parts = datapoint_id.split(Pattern.quote(NodeFunction.s_separator));
		int id = Integer.parseInt(parts[0].substring(1).trim());
		return lab.getTable(id);
	}
	
	public Table getOwner()
	{
		return m_table;
	}
		
	@Override
	public int hashCode()
	{
		return m_table.getId();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof TableFunctionNode))
		{
			return false;
		}
		TableFunctionNode tcn = (TableFunctionNode) o;
		return tcn.m_table.getId() == m_table.getId();
	}
}
