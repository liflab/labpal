package ca.uqac.lif.parkbench.server;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;


public class QueueCallback extends TemplatePageCallback
{
	protected static final transient Pattern s_pattern = Pattern.compile("exp-chk-(\\d+)");

	public QueueCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/queue", lab, assistant);
		setMethod(Method.POST);
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
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
		String out = page.replaceAll("\\{%TITLE%\\}", "Add to queue");
		out = out.replaceAll("\\{%ADDED_TEXT%\\}", queued + " experiments added to " + m_assistant.getName() + "'s queue");
		return out;
	}

}
