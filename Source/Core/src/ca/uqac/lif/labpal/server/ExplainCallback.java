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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.provenance.ExperimentValue;
import ca.uqac.lif.labpal.provenance.NodeFunction;
import ca.uqac.lif.labpal.provenance.ProvenanceNode;
import ca.uqac.lif.labpal.table.TableCellNode;

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
		s = s.replaceAll("\\{%TITLE%\\}", "Explanation");
		s = s.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.TABLE));
		ProvenanceNode node = m_lab.getDataTracker().explain(datapoint_id);
		if (node == null)
		{
			s = s.replaceAll("\\{%EXPLANATION%\\}", "<p>There does not seem to be an explanation available for this data point. Some data points are available only when the experiments they depend on have been executed.</p>");
			return s;
		}
		s = s.replaceAll("\\{%IMAGE_URL%\\}", Matcher.quoteReplacement("provenance-graph?id=" + datapoint_id));
		Map<String,Map<String,Set<String>>> highlight_groups = new HashMap<String,Map<String,Set<String>>>();
		getHighlightGroups(node, highlight_groups);
		StringBuilder out = new StringBuilder();
		out.append("<ul class=\"explanation\">\n");
		explanationToHtml(node, "", highlight_groups, out);
		out.append("</ul>\n");
		s = s.replaceAll("\\{%EXPLANATION%\\}", Matcher.quoteReplacement(out.toString()));
		
		return s;
	}
	
	protected void explanationToHtml(ProvenanceNode node, String parent_id, Map<String,Map<String,Set<String>>> highlight_groups, StringBuilder out)
	{
		out.append("<li><a title=\"Click to see where this value comes from\" href=\"").append(getDataPointUrl(node, parent_id, highlight_groups)).append("\">").append(node).append("</a>");
		List<ProvenanceNode> parents = node.getParents();
		if (parents != null && !parents.isEmpty())
		{
			String new_parent = node.getNodeFunction().getDataPointId();
			out.append("<ul>");
			for (ProvenanceNode pn : parents)
			{
				explanationToHtml(pn, new_parent, highlight_groups, out);
			}
			out.append("</ul>");
		}
		out.append("</li>\n");
	}
	
	public static void getHighlightGroups(ProvenanceNode node, Map<String,Map<String,Set<String>>> map)
	{
		/*if (node == null)
		{
			return;
		}
		String key = node.getNodeFunction().getDataPointId();
		Map<String,Set<String>> children = new HashMap<String,Set<String>>();
		List<ProvenanceNode> parents = node.getParents();
		for (ProvenanceNode pn : parents)
		{
			String parent_id = pn.getNodeFunction().getDataPointId();
			String prefix = parent_id.split(NodeFunction.s_separator)[0];
			if (!children.containsKey(prefix))
			{
				Set<String> new_set = new HashSet<String>();
				//new_set.add(key);
				children.put(prefix, new_set);
			}
			Set<String> to_highlight = children.get(prefix);
			to_highlight.add(parent_id);
			children.put(prefix, to_highlight);
		}
		map.put(key, children);
		for (ProvenanceNode pn : parents)
		{
			getHighlightGroups(pn, map);
		}*/
		return;
	}
	
	public static String getDataPointUrl(ProvenanceNode node, String parent_id, Map<String,Map<String,Set<String>>> highlight_groups)
	{
		NodeFunction nf = node.getNodeFunction();
		String id = nf.getDataPointId();
		Map<String,Set<String>> to_highlight = highlight_groups.get(parent_id);
		StringBuilder highlight_string = new StringBuilder();
		/*highlight_string.append(id);
		if (!parent_id.isEmpty())
		{
			String prefix = parts[0];
			if (to_highlight.containsKey(prefix))
			{
				Set<String> id_set = to_highlight.get(prefix);
				for (String id_to_highlight : id_set)
				{
					highlight_string.append(",").append(id_to_highlight);
				}
			}
		}*/
		if (nf instanceof TableCellNode)
		{
			TableCellNode tcn = (TableCellNode) nf;
			return "table?id=" + tcn.getOwner().getId() + "&amp;highlight=" + highlight_string.toString();
		}
		if (nf instanceof ExperimentValue)
		{
			ExperimentValue ev = (ExperimentValue) nf;
			return "experiment?id=" + ev.getOwner().getId() + "&amp;highlight=" + highlight_string.toString();
		}
		return "#";
	}
}
