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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.provenance.NodeFunction;
import ca.uqac.lif.labpal.table.Table.CellCoordinate;

public class TableNode
{
	protected final String m_key;
	
	protected final Object m_value;
	
	protected int m_row;
	
	protected int m_col;
	
	protected List<CellCoordinate> m_coordinates;
	
	public List<TableNode> m_children;
	
	public TableNode(String key, Object value)
	{
		super();
		m_children = new LinkedList<TableNode>();
		m_key = key;
		m_value = value;
		m_coordinates = new LinkedList<CellCoordinate>();
	}
	
	public void addCoordinate(CellCoordinate c)
	{
		m_coordinates.add(c);
	}
	
	public void addCoordinate(int row, int col)
	{
		m_coordinates.add(new CellCoordinate(row, col));
	}
	
	public void setRow(int row)
	{
		m_row = row;
	}
	
	public void setCol(int col)
	{
		m_col = col;
	}
	
	public int getRow()
	{
		return m_row;
	}
	
	public int getCol()
	{
		return m_col;
	}
	
	/**
	 * Gets the number of leaf nodes in this tree
	 * @return The number of leaf nodes
	 */
	public int countLeaves()
	{
		if (m_children.isEmpty())
		{
			return 1;
		}
		int count = 0;
		for (TableNode child : m_children)
		{
			count += child.countLeaves();
		}
		return count;
	}
		
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		toString(out, "");
		return out.toString();
	}
	
	protected void toString(StringBuilder out, String indent)
	{
		out.append(indent);
		out.append(m_key).append("=").append(m_value);
		if (!m_children.isEmpty())
		{
			out.append("\n");
		}
		for (TableNode child : m_children)
		{
			child.toString(out, indent + "  ");
		}
		out.append("\n");
	}
	
	public String toHtml()
	{
		StringBuilder out = new StringBuilder();
		out.append("<ul>");
		toHtmlList(out);
		out.append("</ul>");
		return out.toString();
	}
	
	protected void toHtmlList(StringBuilder out)
	{
		out.append("<li>").append(m_key).append("=").append(m_value);
		if (!m_children.isEmpty())
		{
			out.append("<ul>");
			for (TableNode child : m_children)
			{
				child.toHtmlList(out);
			}
			out.append("</ul>\n");
		}
		out.append("</li>\n");
	}

	public Object getValue()
	{
		return m_value;
	}

	/**
	 * Gets the cell coordinates associated to this table node
	 * @return The list of coordinates
	 */
	public List<CellCoordinate> getCoordinates()
	{
		return m_coordinates;
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
}