/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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

import ca.uqac.lif.dag.Node;
import ca.uqac.lif.dag.Pin;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.claim.Claim;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentValue;
import ca.uqac.lif.labpal.macro.Macro;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.provenance.GraphViewer;
import ca.uqac.lif.labpal.provenance.LabPalLineageGraphUtilities;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.chart.Chart;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;

public class ExplainCallback extends TemplatePageCallback
{
	/**
	 * A flag indicating if the explanation graph should be simplified.
	 */
	protected boolean m_simplifyGraph = true;
	
	public ExplainCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-experiments");
	}

	@Override
	public void fillInputModel(String uri, Map<String,String> req_parameters, Map<String,Object> input, Map<String,byte[]> parts) throws PageRenderingException
	{
		super.fillInputModel(uri, req_parameters, input, parts);
		String datapoint_id = (String) input.get("id");
		StringBuilder out = new StringBuilder();
		PartNode node = m_server.getLaboratory().getExplanation(datapoint_id);
		if (m_simplifyGraph)
		{
			node = (PartNode) LabPalLineageGraphUtilities.simplify(node);
		}
		//explanationToHtml(node, out);
		input.put("exptree", out.toString());
		input.put("imageurl", "/explain/graph?id=" + datapoint_id);
		GraphViewer renderer = new GraphViewer();
		byte[] image = renderer.toImage(node, ChartFormat.SVG, false);
		input.put("imagesvg", hackSvg(image));
	}

	/**
	 * Transforms the original SVG.
	 * @param image The byte array with the original SVG
	 * @return A string with the modified SVG file
	 */
	protected static String hackSvg(byte[] image)
	{
		return new String(image);
	}

	protected void explanationToHtml(Node node, StringBuilder out)
	{
		out.append(
				"<li><div class=\"around-pulldown\"><div class=\"pulldown\"><a title=\"Click to see where this value comes from\" href=\"")
		.append(htmlEscape(getDataPointUrl(node))).append("\">").append(renderNode(node))
		.append("</a></div>\n");
		List<Pin<? extends Node>> parents = node.getOutputLinks(0);
		if (!parents.isEmpty())
		{
			out.append("<div class=\"pulldown-contents\"><ul>");
			for (Pin<? extends Node> pn : parents)
			{
				explanationToHtml(pn.getNode(), out);
			}
			out.append("</ul></div></div>");
		}
		out.append("</li>\n");
	}

	protected static String renderNode(Node node)
	{
		if (node instanceof PartNode)
		{
			return renderPartNode((PartNode) node);
		}
		else if (node instanceof AndNode)
		{
			return "And";
		}
		else if (node instanceof OrNode)
		{
			return "Or";
		}
		return "Unknown";
	}

	protected static String renderPartNode(PartNode node)
	{
		StringBuilder out = new StringBuilder();
		Part p = node.getPart();
		out.append(p.toString()).append(" of ");
		Object subject = node.getSubject();
		if (subject instanceof Laboratory)
		{
			out.append("this lab");
		}
		else if (subject instanceof Experiment)
		{
			out.append("Experiment #").append(((Experiment) subject).getId());
		}
		else if (subject instanceof Table)
		{
			out.append("Table #").append(((Table) subject).getId());
		}
		else if (subject instanceof Plot)
		{
			out.append("Plot #").append(((Plot) subject).getId());
		}
		/*else if (subject instanceof Macro)
		{
			out.append("Macro #").append(((Macro) subject).getId());
		}*/
		else if (subject instanceof Claim)
		{
			out.append("Claim #").append(((Claim) subject).getId());
		}
		else if (subject == null)
		{
			out.append("null");
		}
		else
		{
			out.append(subject.toString());
		}
		return out.toString();
	}

	/*@ null @*/ protected static PartNode fetchClosestLabPalObject(Node node)
	{
		if (node instanceof PartNode)
		{
			Object o = ((PartNode) node).getSubject();
			if (o instanceof Laboratory || o instanceof Experiment || o instanceof Table /*|| o instanceof Macro*/ || o instanceof Plot || o instanceof Claim)
			{
				return (PartNode) node;
			}
		}
		for (Pin<? extends Node> pin : node.getOutputLinks(0))
		{
			PartNode n = fetchClosestLabPalObject(pin.getNode());
			if (n != null)
			{
				return n;
			}
		}
		return null;
	}

	/*@ non_null @*/ public static String getDataPointUrl(/*@ null @*/ Node node)
	{
		if (node instanceof PartNode)
		{
			return getPartNodeUrl((PartNode) node); 
		}
		if (node instanceof AndNode)
		{
			return getAndNodeUrl((AndNode) node);
		}
		return "#";
	}

	protected static String getAndNodeUrl(AndNode and)
	{
		StringBuilder out = new StringBuilder();
		boolean has_base = false, has_highlight = false;
		for (Pin<? extends Node> pin : and.getOutputLinks(0))
		{
			Node child = pin.getNode();
			Node lp_child = fetchClosestLabPalObject(child);
			String url = getDataPointUrl(lp_child);
			if (url.compareTo("#") == 0)
			{
				continue;
			}
			String[] parts = url.split("&");
			if (!has_base)
			{
				out.append(parts[0]);
				has_base = true;
			}
			for (int i = 1; i < parts.length; i++)
			{
				if (parts[i].startsWith("highlight"))
				{
					String ids = parts[i].substring(parts[i].indexOf("=") + 1);
					if (!has_highlight)
					{
						out.append("&highlight=");
						has_highlight = true;
					}
					else
					{
						out.append(",");
					}
					out.append(ids);
				}
			}
		}
		return out.toString();
	}

	protected static String getPartNodeUrl(PartNode nf)
	{
		String url = "";
		Part part = nf.getPart();
		Object subject = nf.getSubject();
		if (subject instanceof Laboratory)
		{
			for (Pin<? extends Node> pin : nf.getOutputLinks(0))
			{
				Node n = pin.getNode();
				if (!(n instanceof PartNode))
				{
					continue;
				}
				return getPartNodeUrl((PartNode) n);
			}
			return "#";
		}
		if (subject instanceof Table)
		{
			url += "/table/" + ((Table) subject).getId();
			Cell c = Cell.mentionedCell(part);
			if (c != null)
			{
				url += "?highlight=" + c.getRow() + "." + c.getColumn();
			}
		}
		else if (subject instanceof Plot)
		{
			url += "/plot/" + ((Plot) subject).getId();
		}
		else if (subject instanceof Experiment)
		{
			url += "/experiment/" + ((Experiment) subject).getId();
			if (part.head() instanceof ExperimentValue)
			{
				ExperimentValue n = (ExperimentValue) part.head();
				url += "?highlight=" + n.getParameter();
				Part tail = part.tail();
				if (tail != null && tail.head() instanceof NthElement)
				{
					NthElement elem = (NthElement) tail.head();
					url += ":" + elem.getIndex();
				}
			}
		}
		else if (subject instanceof Claim)
		{
			url += "/claim/" + ((Claim) subject).getId();
		}
		else if (subject instanceof Macro)
		{
			url += "/macros?highlight=" + ((Macro) subject).getId();
			if (part.head() instanceof NthElement)
			{
				NthElement n = (NthElement) part.head();
				url += ":" + n.getIndex();
			}
		}
		else
		{
			url = "#";
		}
		return url;
	}

	/**
	 * Gets the icon class associated to a node function
	 * 
	 * @param node
	 *          The node function
	 * @return The icon class
	 */
	public static String getDataPointIconClass(Node node)
	{
		if (!(node instanceof PartNode))
		{
			return "other";
		}
		PartNode nf = (PartNode) node;
		Object o = nf.getSubject();
		if (o instanceof Experiment)
		{
			return "experiment";
		}
		if (o instanceof Table)
		{
			return "table";
		}
		/*if (o instanceof Macro)
		{
			return "macro";
		}*/
		if (o instanceof Chart)
		{
			return "plot";
		}
		return "other";
	}

}
