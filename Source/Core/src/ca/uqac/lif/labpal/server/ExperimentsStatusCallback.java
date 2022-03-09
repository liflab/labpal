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
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * A callback that produces a JSON string indicating the status and progression
 * of each experiment in the lab. 
 * @author Sylvain Hallé
 */
public class ExperimentsStatusCallback extends LabStatusCallback
{
	/**
	 * The pattern to extract the experiment ID from the URL.
	 */
	protected static final Pattern s_idPattern = Pattern.compile("experiments/status/(\\d+)");
	
	public ExperimentsStatusCallback(LabPalServer server, Method m, String path)
	{
		super(server, m, path);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse cbr = new CallbackResponse(t);
		cbr.setCode(CallbackResponse.HTTP_OK);
		cbr.setContentType(ContentType.JSON);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		int exp_id = TemplatePageCallback.fetchId(s_idPattern, t.getRequestURI().toString());
		if (exp_id > 0)
		{
			Experiment e = m_server.getLaboratory().getExperiment(exp_id);
			if (e == null)
			{
				cbr.setCode(CallbackResponse.HTTP_NOT_FOUND);
				cbr.setContents("No such experiment");
				return cbr;
			}
			out.println("[");
			out.print("\"" + e.getStatus() + "\",");
			out.print(e.getProgression() + ",");
			out.print("\"" + e.getStartDate() + "\",");
			out.print("\"" + e.getEndDate() + "\",");
			out.print("\"" + e.getTotalDuration().toString() + "\"");
			out.print("]");
			cbr.setContents(baos.toString());
			return cbr;
		}
		out.println("{");
		boolean first = true;
		for (Experiment e : m_server.getLaboratory().getExperiments())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				out.println(",");
			}
			out.print("\"" + e.getId() + "\" : [");
			if (m_server.getLaboratory().isQueued(e))
			{
				out.print("\"QUEUED\", ");
			}
			else
			{
				out.print("\"" + e.getStatus() + "\", ");
			}
			out.print(e.getProgression());
			out.print("]");
		}
		out.println("}");
		cbr.setContents(baos.toString());
		return cbr;
	}
}
