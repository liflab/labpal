/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2020 Sylvain Hallé

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

/**
 * Callback to download all plots as a single, multi-page PDF file. This
 * callback makes use of Apache PDFBox library, in the background. 
 * 
 * @author Sylvain Hallé
 *
 */
public class AllPlotsCallback extends WebCallback
{

  public AllPlotsCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/all-plots", lab, assistant);
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse response = new CallbackResponse(t);

    Map<String, String> params = getParameters(t);
    boolean with_captions = false;
    if (params.containsKey("captions"))
    {
      with_captions = true;
    }

    byte[] file_contents;

    try
    {
      file_contents = export(with_captions);
    }
    catch (Exception e)
    {
      response.setCode(CallbackResponse.HTTP_BAD_REQUEST);
      return response;
    }

    response.setContentType(ContentType.PDF);
    String filename = Server.urlEncode("labpal-plots.pdf");
    response.setAttachment(filename);
    response.setContents(file_contents);

    return response;
  }

  byte[] export(boolean with_captions) throws IOException
  {
    List<String> filenames = new ArrayList<String>();
    for (int id : m_lab.getPlotIds())
    {
      Plot plot = m_lab.getPlot(id);
      // Get plot's image and write to temporary file
      byte[] image = plot.getImage(Plot.ImageType.PDF, with_captions);
      File tmp_file = File.createTempFile("plot", ".pdf");
      tmp_file.deleteOnExit();
      FileOutputStream fos = new FileOutputStream(tmp_file);
      if (image == null || image.length == 0)
      {
    	  // Substitute plot for a blank image
    	  image = Plot.s_blankImagePdf;
      }
      fos.write(image, 0, image.length);
      fos.flush();
      fos.close();
      String filename = tmp_file.getPath();
      filenames.add(filename);
    }
    String[] tab = filenames.toArray(new String[filenames.size()]);
    byte[] file_contents = FileHelper.mergePdf(File.createTempFile("plots", ".pdf").getPath(),
        tab);
    return file_contents;
  }

  /**
   * Gets the name given to the file containing all the plots
   * 
   * @param lab
   *          The lab
   * @return The name
   */
  public static String getPlotsFilename(Laboratory lab)
  {
    return "labpal-plots.pdf";
  }

  @Override
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {
    ZipEntry ze = new ZipEntry("plot/labpal-plots.pdf");
    zos.putNextEntry(ze);
    zos.write(export(true));
    zos.closeEntry();
  }
}
