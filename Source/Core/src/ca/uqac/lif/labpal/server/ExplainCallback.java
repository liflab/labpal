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
package ca.uqac.lif.labpal.server;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.macro.MacroNode;
import ca.uqac.lif.mtnp.plot.PlotNode;
import ca.uqac.lif.petitpoucet.AggregateFunction;
import ca.uqac.lif.labpal.provenance.ExperimentValue;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.ProvenanceNode;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TableCellNode;
import ca.uqac.lif.mtnp.table.TableFunctionNode;

/**
 * Callback producing a provenance tree from one of the lab's data points.
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
		s = s.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.BINOCULARS));
		ProvenanceNode node = m_lab.getDataTracker().explain(datapoint_id);
		if (node == null)
		{
			s = s.replaceAll("\\{%EXPLANATION%\\}", "<p>There does not seem to be an explanation available for this data point. Some data points are available only when the experiments they depend on have been executed.</p>");
			return s;
		}
		s = s.replaceAll("\\{%IMAGE_URL%\\}", Matcher.quoteReplacement("provenance-graph?id=" + datapoint_id));
		StringBuilder out = new StringBuilder();
		out.append("<ul class=\"explanation\">\n");
		explanationToHtml(node, "", out);
		out.append("</ul>\n");
		s = s.replaceAll("\\{%EXPLANATION%\\}", Matcher.quoteReplacement(out.toString()));
		
		return s;
	}
	
	protected void explanationToHtml(ProvenanceNode node, String parent_id, StringBuilder out)
	{
		out.append("<li><div class=\"around-pulldown\"><div class=\"pulldown\"><a title=\"Click to see where this value comes from\" href=\"").append(htmlEscape(getDataPointUrl(node))).append("\">").append(node).append("</a></div>\n");
		List<ProvenanceNode> parents = node.getParents();
		if (parents != null && !parents.isEmpty())
		{
			String new_parent = node.getNodeFunction().getDataPointId();
			out.append("<div class=\"pulldown-contents\"><ul>");
			for (ProvenanceNode pn : parents)
			{
				explanationToHtml(pn, new_parent, out);
			}
			out.append("</ul></div></div>");
		}
		out.append("</li>\n");
	}
	
	public static String getDataPointUrl(ProvenanceNode node)
	{
		NodeFunction nf = node.getNodeFunction();
		if (nf == null)
		{
			return "#";
		}
		StringBuilder highlight_string = new StringBuilder();
		if (nf instanceof AggregateFunction)
		{
			AggregateFunction af = (AggregateFunction) nf;
			boolean first = true;
			Table first_owner = null;
			for (NodeFunction dep_node : af.getDependencyNodes())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					highlight_string.append(",");
				}
				if (first_owner == null && dep_node instanceof TableCellNode)
				{
					first_owner = ((TableCellNode) dep_node).getOwner();
				}
				highlight_string.append(dep_node.getDataPointId());
			}
			if (first_owner != null)
			{
				return "table?id=" + first_owner.getId() + "&highlight=" + highlight_string.toString();
			}
			else
			{
				return "#";
			}
			
		}
		else if (nf instanceof TableCellNode)
		{
			TableCellNode tcn = (TableCellNode) nf;
			highlight_string.append(tcn.getDataPointId());
			return "table?id=" + tcn.getOwner().getId() + "&highlight=" + highlight_string.toString();
		}
		else if (nf instanceof TableFunctionNode)
		{
			TableFunctionNode tcn = (TableFunctionNode) nf;
			return "table?id=" + tcn.getOwner().getId();
		}
		else if (nf instanceof PlotNode)
		{
			PlotNode tcn = (PlotNode) nf;
			return "plot?id=" + tcn.getOwner().getId();
		}
		else if (nf instanceof MacroNode)
		{
			MacroNode tcn = (MacroNode) nf;
			return "macros" + "?highlight=" + tcn.getDataPointId() + "#" + tcn.getOwner().getId();
		}
		else if (nf instanceof ExperimentValue)
		{
			ExperimentValue ev = (ExperimentValue) nf;
			highlight_string.append(ev.getDataPointId());
			return "experiment?id=" + ev.getOwner().getId() + "&highlight=" + highlight_string.toString();
		}
		return "#";
	}
}
