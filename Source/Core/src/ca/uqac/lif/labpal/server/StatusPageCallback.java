/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

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
package ca.uqac.lif.labpal.server;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import ca.uqac.lif.labpal.EnvironmentMessage;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

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
	 * If the lab's environment requirements are not met, the error messages
	 * are stored here. Since environment checks can be long, this check is
	 * done only once, and the result is cached for future calls to this
	 * class. 
	 */
	protected final transient List<EnvironmentMessage> m_environmentMessage;

	/**
	 * The description associated to the lab
	 */
	protected final transient String m_labDescription;
	
	/**
	 * Whether to skip the check of the environment parameters at startup
	 */
	protected boolean m_skipEnvironmentCheck = false;

	public StatusPageCallback(Laboratory lab, LabAssistant assistant, boolean skip_environment_check)
	{
		super("/status", lab, assistant);
		m_skipEnvironmentCheck = skip_environment_check;
		m_environmentMessage = new LinkedList<EnvironmentMessage>();
		if (!m_skipEnvironmentCheck)
		{
			lab.isEnvironmentOk(m_environmentMessage);
		}
		else
		{
			m_environmentMessage.add(new EnvironmentMessage("The check of environment parameters has been manually bypassed by a command-line parameter.", EnvironmentMessage.Severity.WARNING));
		}
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
		out = out.replaceAll("\\{%HOSTNAME%\\}", m_lab.getHostName());
		out = out.replaceAll("\\{%SPEED_FACTOR%\\}", String.format("%.2f", Laboratory.s_parkMips));
		out = out.replaceAll("\\{%DATA_POINTS%\\}", Integer.toString(m_lab.countDataPoints()));
		out = out.replaceAll("\\{%SEL_HOME%\\}", "selected");
		out = out.replaceAll("\\{%OS_NAME%\\}", System.getProperty("os.name"));
		out = out.replaceAll("\\{%OS_ARCH%\\}", System.getProperty("os.arch"));
		out = out.replaceAll("\\{%OS_VERSION%\\}", System.getProperty("os.version"));
		String doi = m_lab.getDoi();
		if (!doi.isEmpty())
		{
			out = out.replaceAll("\\{%DOI%\\}", "<tr><th title=\"The Digital Object Identifier assigned to this lab\">DOI</th><td>" + Matcher.quoteReplacement(htmlEscape(doi)) + "</td></tr>\n");
		}
		out = out.replaceAll("\\{%PROGRESS_BAR%\\}", getBar());
		if (!m_environmentMessage.isEmpty())
		{
			StringBuilder messages = new StringBuilder();
			for (EnvironmentMessage msg : m_environmentMessage)
			{
				String css_class = "error";
				if (msg.getSeverity() == EnvironmentMessage.Severity.WARNING)
				{
					css_class = "warning";
				}
				messages.append("<p class=\"message ").append(css_class).append("\"><span>");
				messages.append(msg.getMessage());
				messages.append("</span></p>\n");
			}
			out = out.replaceAll("\\{%ENVIRONMENT_MESSAGE%\\}", messages.toString());
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
		final float bar_width_px = 400;
		int num_ex = 0, num_q = 0, num_failed = 0, num_done = 0, num_warn = 0;
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
			case DONE_WARNING:
				num_warn++;
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
		float scale = bar_width_px / num_ex;
		int num_remaining = num_ex - num_done - num_q - num_failed;
		out.append("<ul id=\"progress-bar\" style=\"float:left;margin-bottom:20px;width:").append(((float) num_ex) * scale).append("px;\">");
		out.append("<li class=\"done\" title=\"Done: ").append(num_done).append("\" style=\"width:").append(((float) num_done) * scale).append("px\"><span class=\"text-only\">Done: ").append(num_done).append("</span></li>");
		out.append("<li class=\"queued\" title=\"Queued: ").append(num_q).append("\" style=\"width:").append(((float) num_q) * scale).append("px\"><span class=\"text-only\">Queued: ").append(num_q).append("</span></li>");
		out.append("<li class=\"warning\" title=\"Warning: ").append(num_warn).append("\" style=\"width:").append(((float) num_warn) * scale).append("px\"><span class=\"text-only\">Warnings: ").append(num_warn).append("</span></li>");
		out.append("<li class=\"failed\" title=\"Failed/cancelled: ").append(num_failed).append("\" style=\"width:").append(((float) num_failed) * scale).append("px\"><span class=\"text-only\">Failed/cancelled: ").append(num_failed).append("</span></li>");
		out.append("<li class=\"other\" title=\"Other: ").append(num_remaining).append("\" style=\"width:").append(((float) num_remaining) * scale).append("px\"><span class=\"text-only\">Other: ").append(num_remaining).append("</span></li>");
		out.append("</ul>");
		out.append("<div>").append(num_done).append("/").append(num_ex).append("</div>");
		out.append("<div style=\"clear:both\"></div>");
		return out.toString();
	}
}
