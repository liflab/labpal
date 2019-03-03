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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RestCleanCallback;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for an HTTP request in the LabPal server.
 * 
 * @author Sylvain Hallé
 *
 */
public abstract class WebCallback extends RestCleanCallback
{
  /**
   * The current laboratory
   */
  protected Laboratory m_lab;

  /**
   * The lab assistant associated to the laboratory
   */
  protected LabAssistant m_assistant;

  /**
   * Creates a new callback
   * 
   * @param path
   *          The path in the HTTP request to respond to
   * @param lab
   *          The laboratory
   * @param assistant
   *          The assistant
   */
  public WebCallback(String path, Laboratory lab, LabAssistant assistant)
  {
    super(Method.GET, path);
    m_lab = lab;
    m_assistant = assistant;
  }

  /**
   * Changes the laboratory associated with this callback
   * 
   * @param lab
   *          The new laboratory
   */
  public void changeLab(Laboratory lab)
  {
    m_lab = lab;
  }

  /**
   * Escapes characters to HTML entities
   * 
   * @param s
   *          The input string
   * @return The escaped string
   */
  public static String htmlEscape(String s)
  {
    if (s == null)
      return s;
    s = s.replaceAll("&", "&amp;");
    s = s.replaceAll("<", "&lt;");
    s = s.replaceAll(">", "&gt;");
    return s;
  }

  /**
   * Extracts parameters from the slash-separated list of arguments in an URL path
   * 
   * @param parameters
   *          The map of parameters parsed
   * @return The list of parameters
   */
  protected static List<String> getParametersFromPath(Map<String, String> parameters)
  {
    if (!parameters.containsKey(""))
    {
      return new ArrayList<String>(0);
    }
    String[] parts = parameters.get("").split("/");
    List<String> list = new ArrayList<String>(parts.length);
    for (String part : parts)
    {
      if (!part.trim().isEmpty())
        list.add(part);
    }
    return list;
  }

  /**
   * Creates an HTTP "bad request" response
   * 
   * @param cbr
   *          The callback response to fill with data
   * @param message
   *          The error message to return to the browser
   */
  protected static void doBadRequest(CallbackResponse cbr, String message)
  {
    cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
    String file_contents = FileHelper.internalFileToString(LabPalServer.class,
        TemplatePageCallback.s_path + "/error-message.html");
    file_contents = TemplatePageCallback.resolveInclude(file_contents);
    file_contents = file_contents.replaceAll("\\{%TITLE%\\}", "Error uploading file");
    file_contents = file_contents.replaceAll("\\{%MESSAGE%\\}", message);
    file_contents = file_contents.replaceAll("\\{%VERSION_STRING%\\}", Laboratory.s_versionString);
    cbr.setContents(file_contents);
  }

  /**
   * Adds contents to a zip archive of a bundle of all the server's pages. This is
   * used by the "export to static HTML" feature.
   * 
   * @param zos
   *          An output stream to add content to
   * @throws IOException
   *           Thrown if writing to the output stream cannot be done
   */
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {
    // Do nothing
    return;
  }
}
