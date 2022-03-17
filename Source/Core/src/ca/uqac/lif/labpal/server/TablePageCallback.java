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
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.experiment.DependencyExperimentSelector;
import ca.uqac.lif.labpal.table.CsvTableRenderer;
import ca.uqac.lif.labpal.table.LatexTableRenderer;
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
	public CallbackResponse process(HttpExchange h)
	{
		String uri = h.getRequestURI().toString();
		int id = fetchId(s_idPattern, uri);
		Table t = m_server.getLaboratory().getTable(id);
		if (uri.contains("/csv"))
		{
			CallbackResponse cbr = new CallbackResponse(h);
			cbr.setContentType(ContentType.TEXT);
			if (t == null)
			{
				cbr.setCode(CallbackResponse.HTTP_NOT_FOUND);
				cbr.setContents("No such table");
				return cbr;
			}
			CsvTableRenderer renderer = new CsvTableRenderer(t);
			cbr.setCode(CallbackResponse.HTTP_OK);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			renderer.render(new PrintStream(baos));
			cbr.setContents(baos.toString());
			if (uri.contains("?dl"))
			{
				cbr.setAttachment(Server.urlEncode(t.getTitle() + ".csv"));	
			}
			return cbr;
		}
		if (uri.contains("/html"))
		{
			CallbackResponse cbr = new CallbackResponse(h);
			cbr.setContentType(ContentType.HTML);
			if (t == null)
			{
				cbr.setCode(CallbackResponse.HTTP_NOT_FOUND);
				cbr.setContents("No such table");
				return cbr;
			}
			HtmlTableRenderer renderer = new HtmlTableRenderer(t);
			cbr.setCode(CallbackResponse.HTTP_OK);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			renderer.render(new PrintStream(baos));
			cbr.setContents(baos.toString());
			if (uri.contains("?dl"))
			{
				cbr.setAttachment(Server.urlEncode(t.getTitle() + ".html"));	
			}
			return cbr;
		}
		if (uri.contains("/tex"))
		{
			CallbackResponse cbr = new CallbackResponse(h);
			cbr.setContentType("application/x-latex");
			if (t == null)
			{
				cbr.setCode(CallbackResponse.HTTP_NOT_FOUND);
				cbr.setContents("No such table");
				return cbr;
			}
			LatexTableRenderer renderer = new LatexTableRenderer(t);
			cbr.setCode(CallbackResponse.HTTP_OK);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			renderer.render(new PrintStream(baos));
			cbr.setContents(baos.toString());
			if (uri.contains("?dl"))
			{
				cbr.setAttachment(Server.urlEncode(t.getTitle() + ".tex"));	
			}
			return cbr;
		}
		return super.process(h);
	}


	@Override
	public void fillInputModel(String uri, Map<String,String> req_parameters, Map<String,Object> input, Map<String,byte[]> parts) throws PageRenderingException
	{
		super.fillInputModel(uri, req_parameters, input, parts);
		int id = fetchId(s_idPattern, uri);
		Table t = m_server.getLaboratory().getTable(id);
		if (t == null)
		{
			throw new PageRenderingException(CallbackResponse.HTTP_NOT_FOUND, "Not found", "No such table");
		}
		if (uri.endsWith("/html"))
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
		input.put("expdeps", DependencyExperimentSelector.getDependencyList(t));
		HtmlTableRenderer renderer = new HtmlTableRenderer(t);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		renderer.render(new PrintStream(baos));
		input.put("tablecontents", baos.toString());
	}
}
