package ca.uqac.lif.parkbench.server;

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
import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class ExperimentsPageCallback extends TemplatePageCallback
{
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
		String message = "";
		if (params.containsKey("queue"))
		{
			message = queue(params);
		}
		if (params.containsKey("reset"))
		{
			message = reset(params);
		}
		out = out.replaceAll("\\{%MESSAGE%\\}", message);
		out = out.replaceAll("\\{%EXP_LIST%\\}", getExperimentList(m_lab, m_assistant, m_lab.getExperimentIds()));
		return out;
	}
	
	public static String getExperimentList(Laboratory lab, LabAssistant assistant, Set<Integer> ids)
	{
		Vector<Integer> v_ids = new Vector<Integer>();
		v_ids.addAll(ids);
		return getExperimentList(lab, assistant, v_ids);
	}
	
	public static String getExperimentList(Laboratory lab, LabAssistant assistant, List<Integer> ids)
	{
		StringBuilder out = new StringBuilder();
		// Step 1: fetch all parameters
		Set<String> param_set = new HashSet<String>();
		for (int id : ids)
		{
			Experiment e = lab.getExperiment(id);
			param_set.addAll(e.getInputKeys());
		}
		Vector<String> param_list = new Vector<String>();
		param_list.addAll(param_set);
		Collections.sort(param_list);
		// Step 2: create the table
		out.append("<table class=\"exp-table tablesorter\">\n<thead><tr><th><input type=\"checkbox\" id=\"top-checkbox\" onclick=\"select_all();\"/></th><th>#</th>");
		for (String p_name : param_list)
		{
			out.append("<th>").append(p_name).append("</th>");
		}
		out.append("<th>Status</th></tr></thead>\n<tbody>\n");
		for (int id : ids)
		{
			Experiment e = lab.getExperiment(id);
			out.append("<tr>");
			out.append("<td class=\"exp-chk\"><input type=\"checkbox\" id=\"exp-chk-").append(id).append("\" name=\"exp-chk-").append(id).append("\"/></td>");
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
					out.append(((JsonString) val).stringValue());
				}
				else
				{
					out.append(val.toString());
				}
				out.append("</td>");
			}
			out.append("<td>").append(getStatusIcon(assistant, e)).append("</td>");
			out.append("</tr>\n");
		}
		out.append("</tbody>\n</table>\n");
		return out.toString();
	}
	
	public static String getStatusIcon(LabAssistant assistant, Experiment e)
	{
		switch (e.getStatus())
		{
		case DONE:
			return "<div class=\"status-icon status-done\" title=\"Done\"><span class=\"text-only\">D</span></div>";
		case DUNNO:
			break;
		case FAILED:
			return "<div class=\"status-icon status-failed\" title=\"Failed\"><span class=\"text-only\">F</span></div>";
		case PREREQ_F:
			return "<div class=\"status-icon status-failed\" title=\"Failed\"><span class=\"text-only\">F</span></div>";
		case PREREQ_NOK:
			if (assistant.isQueued(e.getId()))
			{
				return "<div class=\"status-icon status-queued\" title=\"Queued\"><span class=\"text-only\">Q</span></div>";
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
			return "<div class=\"status-icon status-running\" title=\"Running\"><span class=\"text-only\">R</span></div>";
		default:
			return "";		
		}
		return "";
	}
	
	public static String getStatusLabel(LabAssistant assistant, Experiment e)
	{
		switch (e.getStatus())
		{
		case DONE:
			return "Done";
		case DUNNO:
			break;
		case FAILED:
			return "Failed";
		case PREREQ_F:
			return "Failed when generating prerequisites";
		case PREREQ_NOK:
			if (assistant.isQueued(e.getId()))
			{
				return "Queued";
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
			return "Running";
		default:
			return "";		
		}
		return "";		
	}
	
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
		return "<p class=\"message info\"><span>Cleaned" + queued + " experiment(s)</span></p>";
	}
}
