/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.parkbench.server;

import java.util.Map;

import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

/**
 * Callback for the home page, showing various statistics and basic
 * data about the current lab.
 * 
 * @author Sylvain Hallé
 *
 */
public class StatusPageCallback extends TemplatePageCallback
{
	/**
	 * If the lab's environment requirements are not met, the error message
	 * is stored here. Since environment checks can be long, this check is
	 * done only once, and the result is cached for future calls to this
	 * class. 
	 */
	protected final transient String m_environmentMessage;
	
	/**
	 * The description associated to the lab
	 */
	protected final transient String m_labDescription;
	
	public StatusPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/status", lab, assistant);
		m_environmentMessage = lab.isEnvironmentOk();
		m_labDescription = lab.getDescription();
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", m_lab.getTitle());
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.STATUS));
		out = out.replaceAll("\\{%LAB_DESCRIPTION%\\}", m_labDescription);
		out = out.replaceAll("\\{%LAB_ASSISTANT%\\}", m_assistant.getName());
		out = out.replaceAll("\\{%LAB_AUTHOR%\\}", m_lab.getAuthorName());
		out = out.replaceAll("\\{%LAB_SEED%\\}", Integer.toString(m_lab.getRandomSeed()));
		out = out.replaceAll("\\{%SPEED_FACTOR%\\}", String.format("%.2f", Laboratory.s_parkMips));
		out = out.replaceAll("\\{%SEL_HOME%\\}", "selected");
		out = out.replaceAll("\\{%OS_NAME%\\}", System.getProperty("os.name"));
		out = out.replaceAll("\\{%OS_ARCH%\\}", System.getProperty("os.arch"));
		out = out.replaceAll("\\{%OS_VERSION%\\}", System.getProperty("os.version"));
		out = out.replaceAll("\\{%PROGRESS_BAR%\\}", getBar());
		if (m_environmentMessage != null)
		{
			out = out.replaceAll("\\{%ENVIRONMENT_MESSAGE%\\}", "<p class=\"message error\">" 
					+ "<span>The lab's environment requirements are not met. " 
					+ m_environmentMessage 
					+ " This means you may not be able to run the experiments propertly.</span></p>");
		}
		return out;
	}
	
	/**
	 * Produces a status bar indicating the relative completion of the
	 * experiments in this lab.
	 * @return HTML code for the status bar
	 */
	public String getBar()
	{
		// Width of the bar, in pixels
		final int bar_width_px = 400;
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
		int scale = bar_width_px / num_ex;
		int num_remaining = num_ex - num_done - num_q - num_failed;
		out.append("<ul id=\"progress-bar\" style=\"float:left;margin-bottom:20px;width:").append(num_ex * scale).append("px;\">");
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
