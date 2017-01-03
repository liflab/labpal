/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Group;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback to display the list of experiments in the lab
 * 
 * @author Sylvain Hallé
 *
 */
public class ExperimentsPageCallback extends TemplatePageCallback
{
	/**
	 * The regex pattern used to parse incoming form data
	 */
	protected static final transient Pattern s_pattern = Pattern.compile("exp-chk-(\\d+)");
	
	public ExperimentsPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/experiments", lab, assistant);
		setMethod(Method.POST);
		ignoreMethod();
	}
	
	protected ExperimentsPageCallback(String path, Laboratory lab, LabAssistant assistant)
	{
		super(path, lab, assistant);
		setMethod(Method.POST);
		ignoreMethod();
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Experiments");
		out = out.replaceAll("\\{%SEL_EXPERIMENTS%\\}", "selected");
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.ERLENMEYER));
		String message = "";
		if (params.containsKey("queue"))
		{
			message = queue(params);
		}
		if (params.containsKey("reset"))
		{
			message = reset(params);
		}
		if (params.containsKey("clean"))
		{
			message = clean(params);
		}
		if (params.containsKey("unqueue"))
		{
			message = unqueue(params);
		}
		out = out.replaceAll("\\{%MESSAGE%\\}", message);
		StringBuilder list_of_lists = new StringBuilder();
		boolean has_groups = false;
		List<Integer> sorted_groups = new Vector<Integer>(m_lab.getGroupIds());
		Collections.sort(sorted_groups);
		for (int id : sorted_groups)
		{
			Group g = m_lab.getGroupById(id);
			has_groups = true;
			list_of_lists.append("<div class=\"around-pulldown\">\n");
			list_of_lists.append("<h2 class=\"pulldown\" title=\"Click to show/hide the list of experiments for this group\">").append(g.getName()).append("</h2>\n");
			list_of_lists.append("<div class=\"pulldown-contents\">\n");
			list_of_lists.append("<p class=\"group-description\">").append(g.getDescription()).append("</p>\n");
			list_of_lists.append(getExperimentList(m_lab, m_assistant, g.getExperimentIds()));
			list_of_lists.append("</div></div>\n");
		}
		Set<Integer> orphan_ids = m_lab.getOrphanExperiments();
		if (!orphan_ids.isEmpty())
		{
			if (has_groups)
			{
				list_of_lists.append("<h3>Ungrouped</h3>\n");
			}
			list_of_lists.append(getExperimentList(m_lab, m_assistant, orphan_ids));
		}
		out = out.replaceAll("\\{%EXP_LIST%\\}", list_of_lists.toString());
		return out;
	}
	
	/**
	 * Produces an HTML structure displaying a list of experiments
	 * @param lab The lab that contains the experiments to display
	 * @param assistant The lab assistant associated to the lab
	 * @param ids A set of experiment IDs to display
	 * @return A well-formatted HTML list of experiments
	 */
	public static String getExperimentList(Laboratory lab, LabAssistant assistant, Set<Integer> ids)
	{
		Vector<Integer> v_ids = new Vector<Integer>();
		v_ids.addAll(ids);
		return getExperimentList(lab, assistant, v_ids);
	}

	/**
	 * Produces an HTML structure displaying a list of experiments
	 * @param lab The lab that contains the experiments to display
	 * @param assistant The lab assistant associated to the lab
	 * @param ids A list of experiment IDs to display
	 * @return A well-formatted HTML list of experiments
	 */	
	public static String getExperimentList(Laboratory lab, LabAssistant assistant, List<Integer> ids)
	{
		StringBuilder out = new StringBuilder();
		// Step 1: fetch all parameters
		Set<String> param_set = new HashSet<String>();
		for (int id : ids)
		{
			Experiment e = lab.getExperiment(id);
			param_set.addAll(e.getInputKeys(true));
		}
		Vector<String> param_list = new Vector<String>();
		param_list.addAll(param_set);
		Collections.sort(param_list);
		// Step 2: create the table
		out.append("<table class=\"exp-table tablesorter\">\n<thead><tr><td><input type=\"checkbox\" class=\"top-checkbox\" /></td><th>#</th>");
		for (String p_name : param_list)
		{
			
			out.append("<th>").append(p_name).append("</th>");
		}
		out.append("<th>Status</th></tr></thead>\n<tbody>\n");
		for (int id : ids)
		{
			Experiment e = lab.getExperiment(id);
			out.append("<tr>");
			out.append("<td class=\"exp-chk\"><input type=\"checkbox\" class=\"side-checkbox\" id=\"exp-chk-").append(id).append("\" name=\"exp-chk-").append(id).append("\"/></td>");
			out.append("<td><a href=\"experiment?id=").append(id).append("\">").append(id).append("</a></td>");
			for (String p_name : param_list)
			{
				out.append("<td>");
				JsonElement val = e.read(p_name);
				if (val == null)
				{
					out.append("");
				}
				else if (val instanceof JsonString)
				{
					out.append(htmlEscape(((JsonString) val).stringValue()));
				}
				else
				{
					out.append(htmlEscape(val.toString()));
				}
				out.append("</td>");
			}
			out.append("<td>").append(getStatusIcon(e, assistant)).append("</td>");
			out.append("</tr>\n");
		}
		out.append("</tbody>\n</table>\n");
		return out.toString();
	}

	/**
	 * Produces an icon from the status of an experiment (e.g. "Failed",
	 * "Ready", etc.)
	 * @param e The experiment
	 * @param assistant The assistant this experiment is associated with 
	 * @return HTML code for the corresponding icon
	 */
	public static String getStatusIcon(Experiment e, LabAssistant assistant)
	{
		switch (e.getStatus())
		{
		case DONE:
			return "<div class=\"status-icon status-done\" title=\"Done\"><span class=\"text-only\">D</span></div>";
		case DUNNO:
			break;
		case FAILED:
			return "<div class=\"status-icon status-failed\" title=\"Failed\"><span class=\"text-only\">F</span></div>";
		case KILLED:
			return "<div class=\"status-icon status-failed\" title=\"Timed out\"><span class=\"text-only\">K</span></div>";
		case PREREQ_F:
			return "<div class=\"status-icon status-failed\" title=\"Failed\"><span class=\"text-only\">F</span></div>";
		case PREREQ_NOK:
			if (assistant.isQueued(e.getId()))
			{
				return "<div class=\"status-icon status-queued\" title=\"Queued\"><span class=\"text-only\">Q</span></div>";
			}
			else
			{
				return "<div class=\"status-icon status-prereq\" title=\"Prerequisites not fulfilled\"><span class=\"text-only\">P</span></div>";
			}
		case PREREQ_OK:
			if (assistant.isQueued(e.getId()))
			{
				return "<div class=\"status-icon status-queued\" title=\"Queued\"><span class=\"text-only\">Q</span></div>";
			}
			else
			{
				return "<div class=\"status-icon status-ready\" title=\"Ready\"><span class=\"text-only\">r</span></div>";
			}
		case RUNNING:
			String out = "<div class=\"status-icon status-running\" title=\"Running\"><span class=\"text-only\">R</span></div>";
			out += getProgressionBar(e.getProgression());
			return out;
		default:
			return "";		
		}
		return "";
	}
	
	/**
	 * Produces a text label from the status of an experiment (e.g. "Failed",
	 * "Ready", etc.)
	 * @param e The experiment
	 * @param assistant The assistant this experiment is associated with 
	 * @return A label
	 */
	public static String getStatusLabel(Experiment e, LabAssistant assistant)
	{
		switch (e.getStatus())
		{
		case DONE:
			return "Done";
		case DUNNO:
			break;
		case FAILED:
			return "Failed";
		case KILLED:
			return "Timed out";
		case PREREQ_F:
			return "Failed when generating prerequisites";
		case PREREQ_NOK:
			if (assistant.isQueued(e.getId()))
			{
				return "Queued";
			}
			else
			{
				return "Prerequisites not fulfilled";
			}
		case PREREQ_OK:
			if (assistant.isQueued(e.getId()))
			{
				return "Queued";
			}
			else
			{
				return "Ready";
			}
		case RUNNING:
			return "Running " + getProgressionBar(e.getProgression());
		default:
			return "";		
		}
		return "";		
	}
	
	/**
	 * Performs the "queue" action on every experiment selected in the
	 * input form 
	 * @see LabAssistant#queue(Experiment)
	 * @param params The input parameters of the HTML form
	 * @return A message indicating the success of the operation
	 */
	protected String queue(Map<String,String> params)
	{
		int queued = 0;
		for (String k : params.keySet())
		{
			Matcher mat = s_pattern.matcher(k);
			if (mat.find())
			{
				int exp_id = Integer.parseInt(mat.group(1));
				Experiment e = m_lab.getExperiment(exp_id);
				if (e != null)
				{
					m_assistant.queue(e);
					queued++;
				}
			}
		}
		return "<p class=\"message info\"><span>" + queued + " experiment(s) added to the queue</span></p>";
	}
	
	/**
	 * Performs the "reset" action on every experiment selected in the
	 * input form
	 * @see Experiment#reset()
	 * @param params The input parameters of the HTML form
	 * @return A message indicating the success of the operation
	 */
	protected String reset(Map<String,String> params)
	{
		int queued = 0;
		for (String k : params.keySet())
		{
			Matcher mat = s_pattern.matcher(k);
			if (mat.find())
			{
				int exp_id = Integer.parseInt(mat.group(1));
				Experiment e = m_lab.getExperiment(exp_id);
				if (e != null)
				{
					e.reset();
					queued++;
				}
			}
		}
		return "<p class=\"message info\"><span>" + queued + " experiment(s) reset</span></p>";
	}

	/**
	 * Performs the "unqueue" action on every experiment selected in the
	 * input form
	 * @see LabAssistant#unqueue(Experiment)
	 * @param params The input parameters of the HTML form
	 * @return A message indicating the success of the operation
	 */
	protected String unqueue(Map<String,String> params)
	{
		int queued = 0;
		for (String k : params.keySet())
		{
			Matcher mat = s_pattern.matcher(k);
			if (mat.find())
			{
				int exp_id = Integer.parseInt(mat.group(1));
				Experiment e = m_lab.getExperiment(exp_id);
				if (e != null)
				{
					m_assistant.unqueue(exp_id);
					queued++;
				}
			}
		}
		return "<p class=\"message info\"><span>Removed " + queued + " experiment(s) from the queue</span></p>";
	}

	/**
	 * Performs the "clean" action on every experiment selected in the
	 * input form
	 * @see Experiment#clean()
	 * @param params The input parameters of the HTML form
	 * @return A message indicating the success of the operation
	 */
	protected String clean(Map<String,String> params)
	{
		int queued = 0;
		for (String k : params.keySet())
		{
			Matcher mat = s_pattern.matcher(k);
			if (mat.find())
			{
				int exp_id = Integer.parseInt(mat.group(1));
				Experiment e = m_lab.getExperiment(exp_id);
				if (e != null)
				{
					e.clean();
					queued++;
				}
			}
		}
		return "<p class=\"message info\"><span>Cleaned " + queued + " experiment(s)</span></p>";
	}
	
	public static String getProgressionBar(float f)
	{
		float width = 50;
		StringBuilder out = new StringBuilder();
		out.append("<ul id=\"progress-bar\" style=\"width:").append(width).append("px\">");
		out.append("<li class=\"done\" style=\"width:").append((int)(f * width)).append("px\"><span class=\"text-only\">").append(f*100).append("% completed</span></li>");
		out.append("</ul>");
		return out.toString();
	}
}
