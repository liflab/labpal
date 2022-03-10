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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.experiment.DependencyExperimentSelector;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.plot.Plot;

/**
 * A callback that produces a JSON string indicating the status and progression
 * of each table in the lab. 
 * @author Sylvain Hallé
 */
public class PlotsStatusCallback extends TemplatePageCallback
{
	/**
	 * The pattern to extract the experiment ID from the URL.
	 */
	protected static final Pattern s_idPattern = Pattern.compile("plots/status/(\\d+)");

	public PlotsStatusCallback(LabPalServer server, Method m, String path)
	{
		super(server, m, path, null, null);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse cbr = new CallbackResponse(t);
		cbr.setCode(CallbackResponse.HTTP_OK);
		cbr.setContentType(ContentType.JSON);
		int id = fetchId(t);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		if (id <= 0)
		{
			// All plots
			out.println("{");
			boolean first = true;
			for (Plot p : m_server.getLaboratory().getPlots())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					out.println(",");
				}
				out.print("\"" + p.getId() + "\" : [");
				out.print("\"" + p.getStatus() + "\", ");
				out.print(p.getProgression());
				out.print("]");
			}
			out.println("}");
		}
		else
		{
			Plot p = m_server.getLaboratory().getPlot(id);
			if (p == null)
			{
				cbr.setCode(CallbackResponse.HTTP_NOT_FOUND);
				return cbr;
			}
			out.print("[");
			out.print("\"" + p.getStatus() + "\", " + p.getProgression() + ", ");
			List<Experiment> deps = DependencyExperimentSelector.getDependencyList(p);
			boolean d_first = true;
			out.print("[");
			for (Experiment e : deps)
			{
				if (d_first)
				{
					d_first = false;
				}
				else
				{
					out.print(", ");
				}
				out.print("[\"" + e.getId() + "\", \"" + e.getStatus() + "\"]");
			}
			out.print("]");
			out.print("]");
		}
		cbr.setContents(baos.toString());
		return cbr;
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
