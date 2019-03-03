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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.azrael.SerializerException;
import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

import com.sun.net.httpserver.HttpExchange;

/**
 * Callback to download the lab's data.
 * 
 * @author Sylvain Hallé
 *
 */
public class DownloadCallback extends WebCallback
{
  public DownloadCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/download", lab, assistant);
    // setMethod(Method.GET);
    setMethod(Method.POST);
  }

  /**
   * Whether to zip the response
   */
  public static final boolean s_zip = true;

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse response = new CallbackResponse(t);
    String lab_contents = null;
    try
    {
      lab_contents = m_lab.saveToString();
    }
    catch (SerializerException e)
    {
      // Baaad request
      doBadRequest(response, "The lab's contents could not be saved");
      return response;
    }
    String filename = Server.urlEncode(m_lab.getTitle());
    if (s_zip)
    {
      // zip contents of JSON
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(bos);
      String ZE = filename + ".json";
      ZipEntry ze = new ZipEntry(ZE);
      try
      {
        zos.putNextEntry(ze);
        zos.write(lab_contents.getBytes());
        zos.closeEntry();
        zos.close();
      }
      catch (IOException e)
      {
        Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
      }
      response.setContents(bos.toByteArray());
      response.setContentType(Laboratory.s_mimeType);
      filename += "." + Laboratory.s_fileExtension;
    }
    else
    {
      // Send in clear text
      response.setContents(lab_contents);
      response.setContentType(CallbackResponse.ContentType.JSON);
      filename += ".json";
    }
    // Tell the browser to download the document rather than display it
    response.setAttachment(filename);
    return response;
  }

}
