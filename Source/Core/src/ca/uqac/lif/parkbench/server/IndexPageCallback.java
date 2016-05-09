package ca.uqac.lif.parkbench.server;

import java.util.Map;

import ca.uqac.lif.parkbench.Experiment;
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
		out = out.replaceAll("\\{%PROGRESS_BAR%\\}", getBar());
		return out;
	}
	
	public String getBar()
	{
		int num_ex = 0, num_q = 0, num_failed = 0, num_done = 0;
		for (int id : m_lab.getExperimentIds())
		{
			num_ex++;
			Experiment ex = m_lab.getExperiment(id);
			switch (ex.getStatus())
			{
			case DONE:
				num_done++;
				break;
			case FAILED:
				num_failed++;
				break;
			default:
				if (m_assistant.isQueued(id))
				{
					num_q++;
				}
				break;
			}
		}
		StringBuilder out = new StringBuilder();
		int scale = 400 / num_ex;
		int num_remaining = num_ex - num_done - num_q - num_failed;
		out.append("<ul id=\"progress-bar\" style=\"width:").append(num_ex * scale).append("px;\">");
		out.append("<li class=\"done\" title=\"Done: ").append(num_done).append("\" style=\"width:").append(num_done * scale).append("px\"><span class=\"text-only\">Done: ").append(num_done).append("</span></li>");
		out.append("<li class=\"queued\" title=\"Queued: ").append(num_q).append("\" style=\"width:").append(num_q * scale).append("px\"><span class=\"text-only\">Queued: ").append(num_q).append("</span></li>");
		out.append("<li class=\"failed\" title=\"Failed/cancelled: ").append(num_failed).append("\" style=\"width:").append(num_failed * scale).append("px\"><span class=\"text-only\">Failed/cancelled: ").append(num_failed).append("</span></li>");
		out.append("<li class=\"other\" title=\"Other: ").append(num_remaining).append("\" style=\"width:").append(num_remaining * scale).append("px\"><span class=\"text-only\">Other: ").append(num_remaining).append("</span></li>");
		out.append("</ul>");
		out.append("<div>").append(num_done).append("/").append(num_ex).append("</div>");
		out.append("<div style=\"clear:both\"></div>");
		return out.toString();
	}
}
