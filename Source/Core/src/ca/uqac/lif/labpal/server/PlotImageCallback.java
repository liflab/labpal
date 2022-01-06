/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2019 Sylvain Hallé

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
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.LabPalGnuplot;
import ca.uqac.lif.labpal.plot.LabPalPlot;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.plots.gnuplot.Gnuplot;

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
public class PlotImageCallback extends WebCallback
{
  public PlotImageCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/plot", lab, assistant);
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse response = new CallbackResponse(t);
    Map<String, String> params = getParameters(t);
    List<String> path_parts = getParametersFromPath(params);
    int plot_id = -1;
    if (!path_parts.isEmpty())
    {
      plot_id = Integer.parseInt(path_parts.get(0));
    }
    else if (params.containsKey("id"))
    {
      plot_id = Integer.parseInt(params.get("id"));
    }
    LabPalPlot p = m_lab.getPlot(plot_id);
    if (p == null)
    {
      response.setCode(CallbackResponse.HTTP_NOT_FOUND);
      return response;
    }
    if (params.containsKey("format") && params.get("format").compareToIgnoreCase("gp") == 0
        && p instanceof LabPalGnuplot)
    {
      response.setContents(((LabPalGnuplot) p).toGnuplot(PlotFormat.PDF, m_lab.getTitle(), true));
      response.setCode(CallbackResponse.HTTP_OK);
      response.setAttachment(Server.urlEncode(p.getTitle() + ".gp"));
      return response;
    }
    if (!Gnuplot.isGnuplotPresent())
    {
      // Asking for an image, but Gnuplot not available: stop right here
      response.setCode(CallbackResponse.HTTP_NOT_FOUND);
      return response;
    }
    PlotFormat term = PlotFormat.PNG;
    response.setContentType(ContentType.PNG);
    if (params.containsKey("format") && params.get("format").compareToIgnoreCase("pdf") == 0)
    {
      term = PlotFormat.PDF;
      response.setContentType(ContentType.PDF);
    }
    if (params.containsKey("format") && params.get("format").compareToIgnoreCase("dumb") == 0)
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
    if (params.containsKey("dl"))
    {
      response.setAttachment(Server.urlEncode(p.getTitle() + "." + term.getExtension()));
    }
    return response;
  }

  /**
   * Gets the image file corresponding to a plot in the given format
   * 
   * @param plot_id
   *          The ID of the plot to display
   * @param format
   *          The image file format (png, pdf, dumb or gp)
   * @return An array of bytes containing the image
   */
  public byte[] exportTo(int plot_id, String format)
  {
    LabPalPlot p = m_lab.getPlot(plot_id);
    byte[] image = null;
    if (format.compareToIgnoreCase("png") == 0)
    {
      image = p.getImage(PlotFormat.PNG);
    }
    else if (format.compareToIgnoreCase("pdf") == 0)
    {
      image = p.getImage(PlotFormat.PDF);
    }
    else if (format.compareToIgnoreCase("dumb") == 0 && p instanceof LabPalGnuplot)
    {
      image = p.getImage(Gnuplot.DUMB);
    }
    else if (format.compareToIgnoreCase("gp") == 0 && p instanceof LabPalGnuplot)
    {
      image = ((LabPalGnuplot) p).toGnuplot(PlotFormat.PDF, m_lab.getTitle(), true).getBytes();
    }
    return image;
  }

  @Override
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {
    String filename;
    // Plots in various formats
    Set<Integer> ids = m_lab.getPlotIds();
    byte[] byte_contents;
    for (int id : ids) // PNG
    {
      byte_contents = exportTo(id, "png");
      if (byte_contents == null)
      {
        continue;
      }
      filename = "plot/" + id + ".png";
      ZipEntry ze = new ZipEntry(filename);
      zos.putNextEntry(ze);
      zos.write(byte_contents);
      zos.closeEntry();
    }
    for (int id : ids) // PDF
    {
      byte_contents = exportTo(id, "pdf");
      if (byte_contents == null)
      {
        continue;
      }
      filename = "plot/" + id + ".pdf";
      ZipEntry ze = new ZipEntry(filename);
      zos.putNextEntry(ze);
      zos.write(byte_contents);
      zos.closeEntry();
    }
    for (int id : ids) // DUMB
    {
      byte_contents = exportTo(id, "dumb");
      if (byte_contents == null)
      {
        continue;
      }
      filename = "plot/" + id + ".txt";
      ZipEntry ze = new ZipEntry(filename);
      zos.putNextEntry(ze);
      zos.write(byte_contents);
      zos.closeEntry();
    }
    for (int id : ids) // GP
    {
      byte_contents = exportTo(id, "gp");
      if (byte_contents == null)
      {
        continue;
      }
      filename = "plot/" + id + ".gp";
      ZipEntry ze = new ZipEntry(filename);
      zos.putNextEntry(ze);
      zos.write(byte_contents);
      zos.closeEntry();
    }
  }

}
