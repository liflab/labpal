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
package ca.uqac.lif.labpal.server;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.provenance.ProvenanceNode;

/**
 * Callback producing an image from one of the lab's plots, in various
 * formats.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>dl=1</tt>: to download the image instead of displaying it. This
 *   will prompt the user to save the file in its browser</li>
 * <li><tt>id=x</tt>: mandatory; the ID of the plot to display</li>
 * <li><tt>format=x</tt>: the requested image format. Currenly supports
 *   pdf, dumb (text), png and gp (raw data file for Gnuplot).
 * </ul>
 * 
 * @author Sylvain Hallé
 *
 */
public class ExplainCallback extends TemplatePageCallback
{	
	public ExplainCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/explain", lab, assistant);
	}

	@Override
	public String fill(String s, Map<String,String> params)
	{
		String datapoint_id = params.get("id");
		ProvenanceNode node = m_lab.getDataTracker().explain(datapoint_id);
		StringBuilder out = new StringBuilder();
		out.append("<ul class=\"explanation\">\n");
		explanationToHtml(node, out);
		out.append("</ul>\n");
		s = s.replaceAll("\\{%TITLE%\\}", "Explanation");
		s = s.replaceAll("\\{%EXPLANATION%\\}", Matcher.quoteReplacement(out.toString()));
		s = s.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.TABLE));
		return s;
	}
	
	protected void explanationToHtml(ProvenanceNode node, StringBuilder out)
	{
		out.append("<li><a title=\"Click to see where this value comes from\" href=\"").append(getDataPointUrl(node)).append("\">").append(node).append("</a>");
		List<ProvenanceNode> parents = node.getParents();
		if (parents != null && !parents.isEmpty())
		{
			out.append("<ul>");
			for (ProvenanceNode pn : parents)
			{
				explanationToHtml(pn, out);
			}
			out.append("</ul>");
		}
		out.append("</li>\n");
	}
	
	protected String getDataPointUrl(ProvenanceNode node)
	{
		String id = node.getDataPointId();
		String[] parts = id.split(":");
		String type = parts[0].substring(0, 1);
		int number = Integer.parseInt(parts[0].substring(1, parts[0].length()));
		if (type.compareTo("T") == 0)
		{
			// Table
			return "table?id=" + number + "&amp;highlight=" + id;
		}
		if (type.compareTo("E") == 0)
		{
			// Table
			return "experiment?id=" + number + "&amp;highlight=" + id;
		}
		return "#";
	}
}
