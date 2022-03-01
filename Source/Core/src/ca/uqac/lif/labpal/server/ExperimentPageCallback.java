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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.labpal.experiment.Experiment;

public class ExperimentPageCallback extends TemplatePageCallback
{
	/**
	 * The pattern to extract the experiment ID from the URL.
	 */
	protected static final Pattern s_idPattern = Pattern.compile("experiment/(\\d+)");

	public ExperimentPageCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-experiments");
	}

	@Override
	public void fillInputModel(HttpExchange h, Map<String,Object> input) throws PageRenderingException
	{
		int id = fetchId(s_idPattern, h);
		Experiment e = m_server.getLaboratory().getExperiment(id);
		if (e == null)
		{
			throw new PageRenderingException(CallbackResponse.HTTP_NOT_FOUND, "Not found", "No such experiment");
		}
		String uri = h.getRequestURI().toString();
		if (uri.contains("output/html"))
		{
			String table = formatTable(e.getOutputParameters());
			input.put("outparams", table);
		}
		else
		{
			super.fillInputModel(h, input);
			input.put("id", id);
			input.put("title", "Experiment " + id);
			Map<String,String> ins = formatParameters(e.getInputParameters());
			input.put("inputs", ins);
			Map<String,String> outs = formatParameters(e.getOutputParameters());
			input.put("outputs", outs);
		}
	}
	
	protected static String formatTable(Map<String,Object> params)
	{
		if (params.isEmpty())
		{
			return "<p>No parameter is defined.</p>";
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.println("<table>");
		for (Map.Entry<String,Object> entry : params.entrySet())
		{
			ps.print("<tr><th>");
			ps.print(entry.getKey());
			ps.print("</th><td>");
			ps.print(entry.getValue().toString());
			ps.println("</td></tr>");
		}
		ps.println("</table>");
		return baos.toString();
	}

	protected static Map<String,String> formatParameters(Map<String,Object> params)
	{
		Map<String,String> formatted = new HashMap<String,String>();
		for (Map.Entry<String,Object> e : params.entrySet())
		{
			formatted.put(e.getKey(), e.getValue().toString());
		}
		return formatted;
	}

}

