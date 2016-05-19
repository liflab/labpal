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
package ca.uqac.lif.parkbench.server;

import java.util.Collections;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;
import ca.uqac.lif.parkbench.table.Table;

/**
 * Callback showing a list of plots
 * 
 * @author Sylvain Hallé
 *
 */
public class TablesPageCallback extends TemplatePageCallback
{
	protected static final transient Pattern s_pattern = Pattern.compile("exp-chk-(\\d+)");

	public TablesPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/tables", lab, assistant);
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Tables");
		Vector<Integer> ids = new Vector<Integer>();
		ids.addAll(m_lab.getTableIds());
		if (ids.isEmpty())
		{
			out = out.replaceAll("\\{%TABLES%\\}", "<p>No table is associated to this lab</p>\n");
		}
		else
		{
			Collections.sort(ids);
			out = out.replaceAll("\\{%TABLES%\\}", getTables(ids));			
		}
		out = out.replaceAll("\\{%SEL_TABLES%\\}", "selected");
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.TABLE));
		return out;
	}
	
	/**
	 * Produces the list of plots
	 * @return A well-formatted HTML string showing of each of the lab's plots
	 */
	public String getTables(Vector<Integer> ids)
	{
		StringBuilder out = new StringBuilder();
		out.append("<table class=\"tables\">\n");
		for (int id : ids)
		{
			Table table = m_lab.getTable(id);
			out.append("<tr>");
			out.append("<td class=\"table-icon\"></td>");
			out.append("<td><a href=\"table?id=").append(id).append("\" title=\"Click on table to view in new window\">");
			out.append(id);
			out.append("</a><td><td>").append(table.getTitle()).append("</td></tr>\n");			
		}
		out.append("</table>\n");
		return out.toString();
	}

}
