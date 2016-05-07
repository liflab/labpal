package ca.uqac.lif.parkbench.server;

import java.util.Collections;
import java.util.Map;
import java.util.Vector;
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
		out = out.replaceAll("\\{%PLOTS%\\}", getPlots());
		out = out.replaceAll("\\{%SEL_PLOTS%\\}", "selected");
		return out;
	}
	
	public String getPlots()
	{
		StringBuilder out = new StringBuilder();
		Vector<Integer> ids = new Vector<Integer>();
		ids.addAll(m_lab.getPlotIds());
		Collections.sort(ids);
		for (int id : ids)
		{
			out.append("<div class=\"plot\">\n");
			out.append("<a href=\"plot?id=").append(id).append("&amp;format=png\" target=\"_blank\" title=\"Click on plot to view in new window\">");
			out.append("<img src=\"plot?id=").append(id).append("&amp;format=png\" alt=\"Plot\" /></a>\n");
			out.append("<div><ul><li><a class=\"btn-24 btn-gp\" href=\"plot?id=").append(id).append("&amp;format=gp&amp;dl=1\" title=\"Download as GnuPlot source file\">GP</a></li>");
			out.append("<li><a class=\"btn-24 btn-gp\" href=\"plot?id=").append(id).append("&amp;format=dumb\" title=\"View as ASCII art\" target=\"_blank\">ASCII</a></li>");
			out.append("<li><a class=\"btn-24 btn-pdf\" href=\"plot?id=").append(id).append("&amp;format=pdf&amp;dl=1\" title=\"Download as PDF\">PDF</a></li></ul></div>");
			out.append("</div>\n");			
		}
		out.append("<div style=\"clear:both\"></div>\n");
		return out.toString();
	}

}
