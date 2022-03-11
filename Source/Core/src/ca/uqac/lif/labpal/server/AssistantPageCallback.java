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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.labpal.assistant.AssistantRun;
import ca.uqac.lif.labpal.assistant.BalanceObjects;
import ca.uqac.lif.labpal.assistant.ExperimentScheduler;
import ca.uqac.lif.labpal.assistant.RandomDecimate;
import ca.uqac.lif.labpal.assistant.Shuffle;
import ca.uqac.lif.labpal.assistant.Subsample;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.PlotExperimentSelector;
import ca.uqac.lif.labpal.experiment.TableExperimentSelector;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.table.Table;

public class AssistantPageCallback extends TemplatePageCallback
{
	/**
	 * The pattern to extract the plot ID from the URL.
	 */
	protected static final Pattern s_plotIdPattern = Pattern.compile("assistant/enqueue/plot/(\\d+)");

	/**
	 * The pattern to extract the table ID from the URL.
	 */
	protected static final Pattern s_tableIdPattern = Pattern.compile("assistant/enqueue/table/(\\d+)");

	public AssistantPageCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-assistant");
	}

	@Override
	public void fillInputModel(String uri, Map<String,String> req_parameters, Map<String,Object> input, Map<String,byte[]> req_parts) throws PageRenderingException
	{
		super.fillInputModel(uri, req_parameters, input, req_parts);
		input.put("title", "Assistant");
		if (uri.contains("enqueue/plot"))
		{
			enqueuePlot(uri, input);
		}
		if (uri.contains("enqueue/table"))
		{
			enqueueTable(uri, input);
		}
		Set<Integer> groups = fetchGroupIds(input);
		List<Experiment> exps = fetchExperiments(input, groups);
		if (input.containsKey("enqueue"))
		{
			enqueueExperiments(input, exps);
		}
		if (input.containsKey("dequeue"))
		{
			dequeueExperiments(input, exps);
		}
		if (input.containsKey("reset"))
		{
			resetExperiments(input, exps);
		}
		if (input.containsKey("scheduler"))
		{
			applyScheduler(input);
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
				int run_id = Integer.parseInt(parts[2]);
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
				int run_id = Integer.parseInt(parts[2]);
				m_server.getLaboratory().getAssistant().deleteRun(run_id);
				message = "Run #" + run_id + " deleted";
			}
		}
		if (message != null)
		{
			input.put("message", message);
		}
	}
	
	protected void applyScheduler(Map<String,Object> input)
	{
		if (!input.containsKey("scheduler-type"))
		{
			input.put("error", "No scheduler specified");
		}
		String scheduler_type = (String) input.get("scheduler-type");
		ExperimentScheduler scheduler = null;
		switch (scheduler_type)
		{
		case "balance":
			scheduler = new BalanceObjects(m_server.getLaboratory().getPlots(), m_server.getLaboratory().getTables());
			input.put("message", "Balance objects scheduler applied on experiment queue");
			break;
		case "decimate":
			scheduler = new RandomDecimate();
			input.put("message", "Random decimate scheduler applied on experiment queue");
			break;
		case "shuffle":
			scheduler = new Shuffle();
			input.put("message", "Shuffle scheduler applied on experiment queue");
			break;
		case "subsample":
			scheduler = new Subsample(0.5f, m_server.getLaboratory().getPlots(), m_server.getLaboratory().getTables());
			input.put("message", "Subsampling at 50% applied on experiment queue");
			break;
		default:
			input.put("error", "Unrecognized scheduler");
		}
		if (scheduler != null)
		{
			m_server.getLaboratory().getAssistant().apply(scheduler);
		}
	}
	
	protected void enqueueExperiments(Map<String,Object> input, List<Experiment> exps)
	{
		int processed = m_server.getLaboratory().getAssistant().addToQueue(exps);
		input.put("message", processed + " experiment(s) enqueued");
	}
	
	protected void dequeueExperiments(Map<String,Object> input, List<Experiment> exps)
	{
		int processed = m_server.getLaboratory().getAssistant().removeFromQueue(exps);
		input.put("message", processed + " experiment(s) dequeued");
	}
	
	protected void resetExperiments(Map<String,Object> input, List<Experiment> exps)
	{
		int processed = exps.size();
		for (Experiment e : exps)
		{
			e.reset();
		}
		input.put("message", processed + " experiment(s) reset");
	}
	
	protected void enqueueTable(String uri, Map<String,Object> input) throws PageRenderingException
	{
		int table_id = fetchId(s_tableIdPattern, uri);
		Table t = m_server.getLaboratory().getTable(table_id);
		if (t == null)
		{
			throw new PageRenderingException(CallbackResponse.HTTP_NOT_FOUND, "Not found", "No such table");
		}
		TableExperimentSelector selector = new TableExperimentSelector(m_server.getLaboratory(), t);
		int added = m_server.getLaboratory().getAssistant().addToQueue(selector.select());
		input.put("message", added + " experiment(s) enqueued to generate Table " + table_id);
	}
	
	protected void enqueuePlot(String uri, Map<String,Object> input) throws PageRenderingException
	{
		int plot_id = fetchId(s_plotIdPattern, uri);
		Plot p = m_server.getLaboratory().getPlot(plot_id);
		if (p == null)
		{
			throw new PageRenderingException(CallbackResponse.HTTP_NOT_FOUND, "Not found", "No such plot");
		}
		PlotExperimentSelector selector = new PlotExperimentSelector(m_server.getLaboratory(), p);
		int added = m_server.getLaboratory().getAssistant().addToQueue(selector.select());
		input.put("message", added + " experiment(s) enqueued to generate Plot " + plot_id);
	}
	
	protected Set<Integer> fetchGroupIds(Map<String,Object> input)
	{
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
		return groups;
	}
	
	protected List<Experiment> fetchExperiments(Map<String,Object> input, Set<Integer> groups)
	{
		List<Experiment> exps = new ArrayList<Experiment>();
		for (String key : input.keySet())
		{
			if (key.startsWith("exp-chh-g"))
			{
				if (input.get(key).toString().compareTo("1") == 0)
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
			}
			if (key.startsWith("exp-chk-g"))
			{
				String[] parts = key.split("-");
				int exp_id = Integer.parseInt(parts[4]);
				Experiment e = m_server.getLaboratory().getExperiment(exp_id);
				if (e != null)
				{
					exps.add(e);
				}
			}
		}
		Collections.sort(exps);
		return exps;
	}
}
