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

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.labpal.assistant.AssistantRun;

/**
 * A callback that produces a JSON string indicating the status and progression
 * of each run of an assistant. 
 * @author Sylvain Hallé
 */
public class AssistantStatusCallback extends RestCallback
{
	protected LabPalServer m_server;
	
	public AssistantStatusCallback(LabPalServer server, Method m, String path)
	{
		super(m, path);
		m_server = server;
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse cbr = new CallbackResponse(t);
		cbr.setCode(CallbackResponse.HTTP_OK);
		cbr.setContentType(ContentType.JSON);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		out.println("{");
		boolean first = true;
		for (AssistantRun r : m_server.getLaboratory().getAssistant().getRuns())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				out.println(",");
			}
			out.print("\"" + r.getId() + "\" : [");
			if (r.isRunning())
			{
				out.print("true,");
			}
			else
			{
				out.print("false,");
			}
			out.print(r.getProgression());
			out.print("]");
		}
		out.println("}");
		cbr.setContents(baos.toString());
		return cbr;
	}
}
