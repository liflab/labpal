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
import java.util.Collections;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.mtnp.plot.Plot;
import ca.uqac.lif.mtnp.plot.gnuplot.GnuPlot;
import ca.uqac.lif.mtnp.table.Table;

/**
 * Callback showing a list of plots
 * 
 * @author Sylvain Hallé
 *
 */
public class PlotsPageCallback extends TemplatePageCallback
{
	protected static final transient Pattern s_pattern = Pattern.compile("exp-chk-(\\d+)");

	public PlotsPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/plots", lab, assistant);

	}

	@Override
	public String fill(String page, Map<String,String> params, boolean is_offline)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Plots");
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.GRAPH));
		if (GnuPlot.isGnuplotPresent())
		{
			out = out.replaceAll("\\{%PLOTS%\\}", getPlots());
			//if (AllPlotsCallback.s_pdftkPresent)
			{
				out = out.replaceAll("\\{%ALL_PLOTS%\\}", "<a class=\"btn-24 btn-all-plots\" title=\"Download all plots as a single PDF file\" href=\"/all-plots\">Download all plots</a>");
			}
			out = out.replaceAll("\\{%ALL_PLOTS_LATEX%\\}", "<a class=\"btn-24 btn-all-plots-latex\" title=\"Download LaTeX macros for using the plots\" href=\"/all-plots-latex\">Download LaTeX macros</a>");
		}
		else
		{
			out = out.replaceAll("\\{%PLOTS%\\}", "<p>Gnuplot was not detected on this system. The plot functionality is disabled.</p>");
		}
		out = out.replaceAll("\\{%SEL_PLOTS%\\}", "selected");
		return out;
	}

	/**
	 * Produces the list of plots
	 * @return A well-formatted HTML string showing of each of the lab's plots
	 */
	public String getPlots()
	{
		StringBuilder out = new StringBuilder();
		Vector<Integer> ids = new Vector<Integer>();
		ids.addAll(m_lab.getPlotIds());
		Collections.sort(ids);
		for (int id : ids)
		{
			Plot plot = m_lab.getPlot(id);
			Table t = plot.getTable();
			out.append("<div class=\"plot\">\n");
			out.append("<a href=\"plot/").append(id).append("\" target=\"_blank\" title=\"Click on plot to view in new window\">");
			out.append("<img src=\"plot/").append(id).append("\" alt=\"" + htmlEscape(plot.getNickname()) + "\" /></a>\n");
			out.append("<div><ul>");
			if (plot instanceof GnuPlot)
			{
				out.append("<li><a class=\"btn-24 btn-gp\" href=\"plot/").append(id).append("?format=gp&amp;dl=1\" title=\"Download as GnuPlot source file\">GP</a></li>");
				out.append("<li><a class=\"btn-24 btn-gp\" href=\"plot/").append(id).append("?format=dumb\" title=\"View as ASCII art\" target=\"_blank\">ASCII</a></li>");
			}
			out.append("<li><a class=\"btn-24 btn-pdf\" href=\"plot/").append(id).append("?format=pdf&amp;dl=1\" title=\"Download as PDF\">PDF</a></li>");
			if (t != null)
			{
				out.append("<li><a class=\"btn-24 btn-table\" href=\"table/").append(t.getId()).append("\" title=\"Show the data for this plot\">Table</a></li>");
			}
			out.append("</ul></div>\n");
			out.append("</div>\n");			
		}
		out.append("<div style=\"clear:both\"></div>\n");
		return out.toString();
	}
	
	@Override
	public String exportToStaticHtml(String path_to_root)
	{
		String contents = super.exportToStaticHtml(path_to_root);
		// Transform URLs for individual plot buttons
		contents = contents.replaceAll("src=\"(.*?)\\.html\"", "src=\"$1.png\"");
		contents = contents.replaceAll("href=\"plot/(.*?)\\.html\"", "href=\"plot/$1.png\"");
		contents = contents.replaceAll("href=\"plot/(.*?)\\?format=gp.*?\"", "href=\"plot/$1.gp\"");
		contents = contents.replaceAll("href=\"plot/(.*?)\\?format=dumb.*?\"", "href=\"plot/$1.txt\"");
		contents = contents.replaceAll("href=\"plot/(.*?)\\?format=pdf.*?\"", "href=\"plot/$1.pdf\"");
		contents = contents.replaceAll("href=\"all-plots\"", "href=\"plot/labpal-plots.pdf\"");
		contents = contents.replaceAll("href=\"all-plots-latex\"", "href=\"plot/labpal-plots.tex\"");
		return contents;
	}
	
	@Override
	public void addToZipBundle(ZipOutputStream zos) throws IOException
	{
		ZipEntry 	ze = new ZipEntry("plots.html");
		zos.putNextEntry(ze);
		zos.write(exportToStaticHtml("").getBytes());
		zos.closeEntry();
	}

}
