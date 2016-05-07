package ca.uqac.lif.parkbench.server;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;


public class UnqueueCallback extends TemplatePageCallback
{
	protected static final transient Pattern s_pattern = Pattern.compile("exp-chk-(\\d+)");

	public UnqueueCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/unqueue", lab, assistant);
		
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		int unqueued = 0;
		for (String k : params.keySet())
		{
			Matcher mat = s_pattern.matcher(k);
			if (mat.find())
			{
				int exp_id = Integer.parseInt(mat.group(1));
				m_assistant.unqueue(exp_id);
				unqueued++;
			}
		}
		String out = page.replaceAll("\\{%TITLE%\\}", "Add to queue");
		out = out.replaceAll("\\{%ADDED_TEXT%\\}", unqueued + " experiments removed from " + m_assistant.getName() + "'s queue");
		return out;
	}

}
