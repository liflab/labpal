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
		out = out.replaceAll("\\{%TABLES%\\}", getTables());
		out = out.replaceAll("\\{%SEL_TABLES%\\}", "selected");
		return out;
	}
	
	/**
	 * Produces the list of plots
	 * @return A well-formatted HTML string showing of each of the lab's plots
	 */
	public String getTables()
	{
		StringBuilder out = new StringBuilder();
		Vector<Integer> ids = new Vector<Integer>();
		ids.addAll(m_lab.getTableIds());
		Collections.sort(ids);
		out.append("<ul class=\"tables\">\n");
		for (int id : ids)
		{
			out.append("<li>");
			out.append("<a href=\"table?id=").append(id).append("\" title=\"Click on table to view in new window\">");
			out.append(id);
			out.append("</a></li>\n");			
		}
		out.append("</ul>\n");
		return out.toString();
	}

}
