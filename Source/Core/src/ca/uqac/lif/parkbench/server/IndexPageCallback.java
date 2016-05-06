package ca.uqac.lif.parkbench.server;

import java.util.Map;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class IndexPageCallback extends TemplatePageCallback
{
	public IndexPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/index", lab, assistant);
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", m_lab.getTitle());
		out = out.replaceAll("\\{%SEL_HOME%\\}", "selected");
		return out;
	}
}
