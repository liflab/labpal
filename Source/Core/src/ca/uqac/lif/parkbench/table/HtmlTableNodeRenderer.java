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
package ca.uqac.lif.parkbench.table;

import java.util.List;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonString;

public class HtmlTableNodeRenderer extends TableNodeRenderer 
{
	@Override
	public void startStructure(StringBuilder out)
	{
		out.append("<table border=\"1\">\n");
	}
	
	@Override
	public void startKeys(StringBuilder out)
	{
		out.append("<thead>\n");
	}
	
	@Override
	public void printKey(StringBuilder out, String key)
	{
		out.append("<th>").append(key).append("</th>");
	}
	
	@Override
	public void endKeys(StringBuilder out)
	{
		out.append("</thead>\n");
	}
	
	@Override
	public void startBody(StringBuilder out)
	{
		out.append("<tbody>\n");
	}
	
	@Override
	public void startRow(StringBuilder out)
	{
		out.append("<tr>\n");
	}
	
	@Override
	public void printCell(StringBuilder out, List<JsonElement> values, int nb_children)
	{
		if (nb_children < 2)
		{
			out.append(" <td>");
		}
		else
		{
			out.append(" <td rowspan=\"").append(nb_children).append("\">");
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
		out.append(" </td>\n");
	}
	
	@Override
	public void printRepeatedCell(StringBuilder out, List<JsonElement> values, int index)
	{
		//out.append("<td>-</td>");
	}
	
	@Override
	public void endRow(StringBuilder out)
	{
		out.append("</tr>\n");
	}
	
	@Override
	public void endBody(StringBuilder out)
	{
		out.append("</tbody>\n");
	}
	
	@Override
	public void endStructure(StringBuilder out)
	{
		out.append("</tr>\n</tbody>\n</table>");
	}

}
