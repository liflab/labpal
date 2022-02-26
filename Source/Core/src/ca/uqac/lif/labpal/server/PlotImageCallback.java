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

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.spreadsheet.chart.gnuplot.Gnuplot;

import com.sun.net.httpserver.HttpExchange;

/**
 * Callback producing an image from one of the lab's plots, in various formats.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>dl=1</tt>: to download the image instead of displaying it. This will
 * prompt the user to save the file in its browser</li>
 * <li><tt>id=x</tt>: mandatory; the ID of the plot to display</li>
 * <li><tt>format=x</tt>: the requested image format. Currenly supports pdf,
 * dumb (text), png and gp (raw data file for Gnuplot).
 * </ul>
 * 
 * @author Sylvain Hallé
 *
 */
public class PlotImageCallback extends RestCallback
{
	/**
	 * The pattern to extract the experiment ID from the URL.
	 */
	protected static final Pattern s_idPattern = Pattern.compile("plot/(\\d+)");

	protected Laboratory m_lab;

	public PlotImageCallback(Laboratory lab)
	{
		super(Method.GET, "/plot/");
		m_lab = lab;
	}
	
	@Override
	public boolean fire(HttpExchange t)
	{
		URI u = t.getRequestURI();
		String path = u.getPath();
		String method = t.getRequestMethod();
		return ((m_ignoreMethod || method.compareToIgnoreCase(methodToString(m_method)) == 0)) 
				&& path.startsWith(m_path) && (path.contains("/png") || path.contains("/pdf") || path.contains("/gp") || path.contains("/dumb"));
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		Map<String, String> params = getParameters(t);
		int plot_id = fetchId(t);
		Plot p = m_lab.getPlot(plot_id);
		String uri = t.getRequestURI().toString();
		if (p == null)
		{
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		if (uri.contains("/gp") || (params.containsKey("format") && params.get("format").compareToIgnoreCase("gp") == 0))
		{
			String plot_contents = p.toGnuplot(ChartFormat.PDF, m_lab.getName(), true);
			if (plot_contents != null)
			{
				response.setContents(plot_contents);
				response.setCode(CallbackResponse.HTTP_OK);
				response.setAttachment(Server.urlEncode(p.getTitle() + ".gp"));
				return response;
			}
		}
		if (!Gnuplot.isGnuplotPresent())
		{
			// Asking for an image, but Gnuplot not available: stop right here
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		ChartFormat term = ChartFormat.PNG;
		response.setContentType(ContentType.PNG);
		if (uri.contains("/pdf") || (params.containsKey("format") && params.get("format").compareToIgnoreCase("pdf") == 0))
		{
			term = ChartFormat.PDF;
			response.setContentType(ContentType.PDF);
		}
		if (uri.contains("/dumb") || (params.containsKey("format") && params.get("format").compareToIgnoreCase("dumb") == 0))
		{
			term = Gnuplot.DUMB;
			response.setContentType(ContentType.TEXT);
		}
		byte[] image = p.getImage(term);
		if (image == null)
		{
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		response.setContents(image);
		response.setCode(CallbackResponse.HTTP_OK);
		if (uri.contains("dl") || params.containsKey("dl"))
		{
			response.setAttachment(Server.urlEncode(p.getTitle() + "." + term.getExtension()));
		}
		return response;
	}

	protected static int fetchId(HttpExchange h)
	{
		String uri = h.getRequestURI().toString();
		Matcher mat = s_idPattern.matcher(uri);
		if (!mat.find())
		{
			return -1; // No ID
		}
		return Integer.parseInt(mat.group(1));
	}

}
