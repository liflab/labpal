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

import java.io.IOException;
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
import ca.uqac.lif.mtnp.table.HardTable;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.Table.CellCoordinate;
import ca.uqac.lif.mtnp.table.TableCellNode;
import ca.uqac.lif.mtnp.table.rendering.HtmlTableNodeRenderer;
import ca.uqac.lif.mtnp.table.rendering.PlainTableRenderer;

/**
 * Callback producing a file from one of the lab's tables, in various
 * formats.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>id=x</tt>: mandatory; the ID of the table to display</li>
 * <li><tt>format=x</tt>: the requested image format. Currenly supports
 *   tex, csv and html.
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
	public String fill(String s, Map<String,String> params, boolean is_offline)
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
		if (tab == null)
		{
			return "";
		}
		HardTable tbl = tab.getDataTable();
		String highlight = "";
		String table_html = "";
		if (params.containsKey("highlight"))
		{
			// If cells have to be highlighted, display the table without
			// sorting the cells
			highlight = params.get("highlight");
		}
		if (s_renderPlain && params.containsKey("highlight"))
		{
			PlainTableRenderer renderer = new PlainTableRenderer(tab, getCellsToHighlight(highlight));
			table_html = renderer.render();
		}
		else
		{
			HtmlTableNodeRenderer renderer = new HtmlTableNodeRenderer(tab, getCellsToHighlight(highlight));
			renderer.setExplainUrlPrefix("../explain");
			table_html = renderer.render(tbl.getTree(), tbl.getColumnNames());
		}
		s = s.replaceAll("\\{%TITLE%\\}", Matcher.quoteReplacement(tab.getTitle()));
		s = s.replaceAll("\\{%TABLE%\\}", Matcher.quoteReplacement(table_html));
		String desc = tab.getDescription();
		if (desc != null && !desc.isEmpty())
		{
			s = s.replaceAll("\\{%DESCRIPTION%\\}", Matcher.quoteReplacement(desc));
		}
		s = s.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.TABLE));
		return s;
	}
	
	protected Set<CellCoordinate> getCellsToHighlight(String highlight)
	{
		// TODO: we should call TableCellNode to get the x,y coordinates of a datapoint
		Set<CellCoordinate> to_highlight = new HashSet<CellCoordinate>();
		String[] ids = highlight.split(",");
		for (String id : ids)
		{
			if (id.trim().isEmpty())
				continue;
			String[] parts = id.split(Pattern.quote(TableCellNode.s_separator));
			int row = Integer.parseInt(parts[1]);
			int col = Integer.parseInt(parts[2]);
			to_highlight.add(new Table.CellCoordinate(row, col));
		}
		return to_highlight;
	}
	
	public String exportToStaticHtml(int id)
	{
		String file = readTemplateFile();
		Map<String,String> params = new HashMap<String,String>();
		params.put("id", Integer.toString(id));
		String contents = render(file, params, true);
		contents = createStaticLinks(contents);
		contents = relativizeUrls(contents, "../");
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
		}
	}
}
