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
package ca.uqac.lif.labpal.table.rendering;

import java.util.Collection;
import java.util.List;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.provenance.NodeFunction;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.Table.CellCoordinate;
import ca.uqac.lif.labpal.table.TableNode;

/**
 * Renders a result tree as an HTML table
 * 
 * @author Sylvain Hallé
 */
public class HtmlTableNodeRenderer extends TableNodeRenderer 
{
	public HtmlTableNodeRenderer(Table t)
	{
		super(t);
	}

	public HtmlTableNodeRenderer(Table t, Collection<CellCoordinate> to_highlight)
	{
		super(t, to_highlight);
	}

	/**
	 * OS-dependent carriage return
	 */
	protected static final String CRLF = System.getProperty("line.separator");

	protected int m_row = 0;

	protected int m_col = 0;

	@Override
	public void startStructure(StringBuilder out)
	{
		out.append("<table border=\"1\">").append(CRLF);
	}

	@Override
	public void startKeys(StringBuilder out)
	{
		out.append("<thead>").append(CRLF);
	}

	@Override
	public void printKey(StringBuilder out, String key)
	{
		out.append("<th>").append(key).append("</th>");
	}

	@Override
	public void endKeys(StringBuilder out)
	{
		out.append("</thead>").append(CRLF);
	}

	@Override
	public void startBody(StringBuilder out)
	{
		out.append("<tbody>").append(CRLF);
	}

	@Override
	public void startRow(StringBuilder out, int max_depth)
	{
		out.append("<tr>").append(CRLF);
	}

	@Override
	public void printCell(StringBuilder out, List<JsonElement> values, int nb_children, int max_depth, TableNode node)
	{
		String css_class = "";
		List<CellCoordinate> coordinates = node.getCoordinates();
		for (CellCoordinate cc : coordinates)
		{
			if (isHighlighted(cc.row, cc.col))
			{
				css_class = " class=\"highlighted\"";
				break; // One is enough
			}			
		}
		if (nb_children < 2)
		{
			out.append(" <td" + css_class + ">");
		}
		else
		{
			out.append(" <td" + css_class + " rowspan=\"").append(nb_children).append("\">");
		}
		if (coordinates.size() > 0)
		{
			CellCoordinate cc = coordinates.get(0);
			String dp_id = "";
			NodeFunction nf = m_table.dependsOn(cc.row, cc.col);
			if (nf != null)
			{
				dp_id = "T" + m_table.getId() + ":" + cc.row + ":" + cc.col; //nf.getDataPointId();
			}
			out.append("<a class=\"explanation\" title=\"Click to see where this value comes from\" href=\"explain?id=").append(dp_id).append("\">");
		}
		JsonElement last = values.get(values.size() - 1);
		if (last instanceof JsonString)
		{
			out.append(((JsonString) last).stringValue());
		}
		else if (last instanceof JsonNull)
		{
			out.append("");
		}
		else
		{
			out.append(last);
		}
		if (coordinates.size() > 0)
		{
			out.append("</a>");
		}
		out.append(" </td>").append(CRLF);
		m_col++;
	}

	@Override
	public void printRepeatedCell(StringBuilder out, List<JsonElement> values, int index, int max_depth)
	{
		// Do nothing
		m_col++;
	}

	@Override
	public void endRow(StringBuilder out, int max_depth)
	{
		m_row++;
		m_col = 0;
		out.append("</tr>").append(CRLF);
	}

	@Override
	public void endBody(StringBuilder out)
	{
		out.append("</tbody>").append(CRLF);
	}

	@Override
	public void endStructure(StringBuilder out)
	{
		out.append("</tr>").append(CRLF).append("</tbody>").append(CRLF).append("</table>");
	}

	@Override
	public void reset()
	{
		super.reset();
		m_row = 0;
		m_col = 0;
	}
}
