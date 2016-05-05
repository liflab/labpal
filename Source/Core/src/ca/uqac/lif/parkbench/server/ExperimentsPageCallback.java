package ca.uqac.lif.parkbench.server;

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
	public String render(String page)
	{
		String out = page.replaceAll("\\{%LAB_NAME%\\}", m_lab.getTitle());
		out = out.replaceAll("\\{%EXP_LIST%\\}", getExperimentList());
		return out;
	}
	
	public String getExperimentList()
	{
		StringBuilder out = new StringBuilder();
		out.append("<ul id=\"exp-list\">\n");
		for (int id : m_lab.getExperimentIds())
		{
			Experiment e = m_lab.getExperiment(id);
			out.append("<li id=\"exp-item-").append(id).append("\">");
			out.append("<a href=\"experiment?id=").append(id).append("\">").append(id).append("</a>");
			out.append(e.toString());
			out.append("<input type=\"checkbox\" id=\"exp-chk-").append(id).append("\"/>");
			out.append("</li>\n");
		}
		out.append("</ul>\n");
		return out.toString();
	}
}
