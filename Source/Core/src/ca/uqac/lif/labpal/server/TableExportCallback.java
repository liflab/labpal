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
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TempTable;
import ca.uqac.lif.mtnp.table.rendering.LatexTableRenderer;

import com.sun.net.httpserver.HttpExchange;

/**
 * Callback producing an table from the lab, in various
 * formats.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>dl=1</tt>: to download the table instead of displaying it. This
 *   will prompt the user to save the file in its browser</li>
 * <li><tt>id=x</tt>: mandatory; the ID of the table to display</li>
 * <li><tt>format=x</tt>: the requested table format. Currenly supports
 *   tex, csv and html.
 * </ul>
 * 
 * @author Sylvain Hallé
 *
 */
public class TableExportCallback extends WebCallback
{
	public TableExportCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/table-export", lab, assistant);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		Map<String,String> params = getParameters(t);
		int plot_id = Integer.parseInt(params.get("id"));
		Table tab = m_lab.getTable(plot_id);
		if (tab == null)
		{
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		TempTable d_tab = tab.getDataTable();
		if (params.get("format").compareToIgnoreCase("tex") == 0)
		{
			LatexTableRenderer renderer = new LatexTableRenderer(tab);
			String contents = renderer.render(d_tab.getTree(), d_tab.getColumnNames());
			response.setContents(contents);
			response.setCode(CallbackResponse.HTTP_OK);
			response.setContentType("application/x-latex");
			if (params.containsKey("dl"))
			{
				response.setAttachment(Server.urlEncode(tab.getTitle() + ".tex"));
			}
			return response;
		}
		if (params.get("format").compareToIgnoreCase("html") == 0)
		{
			response.setContents(d_tab.toHtml());
			response.setCode(CallbackResponse.HTTP_OK);
			response.setContentType(ContentType.HTML);
			if (params.containsKey("dl"))
			{
				response.setAttachment(Server.urlEncode(tab.getTitle() + ".html"));
			}
			return response;
		}
		response.setContents(d_tab.toCsv());
		response.setCode(CallbackResponse.HTTP_OK);
		response.setContentType(ContentType.TEXT);
		if (params.containsKey("dl"))
		{
			response.setAttachment(Server.urlEncode(tab.getTitle() + ".csv"));
		}
		return response;
	}
	
	@Override
	public void bundle(ZipOutputStream zos) throws IOException
	{
		Set<Integer> ids = m_lab.getTableIds();
		for (int id : ids)
		{
			Table tab = m_lab.getTable(id);
			TempTable d_tab = tab.getDataTable();
			{
				// Latex
				LatexTableRenderer renderer = new LatexTableRenderer(tab);
				String contents = renderer.render(d_tab.getTree(), d_tab.getColumnNames());
				ZipEntry ze = new ZipEntry("/table/" + id + ".tex");
				zos.putNextEntry(ze);
				zos.write(contents.getBytes());
				zos.closeEntry();
			}
			{
				// CSV
				ZipEntry ze = new ZipEntry("/table/" + id + ".csv");
				zos.putNextEntry(ze);
				zos.write(d_tab.toCsv().getBytes());
				zos.closeEntry();
			}
		}
	}
}
