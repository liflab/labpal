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

import java.util.Map;
import java.util.regex.Pattern;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.labpal.experiment.DependencyExperimentSelector;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.spreadsheet.chart.gnuplot.Gnuplot;

public class PlotPageCallback extends TemplatePageCallback
{
	/**
	 * The pattern to extract the experiment ID from the URL.
	 */
	protected static final Pattern s_idPattern = Pattern.compile("plot/(\\d+)");
	
	public PlotPageCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-plots");
	}

	@Override
	public void fillInputModel(String uri, Map<String,String> req_parameters, Map<String,Object> input, Map<String,byte[]> parts) throws PageRenderingException
	{
		super.fillInputModel(uri, req_parameters, input, parts);
		int id = fetchId(s_idPattern, uri);
		Plot p = m_server.getLaboratory().getPlot(id);
		if (p == null)
		{
			throw new PageRenderingException(CallbackResponse.HTTP_NOT_FOUND, "Not found", "No such plot");
		}
		input.put("id", id);
		input.put("title", "Plot " + id);
		input.put("plot", p);
		input.put("expdeps", DependencyExperimentSelector.getDependencyList(p));
		if (req_parameters.containsKey("textbrowser") && Boolean.TRUE.equals(input.get("textbrowser")))
		{
			byte[] bytes = p.getImage(Gnuplot.DUMB);
			input.put("plottext", new String(bytes));
		}
	}
}
