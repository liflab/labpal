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
import java.io.PrintStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.labpal.table.Table;

public class TablePageCallback extends TemplatePageCallback
{
	/**
	 * The pattern to extract the experiment ID from the URL.
	 */
	protected static final Pattern s_idPattern = Pattern.compile("table/(\\d+)");
	
	public TablePageCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-tables");
	}

	@Override
	public void fillInputModel(HttpExchange h, Map<String,Object> input) throws PageRenderingException
	{
		super.fillInputModel(h, input);
		int id = fetchId(h);
		Table t = m_server.getLaboratory().getTable(id);
		if (t == null)
		{
			throw new PageRenderingException(CallbackResponse.HTTP_NOT_FOUND, "Not found", "No such table");
		}
		if (h.getRequestURI().toString().endsWith("/html"))
		{
			input.put("onlytable", true);
		}
		else
		{
			input.put("onlytable", false);
		}
		input.put("id", id);
		input.put("title", "Table " + id);
		input.put("table", t);
		HtmlTableRenderer renderer = new HtmlTableRenderer(t);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		renderer.render(new PrintStream(baos));
		input.put("tablecontents", baos.toString());
	}
	
	protected static int fetchId(HttpExchange h)
	{
		String uri = h.getRequestURI().toString();
		Matcher mat = s_idPattern.matcher(uri);
		if (!mat.find())
		{
			return -1; // No ID
		}
		return Integer.parseInt(mat.group(1));
	}
}
