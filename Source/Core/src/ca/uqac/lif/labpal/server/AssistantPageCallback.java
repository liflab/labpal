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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.labpal.assistant.AssistantRun;
import ca.uqac.lif.labpal.experiment.Experiment;

public class AssistantPageCallback extends TemplatePageCallback
{

	public AssistantPageCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-assistant");
	}

	@Override
	public void fillInputModel(HttpExchange h, Map<String,Object> input)  throws PageRenderingException
	{
		super.fillInputModel(h, input);
		input.put("title", "Assistant");
		Set<Experiment> exps = new HashSet<Experiment>();
		if (input.containsKey("enqueue"))
		{
			// Adding groups
			Set<Integer> groups = new HashSet<Integer>();
			for (String key : input.keySet())
			{
				if (key.startsWith("top-checkbox"))
				{
					String[] parts = key.split("-");
					int g_id = Integer.parseInt(parts[2]);
					groups.add(g_id);
				}
			}
			// Adding experiments submitted from experiments page to the queue
			for (String key : input.keySet())
			{
				if (key.startsWith("exp-chh-g"))
				{
					String[] parts = key.split("-");
					int g_id = Integer.parseInt(parts[3]);
					int exp_id = Integer.parseInt(parts[4]);
					if (groups.contains(g_id))
					{
						Experiment e = m_server.getLaboratory().getExperiment(exp_id);
						if (e != null)
						{
							exps.add(e);
						}
					}
				}
				if (key.startsWith("exp-chk-g"))
				{
					String[] parts = key.split("-");
					int exp_id = Integer.parseInt(parts[3]);
					Experiment e = m_server.getLaboratory().getExperiment(exp_id);
					if (e != null)
					{
						exps.add(e);
					}
				}
			}
			int added = m_server.getLaboratory().getAssistant().addToQueue(exps);
			input.put("message", added + " experiment(s) enqueued");
		}
		if (input.containsKey("start"))
		{
			// Start run with current queue contents
			AssistantRun run = m_server.getLaboratory().getAssistant().enqueueCurrent();
			input.put("message", "Run #" + run.getId() + " added to assistant's jobs");
		}
		String message = null;
		for (String key : input.keySet())
		{
			if (key.startsWith("stop"))
			{
				// Stop a run
				String[] parts = key.split("-");
				int run_id = Integer.parseInt(parts[1]);
				AssistantRun run = m_server.getLaboratory().getAssistant().getRun(run_id);
				if (run != null)
				{
					run.stop(true);
				}
				message = "Run #" + run_id + " interrupted";
			}
			if (key.startsWith("delete"))
			{
				// Stop a run
				String[] parts = key.split("-");
				int run_id = Integer.parseInt(parts[1]);
				m_server.getLaboratory().getAssistant().deleteRun(run_id);
				message = "Run #" + run_id + " deleted";
			}
		}
		if (message != null)
		{
			input.put("message", message);
		}
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
