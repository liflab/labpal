package ca.uqac.lif.parkbench.server;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class IndexPageCallback extends TemplatePageCallback
{
	public IndexPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/index", lab, assistant);
	}
	
	@Override
	public String render(String page)
	{
		String out = page.replaceAll("\\{%LAB_NAME%\\}", m_lab.getTitle());
		return out;
	}
}
