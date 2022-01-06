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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.GraphvizRenderer;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.macro.MacroNode;
import ca.uqac.lif.mtnp.plot.PlotNode;
import ca.uqac.lif.petitpoucet.AggregateFunction;
import ca.uqac.lif.labpal.provenance.DotProvenanceTreeRenderer;
import ca.uqac.lif.labpal.ExperimentValue;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.ProvenanceNode;
import ca.uqac.lif.mtnp.table.HardTable;
import ca.uqac.lif.mtnp.table.PrimitiveValue;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.mtnp.table.TableCellNode;
import ca.uqac.lif.mtnp.table.TableFunctionNode;
import ca.uqac.lif.mtnp.table.TableNode;
import ca.uqac.lif.mtnp.table.Table.CellCoordinate;

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
    ProvenanceNode node = m_lab.getDataTracker().explain(datapoint_id);
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
    explanationToHtml(node, "", out);
    out.append("</ul>\n");
    s = s.replaceAll("\\{%EXPLANATION%\\}", Matcher.quoteReplacement(out.toString()));

    return s;
  }

  protected void explanationToHtml(ProvenanceNode node, String parent_id, StringBuilder out)
  {
    out.append(
        "<li><div class=\"around-pulldown\"><div class=\"pulldown\"><a title=\"Click to see where this value comes from\" href=\"")
    .append(htmlEscape(getDataPointUrl(node))).append("\">").append(node)
    .append("</a></div>\n");
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

  public static String getDataPointUrl(NodeFunction nf)
  {
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
        return "/table?id=" + first_owner.getId() + "&highlight=" + highlight_string.toString();
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
      return "/table?id=" + tcn.getOwner().getId() + "&highlight=" + highlight_string.toString();
    }
    else if (nf instanceof TableFunctionNode)
    {
      TableFunctionNode tcn = (TableFunctionNode) nf;
      return "/table?id=" + tcn.getOwner().getId();
    }
    else if (nf instanceof PlotNode)
    {
      PlotNode tcn = (PlotNode) nf;
      return "/plot?id=" + tcn.getOwner().getId();
    }
    else if (nf instanceof MacroNode)
    {
      MacroNode tcn = (MacroNode) nf;
      return "/macros" + "?highlight=" + tcn.getDataPointId() + "#" + tcn.getOwner().getId();
    }
    else if (nf instanceof ExperimentValue)
    {
      ExperimentValue ev = (ExperimentValue) nf;
      highlight_string.append(ev.getDataPointId());
      return "/experiment?id=" + ev.getOwner().getId() + "&highlight="
      + highlight_string.toString();
    }
    return "#";
  }

  public static String getDataPointUrl(ProvenanceNode node)
  {
    NodeFunction nf = node.getNodeFunction();
    return getDataPointUrl(nf);
  }

  /**
   * Gets the icon class associated to a node function
   * 
   * @param nf
   *          The node function
   * @return The icon class
   */
  public static String getDataPointIconClass(NodeFunction nf)
  {
    String dp_id = nf.getDataPointId();
    if (dp_id.startsWith("E"))
    {
      return "experiment";
    }
    if (dp_id.startsWith("T"))
    {
      return "table";
    }
    if (dp_id.startsWith("M"))
    {
      return "macro";
    }
    if (dp_id.startsWith("P"))
    {
      return "plot";
    }
    return "other";
  }

  @Override
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {
    Set<Integer> ids = m_lab.getTableIds();
    Set<String> rendered_ids = new HashSet<String>();
    for (int id : ids)
    {
      Table tab = m_lab.getTable(id);
      HardTable tbl = tab.getDataTable();
      renderTableTree(zos, tab, tbl.getTree(), tbl.getColumnNames(), rendered_ids);
      zos.closeEntry();
    }
  }

  String renderTableTree(ZipOutputStream zos, Table tab, TableNode node, String[] sort_order, Set<String> rendered_ids)
  {
    int width = sort_order.length;
    StringBuilder out = new StringBuilder();
    if (node == null || (node.m_children.isEmpty()))
    {
      return "";
    }
    List<PrimitiveValue> values = new ArrayList<PrimitiveValue>();
    renderRecursive(zos, tab, node, values, out, width, rendered_ids);
    return out.toString();
  }

  protected void renderRecursive(ZipOutputStream zos, Table tab, TableNode cur_node,
      List<PrimitiveValue> values, StringBuilder out, int max_depth, Set<String> rendered_ids)
  {
    if (values != null && values.size() > 0)
    {
      WriteZipElement(zos, tab, out, values, cur_node.countLeaves(), max_depth, cur_node, rendered_ids);
    }
    boolean first_child = true;
    for (TableNode child : cur_node.m_children)
    {
      values.add(child.getValue());
      if (first_child)
      {
        first_child = false;
      }
      renderRecursive(zos, tab, child, values, out, max_depth, rendered_ids);
      values.remove(values.size() - 1);
    }
  }

  public void WriteZipElement(ZipOutputStream zos, Table tab, StringBuilder out,
      List<PrimitiveValue> values, int nb_children, int max_depth, TableNode node,
      Set<String> rendered_ids)
  {
    List<CellCoordinate> coordinates = node.getCoordinates();
    if (coordinates.size() > 0)
    {
      CellCoordinate cc = coordinates.get(0);
      String dp_id = "";
      NodeFunction nf = tab.dependsOn(cc.row, cc.col);
      if (nf != null)
      {
        dp_id = nf.getDataPointId();
      }
      if (rendered_ids.contains(dp_id))
      {
        // This file has already been rendered
        return;
      }
      rendered_ids.add(dp_id);
      HashMap<String, String> params = new HashMap<String, String>();
      params.put("id", dp_id);
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
      renderImage(zos, dp_id);
    }
  }

  public String exportToStaticHtml(String path_to_root, HashMap<String, String> params)
  {
    String file = readTemplateFile();
    String contents = render(file, params, true);
    contents = createStaticLinks(contents);
    contents = relativizeUrls(contents, path_to_root);
    return contents;
  }

  protected void renderImage(ZipOutputStream zos, String datapoint_id)
  {
    if (!GraphvizRenderer.s_dotPresent)
    {
      return;
    }
    ProvenanceNode p_node = m_lab.getDataTracker().explain(datapoint_id);
    DotProvenanceTreeRenderer renderer = new DotProvenanceTreeRenderer();
    // Render as SVG
    byte[] image = renderer.toImage(p_node, "svg");
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
    image = renderer.toImage(p_node, "png");
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
