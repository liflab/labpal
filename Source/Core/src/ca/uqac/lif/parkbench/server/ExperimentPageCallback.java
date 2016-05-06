package ca.uqac.lif.parkbench.server;

import java.util.Map;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class ExperimentPageCallback extends TemplatePageCallback
{
	public ExperimentPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/experiment", lab, assistant);
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		if (!params.containsKey("id"))
			return "";
		int experiment_nb = Integer.parseInt(params.get("id"));
		Experiment e = m_lab.getExperiment(experiment_nb);
		String out = page.replaceAll("\\{%TITLE%\\}", "Experiment #" + experiment_nb);
		out = out.replaceAll("\\{%EXP_NB%\\}", Integer.toString(experiment_nb));
		out = out.replaceAll("\\{%EXP_DATA%\\}", renderHtml(e.getAllParameters()).toString());
		return out;
	}
	
	public static StringBuilder renderHtml(JsonElement e)
	{
		StringBuilder out = new StringBuilder();
		if (e instanceof JsonString)
		{
			out.append(((JsonString) e).stringValue());
		}
		else if (e instanceof JsonNumber)
		{
			out.append(((JsonNumber) e).numberValue());
		}
		else if (e instanceof JsonMap)
		{
			JsonMap m = (JsonMap) e;
			out.append("<table class=\"json-table\">\n");
			for (String k : m.keySet())
			{
				out.append("<tr><th>").append(k).append("</th>");
				out.append("<td>");
				JsonElement v = m.get(k);
				out.append(renderHtml(v));
				out.append("</td></tr>\n");
			}
			out.append("</table>\n");
		}
		return out;
	}
	
	public String getExperimentList()
	{
		return "";
	}
}
