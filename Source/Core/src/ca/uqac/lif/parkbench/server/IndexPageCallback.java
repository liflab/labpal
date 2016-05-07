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
		out = out.replaceAll("\\{%LAB_ASSISTANT%\\}", m_assistant.getName());
		out = out.replaceAll("\\{%LAB_AUTHOR%\\}", m_lab.getAuthorName());
		out = out.replaceAll("\\{%SPEED_FACTOR%\\}", String.format("%.2f", Laboratory.s_parkMips));
		out = out.replaceAll("\\{%SEL_HOME%\\}", "selected");
		out = out.replaceAll("\\{%OS_NAME%\\}", System.getProperty("os.name"));
		out = out.replaceAll("\\{%OS_ARCH%\\}", System.getProperty("os.arch"));
		out = out.replaceAll("\\{%OS_VERSION%\\}", System.getProperty("os.version"));
		return out;
	}
}
