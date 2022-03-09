/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import ca.uqac.lif.labpal.experiment.Experiment;

public class StatusPageCallback extends TemplatePageCallback
{
	public StatusPageCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-status");
	}

	@Override
	public void fillInputModel(String uri, Map<String,String> req_parameters, Map<String,Object> input, Map<String,byte[]> parts) throws PageRenderingException
	{
		super.fillInputModel(uri, req_parameters, input, parts);
		input.put("osname", System.getProperty("os.name"));
		input.put("osversion", System.getProperty("os.version"));
		input.put("osarch", System.getProperty("os.arch"));
		input.put("bar", getBar());
	}

	/**
	 * Produces a status bar indicating the relative completion of the experiments
	 * in this lab.
	 * 
	 * @return HTML code for the status bar
	 */
	public String getBar()
	{
		// Width of the bar, in pixels
		final float bar_width_px = 400;
		int num_ex = 0, num_q = 0, num_failed = 0, num_done = 0, num_warn = 0;
		StringBuilder out = new StringBuilder();
		List<Experiment> running = new ArrayList<Experiment>();
		for (Experiment ex : m_server.getLaboratory().getExperiments())
		{
			num_ex++;
			switch (ex.getStatus())
			{
			case RUNNING:
				running.add(ex);
				break;
			case DONE:
				num_done++;
				break;
			case FAILED:
				num_failed++;
				break;
			default:
				if (m_server.getLaboratory().isQueued(ex))
				{
					num_q++;
				}
				break;
			}
		}
		out.append("<h3>Currently running</h3>\n");
		out.append("<ul class=\"running-exps\">\n");
		if (running.isEmpty())
		{
			out.append("<li class=\"none\">None</li>\n");
		}
		else
		{
			Collections.sort(running);
			for (Experiment e : running)
			{
				out.append("<li><a href=\"/experiment/").append(e.getId()).append("\">").append(e.getId()).append("</a></li>\n");
			}
		}
		out.append("</ul>");

		// StringBuilder out = new StringBuilder();
		float scale = bar_width_px / num_ex;
		int num_remaining = num_ex - num_done - num_q - num_failed;

		out.append("<ul class=\"progress-bar\" style=\"float:left;margin-bottom:20px;width:")
		.append(((float) num_ex) * scale).append("px;\">");
		out.append("<li class=\"done\" title=\"Done: ").append(num_done).append("\" style=\"width:")
		.append(((float) num_done) * scale).append("px\"><span class=\"text-only\">Done: ")
		.append(num_done).append("</span></li>");
		out.append("<li class=\"queued\" title=\"Queued: ").append(num_q).append("\" style=\"width:")
		.append(((float) num_q) * scale).append("px\"><span class=\"text-only\">Queued: ")
		.append(num_q).append("</span></li>");
		out.append("<li class=\"warning\" title=\"Warning: ").append(num_warn)
		.append("\" style=\"width:").append(((float) num_warn) * scale)
		.append("px\"><span class=\"text-only\">Warnings: ").append(num_warn)
		.append("</span></li>");
		out.append("<li class=\"failed\" title=\"Failed/cancelled: ").append(num_failed)
		.append("\" style=\"width:").append(((float) num_failed) * scale)
		.append("px\"><span class=\"text-only\">Failed/cancelled: ").append(num_failed)
		.append("</span></li>");
		out.append("<li class=\"other\" title=\"Other: ").append(num_remaining)
		.append("\" style=\"width:").append(((float) num_remaining) * scale)
		.append("px\"><span class=\"text-only\">Other: ").append(num_remaining)
		.append("</span></li>");
		out.append("</ul>");
		out.append("<div id=\"numdone\">").append(num_done).append("/").append(num_ex).append("</div>");
		out.append("<div style=\"clear:both\"></div>");
		return out.toString();
	}

}
