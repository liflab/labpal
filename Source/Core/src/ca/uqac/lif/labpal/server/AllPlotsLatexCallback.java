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
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.sun.net.httpserver.HttpExchange;
import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.mtnp.plot.Plot;
import ca.uqac.lif.mtnp.plot.PlotNode;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.rendering.LatexTableRenderer;

/**
 * Callback to download a LaTeX file defining a unique command for each plot of
 * the lab. This file is to be used in conjunction with the
 * {@link AllPlotsCallback}, which produces a single, multi-page PDF file.
 * 
 * @author Sylvain Hallé
 *
 */
public class AllPlotsLatexCallback extends TemplatePageCallback
{
  public AllPlotsLatexCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/all-plots-latex", lab, assistant);
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse response = new CallbackResponse(t);
    StringBuilder out = generateBodyLatex(this.m_lab);
    response.setContentType(ContentType.LATEX);
    String filename = Server.urlEncode("labpal-plots.tex");
    response.setAttachment(filename);
    response.setContents(out.toString());
    return response;
  }

  public static StringBuilder generateBodyLatex(Laboratory m_lab)
  {
    StringBuilder out = new StringBuilder();
    out.append("% ----------------------------------------------------------------")
        .append(FileHelper.CRLF);
    out.append("% File generated by LabPal ").append(Laboratory.s_versionString)
        .append(FileHelper.CRLF);
    out.append("% Date:     ").append(String.format("%1$te-%1$tm-%1$tY", Calendar.getInstance()))
        .append(FileHelper.CRLF);
    out.append("% Lab name: ").append(m_lab.getTitle()).append(FileHelper.CRLF);
    out.append("%").append(FileHelper.CRLF)
        .append("% To insert one of the figures into your text, do:").append(FileHelper.CRLF);
    out.append("% \\begin{figure}").append(FileHelper.CRLF).append("% \\usebox{\\boxname}")
        .append(FileHelper.CRLF).append("% \\end{figure}").append(FileHelper.CRLF);
    out.append("% where \\boxname is one of the boxes defined in the file below")
        .append(FileHelper.CRLF);
    out.append("% ----------------------------------------------------------------")
        .append(FileHelper.CRLF).append(FileHelper.CRLF);
    int page_nb = 0;
    for (int id : m_lab.getPlotIds())
    {
      page_nb++;
      Plot plot = m_lab.getPlot(id);
      Table tab = plot.getTable();
      String box_name = plot.getTitle();
      if (!plot.getNickname().isEmpty())
      {
        box_name = plot.getNickname();
      }
      else if (!tab.getNickname().isEmpty())
      {
        box_name = "plot" + tab.getNickname();
      }
      if (box_name.compareTo("Untitled") == 0)
      {
        box_name += id;
      }
      box_name = LatexTableRenderer.formatName(box_name);
      String plots_filename = Server.urlEncode(AllPlotsCallback.getPlotsFilename(m_lab));
      out.append("% ----------------------").append(FileHelper.CRLF).append("% Plot: ")
          .append(box_name).append(FileHelper.CRLF);
      out.append("% ----------------------").append(FileHelper.CRLF);
      out.append("\\newsavebox{\\").append(box_name).append("}").append(FileHelper.CRLF);
      out.append("\\begin{lrbox}{\\").append(box_name).append("}").append(FileHelper.CRLF);
      out.append("\\href{").append(PlotNode.getDataPointId(plot)).append("}{");
      out.append("\\includegraphics[page=").append(page_nb).append(",width=\\linewidth]{")
          .append(plots_filename).append("}");
      out.append("}");
      out.append(FileHelper.CRLF).append("\\end{lrbox}").append(FileHelper.CRLF)
          .append(FileHelper.CRLF);
    }

    return out;
  }

  @Override
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {

    StringBuilder out = generateBodyLatex(this.m_lab);
    ZipEntry ze = new ZipEntry("plot/labpal-plots.tex");
    zos.putNextEntry(ze);
    zos.write(out.toString().getBytes());
    zos.closeEntry();

  }

}
