package ca.uqac.lif.parkbench.server;

import java.util.Map;
import java.util.regex.Pattern;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;


public class PlotsPageCallback extends TemplatePageCallback
{
	protected static final transient Pattern s_pattern = Pattern.compile("exp-chk-(\\d+)");

	public PlotsPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/plots", lab, assistant);
		
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Plots");
		out = out.replaceAll("\\{%SEL_PLOTS%\\}", "selected");
		return out;
	}

}
