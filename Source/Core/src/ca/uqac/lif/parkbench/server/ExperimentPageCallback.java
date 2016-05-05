package ca.uqac.lif.parkbench.server;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class ExperimentPageCallback extends TemplatePageCallback
{
	public ExperimentPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/experiment", lab, assistant);
	}
	
	@Override
	public String render(String page)
	{
		String out = page.replaceAll("\\{%LAB_NAME%\\}", m_lab.getTitle());
		out = out.replaceAll("\\{%EXP_NB%\\}", "X");
		return out;
	}
	
	public String getExperimentList()
	{
		return "";
	}
}
