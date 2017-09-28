/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.LabPalTui;

/**
 * Callback to display, start and stop the lab assistant.
 * 
 * @author Sylvain Hallé
 *
 */
public class AssistantPageCallback extends ExperimentsPageCallback
{

	public AssistantPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/assistant", lab, assistant);
		ignoreMethod();
	}
	
	@Override
	public String fill(String page, Map<String,String> params, boolean is_offline)
	{
		String message = "";
		if (params.containsKey("start"))
		{
			if (!m_assistant.isRunning())
			{
				m_lab.start();
				message = "<p class=\"message info\"><span>Assistant started</span></p>";
			}
			else
			{
				message = "<p class=\"message info\"><span>Assistant already started</span></p>";
			}
		}
		else if (params.containsKey("stop"))
		{
			m_assistant.stop();
			message = "<p class=\"message info\"><span>Assistant stopped</span></p>";
		}
		String out = page.replaceAll("\\{%TITLE%\\}", "Lab assistant");
		if (params.containsKey("unqueue"))
		{
			message = unqueue(params);
		}
		else if (params.containsKey("reset"))
		{
			message = reset(params);
		}
		else if (params.containsKey("clean"))
		{
			message = clean(params);
		}
		List<Integer> queue = m_assistant.getCurrentQueue();
		if (queue.isEmpty())
		{
			out = out.replaceAll("\\{%EXP_LIST%\\}", "<p>This lab assistant has no experiment left to do.</p>\n");
		}
		else
		{
			out = out.replaceAll("\\{%EXP_LIST%\\}", Matcher.quoteReplacement(ExperimentsPageCallback.getExperimentList(m_lab, m_assistant, queue)));
		}
		out = out.replaceAll("\\{%ASSISTANT_TIME%\\}", LabPalTui.formatEta(m_assistant.getRunningTime() / 1000));
		out = out.replaceAll("\\{%ASSISTANT_NAME%\\}", Matcher.quoteReplacement(htmlEscape(m_assistant.getName())));
		out = out.replaceAll("\\{%SEL_ASSISTANT%\\}", "selected");
		out = out.replaceAll("\\{%TIME_ESTIMATE%\\}", LabPalTui.formatEta(m_assistant.getTimeEstimate()));
		if (m_assistant.isRunning())
		{
			out = out.replaceAll("\\{%BTN_STOP%\\}", "<input type=\"submit\" class=\"btn\" id=\"btn-stop\" name=\"stop\" value=\"Stop the assistant\"/>");
		}
		else
		{
			if (!m_assistant.getCurrentQueue().isEmpty())
			{
				out = out.replaceAll("\\{%BTN_START%\\}", "<input type=\"submit\" class=\"btn\" id=\"btn-start\" name=\"start\" value=\"Start the assistant\"/>");
			}
		}
		out = out.replaceAll("\\{%MESSAGE%\\}", Matcher.quoteReplacement(message));
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.ASSISTANT));
		out = out.replaceAll("\\{%HEADER_PROGRESS_BAR%\\}", getHeaderBar());
		return out;
	}
	
	@Override
	public void addToZipBundle(ZipOutputStream zos) throws IOException
	{
		// Do nothing; this method must stay here to override
		// ExperimentsPageCallback. Otherwise, will create a duplicate
		// "experiments.html" page.
	}

}
