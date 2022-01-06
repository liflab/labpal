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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.spreadsheet.Cell;

/**
 * Callback producing a file from one of the lab's tables, in various formats.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>id=x</tt>: mandatory; the ID of the table to display</li>
 * <li><tt>format=x</tt>: the requested image format. Currenly supports tex, csv
 * and html.
 * </ul>
 * 
 * @author Sylvain Hallé
 *
 */
public class TablePageCallback extends TemplatePageCallback
{
  /**
   * Whether to render the tables as "plain". This is mostly to debug the
   * highlighting of tables with the normal renderer.
   */
  protected static boolean s_renderPlain = false;

  public TablePageCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/table", lab, assistant);
  }

  @Override
  public String fill(String s, Map<String, String> params, boolean is_offline)
  {
    List<String> path_parts = getParametersFromPath(params);
    int tab_id = -1;
    if (!path_parts.isEmpty())
    {
      tab_id = Integer.parseInt(path_parts.get(0));
    }
    else if (params.containsKey("id"))
    {
      tab_id = Integer.parseInt(params.get("id"));
    }
    Table tab = m_lab.getTable(tab_id);
    String highlight = "";
    if (params.containsKey("highlight"))
    {
      // If cells have to be highlighted, display the table without
      // sorting the cells
      highlight = params.get("highlight");
    }
    HtmlTableRenderer renderer = new HtmlTableRenderer(tab);
    if (params.containsKey("highlight"))
    {
    	renderer.highlight(getCellsToHighlight(params.get(highlight)));
    }
    renderer.setExplainUrlPrefix("../explain");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    renderer.render(new PrintStream(baos));
    String table_html = baos.toString();
    s = s.replaceAll("\\{%TITLE%\\}", Matcher.quoteReplacement(tab.getTitle()));
    s = s.replaceAll("\\{%TABLE%\\}", Matcher.quoteReplacement(table_html));
    String desc = tab.getDescription();
    if (desc != null && !desc.isEmpty())
    {
      s = s.replaceAll("\\{%DESCRIPTION%\\}", Matcher.quoteReplacement(desc));
    }
    String nick = tab.getNickname();
    if (nick != null && !nick.isEmpty())
    {
      s = s.replaceAll("\\{%NICKNAME%\\}", Matcher.quoteReplacement(nick));
    }
    s = s.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.TABLE));
    return s;
  }

  protected Set<Cell> getCellsToHighlight(String highlight)
  {
    Set<Cell> to_highlight = new HashSet<Cell>();
    String[] ids = highlight.split(",");
    for (String id : ids)
    {
      if (id.trim().isEmpty())
        continue;
      String[] parts = id.split(Pattern.quote(","));
      int row = Integer.parseInt(parts[1]);
      int col = Integer.parseInt(parts[2]);
      to_highlight.add(Cell.get(col, row));
    }
    return to_highlight;
  }

  public String exportToStaticHtml(int id)
  {
    String file = readTemplateFile();
    Map<String, String> params = new HashMap<String, String>();
    params.put("id", Integer.toString(id));
    String contents = render(file, params, true);
    contents = createStaticLinks(contents);
    contents = relativizeUrls(contents, "../");
    contents = contents.replaceAll("href=\"../explain(.)id=(T.+)\"", "href=\"../table/$2.html\"");
    return contents;
  }

  @Override
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {
    Set<Integer> ids = m_lab.getTableIds();
    for (int id : ids)
    {
      ZipEntry ze = new ZipEntry("table/" + id + ".html");
      zos.putNextEntry(ze);
      zos.write(exportToStaticHtml(id).getBytes());
      zos.closeEntry();
      // No need to export to CSV: already done by TableExportCallback
    }
  }
}
