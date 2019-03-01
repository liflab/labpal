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

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Special callback for the CSS file. The source CSS file contains placeholders
 * that are to be replaced by actual color values when queried.
 * 
 * @author Sylvain Hallé
 *
 */
public class CssCallback extends WebCallback
{
  protected LabPalServer m_server;

  public CssCallback(LabPalServer server, Laboratory lab, LabAssistant assistant)
  {
    super("/screen.css", lab, assistant);
    m_server = server;
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse response = new CallbackResponse(t);
    response.setCode(CallbackResponse.HTTP_OK);
    response.setContentType(ContentType.CSS);
    response.setContents(getCssContents());
    return response;
  }

  /**
   * Exports the CSS file as a static
   * 
   * @param string
   *          Unused
   * @return The contents of the CSS file
   */
  public String exportToStaticHtml(String string)
  {
    return getCssContents();
  }

  protected String getCssContents()
  {
    String file_contents = FileHelper.internalFileToString(LabPalServer.class,
        "resource/screen.css");
    String[] color_scheme = m_server.getColorScheme();
    for (int i = 0; i < color_scheme.length; i++)
    {
      file_contents = file_contents.replace("{%COLOR " + (i + 1) + "%}", color_scheme[i]);
    }
    return file_contents;
  }

}
