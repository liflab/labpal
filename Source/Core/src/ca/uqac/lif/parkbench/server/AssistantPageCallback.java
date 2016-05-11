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

import java.util.List;
import java.util.Map;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;
import ca.uqac.lif.parkbench.ParkbenchTui;

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
		setMethod(Method.POST);
		ignoreMethod();
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
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
			out = out.replaceAll("\\{%EXP_LIST%\\}", ExperimentsPageCallback.getExperimentList(m_lab, m_assistant, queue));
		}
		out = out.replaceAll("\\{%ASSISTANT_NAME%\\}", m_assistant.getName());
		out = out.replaceAll("\\{%SEL_ASSISTANT%\\}", "selected");
		out = out.replaceAll("\\{%TIME_ESTIMATE%\\}", ParkbenchTui.formatEta(m_assistant.getTimeEstimate()));
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
		out = out.replaceAll("\\{%MESSAGE%\\}", message);
		return out;
	}

}
