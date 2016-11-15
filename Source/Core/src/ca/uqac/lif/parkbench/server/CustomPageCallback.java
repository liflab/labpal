package ca.uqac.lif.parkbench.server;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class CustomPageCallback extends TemplatePageCallback
{
	public CustomPageCallback(String prefix, Laboratory lab, LabAssistant assistant) 
	{
		super(prefix, lab, assistant);
		m_filename = s_path + "/custom.html";
	}
}
