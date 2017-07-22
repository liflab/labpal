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
import java.util.Collections;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.mtnp.table.Table;

/**
 * Callback showing a list of tables
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
	public String fill(String page, Map<String,String> params, boolean is_offline)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Tables");
		{
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
		}
		out = out.replaceAll("\\{%ALL_TABLES%\\}", Matcher.quoteReplacement("<p><a class=\"btn-24 btn-all-tables\" title=\"Download all tables as a single LaTeX file\" href=\"all-tables\">Download all tables</a></p>"));
		out = out.replaceAll("\\{%SEL_TABLES%\\}", "selected");
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.TABLE));
		return out;
	}
	
	/**
	 * Produces the list of tables
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
			out.append("<td class=\"id-cell\"><a href=\"/table/").append(id).append("\" title=\"Click on table to view in new window\">");
			out.append(id).append("</a></td>");
			out.append("<td class=\"table-icon\"></td>");
			out.append("<td><a href=\"table/").append(id).append("\">").append(htmlEscape(table.getTitle())).append("</a></td>");
			out.append("<td><a class=\"btn-csv\" href=\"/table/export/").append(id).append("?format=csv&amp;dl=1\" title=\"Download as CSV\"><span class=\"text-only\">CSV</span></a></td>");
			out.append("<td><a class=\"btn-tex\" href=\"/table/export/").append(id).append("?format=tex&amp;dl=1\" title=\"Download as LaTeX\"><span class=\"text-only\">TeX</span></a></td>");
			out.append("<td><a class=\"btn-html\" href=\"/table/export/").append(id).append("?format=html&amp;dl=1\" title=\"Download as HTML\"><span class=\"text-only\">HTML</span></a></td>");
			out.append("</tr>\n");
		}
		out.append("</table>\n");
		return out.toString();
	}
	
	@Override
	public String exportToStaticHtml(String path_to_root)
	{
		String contents = super.exportToStaticHtml(path_to_root);
		// Transform URLs for individual plot buttons
		contents = contents.replaceAll("src=\"(.*?)\\.html\"", "src=\"$1.png\"");
		contents = contents.replaceAll("href=\"table/export/(.*?)\\?format=csv.*?\"", "href=\"table/$1.csv\"");
		contents = contents.replaceAll("href=\"table/export/(.*?)\\?format=tex.*?\"", "href=\"table/$1.tex\"");
		contents = contents.replaceAll("href=\"table/export/(.*?)\\?format=html.*?\"", "href=\"table/$1.html\"");
		return contents;
	}

	@Override
	public void bundle(ZipOutputStream zos) throws IOException
	{
		ZipEntry ze = new ZipEntry("tables.html");
		zos.putNextEntry(ze);
		zos.write(exportToStaticHtml("").getBytes());
		zos.closeEntry();
	}
}
