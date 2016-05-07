package ca.uqac.lif.parkbench.server;

import java.util.List;
import java.util.Map;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;
import ca.uqac.lif.parkbench.ParkbenchTui;

public class AssistantPageCallback extends TemplatePageCallback
{

	public AssistantPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/assistant", lab, assistant);
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Lab assistant");
		List<Integer> queue = m_assistant.getCurrentQueue();
		if (queue.isEmpty())
		{
			out = out.replaceAll("\\{%EXP_LIST%\\}", "<p>This lab assistant has no experiment left to do.</p>\n");
		}
		else
		{
			out = out.replaceAll("\\{%EXP_LIST%\\}", ExperimentsPageCallback.getExperimentList(m_lab, m_assistant, queue));
		}
		out = out.replaceAll("\\{%ASSISTANT_NAME%\\}", m_assistant.getName());
		out = out.replaceAll("\\{%SEL_ASSISTANT%\\}", "selected");
		out = out.replaceAll("\\{%TIME_ESTIMATE%\\}", ParkbenchTui.formatEta(m_assistant.getTimeEstimate()));
		return out;
	}

}
