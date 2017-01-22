/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

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

public class TableNode
{
	protected final String m_key;
	
	protected final Object m_value;
	
	protected int m_row;
	
	protected int m_col;
	
	public List<TableNode> m_children;
	
	public TableNode(String key, Object value)
	{
		super();
		m_children = new LinkedList<TableNode>();
		m_key = key;
		m_value = value;
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
}