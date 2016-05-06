package ca.uqac.lif.parkbench.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class ExperimentsPageCallback extends TemplatePageCallback
{
	public ExperimentsPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/experiments", lab, assistant);
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Experiments");
		out = out.replaceAll("\\{%EXP_LIST%\\}", getExperimentList());
		out = out.replaceAll("\\{%SEL_EXPERIMENTS%\\}", "selected");
		return out;
	}
	
	public String getExperimentList()
	{
		StringBuilder out = new StringBuilder();
		Set<Integer> ids = m_lab.getExperimentIds();
		// Step 1: fetch all parameters
		Set<String> param_set = new HashSet<String>();
		for (int id : ids)
		{
			Experiment e = m_lab.getExperiment(id);
			param_set.addAll(e.getInputKeys());
		}
		Vector<String> param_list = new Vector<String>();
		param_list.addAll(param_set);
		Collections.sort(param_list);
		// Step 2: create the table
		out.append("<table class=\"exp-table\">\n<tr><th></th><th>#</th>");
		for (String p_name : param_list)
		{
			out.append("<th>").append(p_name).append("</th>");
		}
		out.append("</tr>\n");
		for (int id : m_lab.getExperimentIds())
		{
			Experiment e = m_lab.getExperiment(id);
			out.append("<tr>");
			out.append("<td class=\"exp-chk\"><input type=\"checkbox\" id=\"exp-chk-").append(id).append("\"/></td>");
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
			out.append("</tr>\n");
		}
		out.append("</table>\n");
		return out.toString();
	}
}
