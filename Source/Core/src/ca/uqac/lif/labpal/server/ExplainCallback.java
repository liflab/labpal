/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2022 Sylvain Hallé

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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.macro.Macro;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.provenance.GraphViewer;
import ca.uqac.lif.labpal.provenance.TrackedValue;
import ca.uqac.lif.dag.Node;
import ca.uqac.lif.dag.Pin;
import ca.uqac.lif.labpal.Claim;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentValue;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.Chart;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.labpal.table.Table;

/**
 * Callback producing a provenance tree from one of the lab's data points.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>dl=1</tt>: to download the image instead of displaying it. This will
 * prompt the user to save the file in its browser</li>
 * <li><tt>id=x</tt>: mandatory; the ID of the plot to display</li>
 * <li><tt>format=x</tt>: the requested image format. Currenly supports pdf,
 * dumb (text), png and gp (raw data file for Gnuplot).
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
	public String fill(String s, Map<String, String> params, boolean is_offline)
	{

		String datapoint_id = params.get("id");
		s = s.replaceAll("\\{%TITLE%\\}", "Explanation");
		s = s.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.BINOCULARS));
		PartNode node = m_lab.getExplanation(datapoint_id);
		if (node == null)
		{
			s = s.replaceAll("\\{%EXPLANATION%\\}",
					"<p>There does not seem to be an explanation available for this data point. Some data points are available only when the experiments they depend on have been executed.</p>");
			return s;
		}
		s = s.replaceAll("\\{%IMAGE_URL%\\}",
				Matcher.quoteReplacement("/provenance-graph?id=" + datapoint_id));
		StringBuilder out = new StringBuilder();
		out.append("<ul class=\"explanation\">\n");
		explanationToHtml(node, out);
		out.append("</ul>\n");
		s = s.replaceAll("\\{%EXPLANATION%\\}", Matcher.quoteReplacement(out.toString()));

		return s;
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
		else if (subject instanceof Macro)
		{
			out.append("Macro #").append(((Macro) subject).getId());
		}
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
			if (o instanceof Laboratory || o instanceof Experiment || o instanceof Table || o instanceof Macro || o instanceof Plot)
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
		if (subject instanceof Table)
		{
			url += "/table?id=" + ((Table) subject).getId();
			Cell c = Cell.mentionedCell(part);
			if (c != null)
			{
				url += "&highlight=" + c.getRow() + "." + c.getColumn();
			}
		}
		else if (subject instanceof Plot)
		{
			url += "/plot?id=" + ((Plot) subject).getId();
		}
		else if (subject instanceof Experiment)
		{
			url += "/experiment?id=" + ((Experiment) subject).getId();
			if (part.head() instanceof ExperimentValue)
			{
				ExperimentValue n = (ExperimentValue) part.head();
				url += "&highlight=" + n.getParameter();
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
			// TODO
		}
		else if (subject instanceof Macro)
		{
			url += "/macros?highlight=" + ((Table) subject).getId();
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
		if (o instanceof Macro)
		{
			return "macro";
		}
		if (o instanceof Chart)
		{
			return "plot";
		}
		return "other";
	}

	@Override
	public void addToZipBundle(ZipOutputStream zos) throws IOException
	{
		Set<Integer> ids = m_lab.getTableIds();
		Set<TrackedValue> rendered_ids = new HashSet<TrackedValue>();
		for (int id : ids)
		{
			Table tab = m_lab.getTable(id);
			renderTableTree(zos, tab, rendered_ids);
			zos.closeEntry();
		}
	}

	void renderTableTree(ZipOutputStream zos, Table tab, Set<TrackedValue> rendered_ids)
	{
		Spreadsheet s = tab.getSpreadsheet();
		for (int row = 0; row < s.getHeight(); row++)
		{
			for (int col = 0; col < s.getWidth(); col++)
			{
				WriteZipElement(zos, tab, s, col, row, rendered_ids);
			}
		}
	}

	public void WriteZipElement(ZipOutputStream zos, Table tab, Spreadsheet s, int col, int row, Set<TrackedValue> rendered_ids)
	{
		Table dep_tab = tab.dependsOn(col, row);
		if (dep_tab == null)
		{
			return;
		}
		TrackedValue dp_id = new TrackedValue(null, Part.all, dep_tab);
		if (rendered_ids.contains(dp_id))
		{
			// This table has already been processed
			return;
		}
		rendered_ids.add(dp_id);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("id", dep_tab.getId() + "");
		ZipEntry ze = new ZipEntry("table/" + dp_id + ".html");
		try
		{
			zos.putNextEntry(ze);
			zos.write(exportToStaticHtml("../", params).getBytes());
			zos.closeEntry();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		// Render the image at the same time
		renderImage(zos, "T" + dep_tab.getId());
	}

	/**
	 * Exports the page contents into a string destined to be viewed offline.
	 * @param path_to_root The path to the root of the static file structure
	 * where this page is going to be placed
	 * @param params The parameters passed to the online page in order to be
	 * rendered
	 * @return A character string containing a complete stand-alone version
	 * of the page
	 */
	public String exportToStaticHtml(String path_to_root, HashMap<String, String> params)
	{
		String file = readTemplateFile();
		String contents = render(file, params, true);
		contents = createStaticLinks(contents);
		contents = relativizeUrls(contents, path_to_root);
		return contents;
	}

	/**
	 * Renders an image for the explanation of a datapoint, and saves it into a
	 * zip file.
	 * @param zos The output stream where the file contents are written
	 * @param datapoint_id The ID of the datapoint whose image needs to be
	 * generated
	 */
	protected void renderImage(ZipOutputStream zos, String datapoint_id)
	{
		if (!GraphViewer.s_dotPresent)
		{
			return;
		}
		PartNode p_node = m_lab.getExplanation(datapoint_id);
		GraphViewer renderer = new GraphViewer();
		// Render as SVG
		byte[] image = renderer.toImage(p_node, ChartFormat.SVG, true);
		String file_contents = new String(image);
		// Change links in SVG
		file_contents = createStaticLinks(file_contents);
		file_contents = relativizeUrls(file_contents, "../");
		if (image != null)
		{
			ZipEntry ze = new ZipEntry("table/" + datapoint_id + ".svg");
			try
			{
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		// Render as PNG
		image = renderer.toImage(p_node, ChartFormat.PNG, true);
		if (image != null)
		{
			ZipEntry ze = new ZipEntry("table/" + datapoint_id + ".png");
			try
			{
				zos.putNextEntry(ze);
				zos.write(image);
				zos.closeEntry();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
