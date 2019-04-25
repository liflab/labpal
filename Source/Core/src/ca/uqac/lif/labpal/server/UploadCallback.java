/*
  LabPal, a versatile benchmark environment
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
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for loading a new laboratory from an uploaded file.
 * 
 * @author Sylvain Hallé
 *
 */
public class UploadCallback extends WebCallback
{
  protected LabPalServer m_server;

  public UploadCallback(LabPalServer server, Laboratory lab, LabAssistant assistant)
  {
    super("/upload", lab, assistant);
    setMethod(Method.POST);
    m_server = server;
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse cbr = new CallbackResponse(t);
    Map<String, byte[]> parts = HttpUtilities.getParts(t);
    Laboratory new_lab = null;
    if (parts == null || parts.isEmpty())
    {
      // Baaaad request
      doBadRequest(cbr, "No file was uploaded");
      return cbr;
    }
    String filename = null;
    for (String fn : parts.keySet())
    {
      filename = fn;
      break;
    }
    byte[] lab_file_contents = parts.get(filename);
    if (filename == null || filename.isEmpty() || lab_file_contents == null)
    {
      // Baaaad request
      doBadRequest(cbr, "No file was uploaded");
      return cbr;
    }
    String json = null;
    if (filename.endsWith(".zip") || filename.endsWith("." + Laboratory.s_fileExtension))
    {
      try
      {
        new_lab = m_lab.getFromZip(lab_file_contents);
      }
      catch (ReadException e)
      {
        // Baaaad request
        doBadRequest(cbr, "The file's contents could not be loaded into the "
            + "current laboratory. This can occur when you try loading the data from a different "
            + "lab. " + e.getMessage());
        return cbr;
      }
      catch (JsonParseException e)
      {
        // Baaaad request
        doBadRequest(cbr, "The file's contents could not be loaded into the "
            + "current laboratory. This can occur when you try loading the data from a different "
            + "lab. " + e.getMessage());
        return cbr;
      }
      catch (IOException e)
      {
        // Baaaad request
        doBadRequest(cbr, "The file's contents could not be loaded into the "
            + "current laboratory. This can occur when you try loading the data from a different "
            + "lab. " + e.getMessage());
        return cbr;
      }
    }
    else
    {
      // JSON sent in clear in the request
      json = new String(lab_file_contents);
      if (json.isEmpty())
      {
        // Baaaad request
        doBadRequest(cbr, "No file was uploaded");
        return cbr;
      }
      try
      {
        new_lab = m_lab.loadFromString(json);
      }
      catch (ReadException e)
      {
        // Baaaad request
        doBadRequest(cbr, "The file's contents could not be loaded into the "
            + "current laboratory. This can occur when you try loading the data from a different "
            + "lab. " + e.getMessage());
        return cbr;
      }
      catch (JsonParseException e)
      {
        // Baaaad request
        doBadRequest(cbr, "The file's contents could not be loaded into the "
            + "current laboratory. This can occur when you try loading the data from a different "
            + "lab. " + e.getMessage());
        return cbr;
      }
    }
    if (new_lab == null)
    {
      // Baaaad request
      doBadRequest(cbr,
          "The file's contents could not be loaded into the "
              + "current laboratory. This can occur when you try loading the data from a different "
              + "lab.");
      return cbr;
    }
    new_lab.setAssistant(m_assistant);
    m_server.changeLab(new_lab);
    String file_contents = FileHelper.internalFileToString(LabPalServer.class,
        TemplatePageCallback.s_path + "/upload-ok.html");
    file_contents = TemplatePageCallback.resolveInclude(file_contents);
    file_contents = file_contents.replaceAll("\\{%TITLE%\\}", "File uploaded");
    cbr.setCode(CallbackResponse.HTTP_OK);
    cbr.setContents(file_contents);
    return cbr;
  }
}
