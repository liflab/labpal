/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2022 Sylvain Hallé

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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.petitpoucet.PartNode;

/**
 * Callback for to find a data point by its LDI
 * 
 * @author Sylvain Hallé
 *
 */
public class FindFormCallback extends WebCallback
{
  public FindFormCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/find", lab, assistant);
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse response = new CallbackResponse(t);
    response.disableCaching();
    response.setContentType(ContentType.HTML);
    Map<String, String> params = getParameters(t);
    if (params.containsKey("id"))
    {
      String datapoint_id = params.get("id");
      PartNode pn = m_lab.getExplanation(datapoint_id);
      if (pn == null)
      {
        response.setCode(CallbackResponse.HTTP_NOT_FOUND);
        response.setContents(
            "<html><body><h1>Not Found</h1><p>This data point does not seem to exist.</p></body></html>");
        return response;
      }
      String url = ExplainCallback.getDataPointUrl(pn);
      if (url == null || url.isEmpty() || url.compareTo("#") == 0)
      {
        response.setCode(CallbackResponse.HTTP_NOT_FOUND);
        response.setContents(
            "<html><body><h1>Not Found</h1><p>This data point does not seem to exist.</p></body></html>");
        return response;
      }
      response.setCode(CallbackResponse.HTTP_REDIRECT);
      response.setHeader("Location", url);
      return response;
    }
    // Give the right content-type to the browser by giving it what it's looking for
    Headers headers = t.getRequestHeaders();
    String accept_Header = headers.get("Accept").get(0);
    response.setContentType(accept_Header.split(",")[0]);
    // Read file and put into response
    String file_contents = FileHelper.internalFileToString(LabPalServer.class,
        TemplatePageCallback.s_path + m_path + ".html");
    if (file_contents == null)
    {
      response.setCode(CallbackResponse.HTTP_INTERNAL_SERVER_ERROR);
      return response;
    }
    file_contents = TemplatePageCallback.resolveInclude(file_contents);
    file_contents = file_contents.replaceAll("\\{%TITLE%\\}", "Find a data point");
    file_contents = file_contents.replaceAll("\\{%SEL_FIND%\\}", "selected");
    file_contents = file_contents.replaceAll("\\{%VERSION_STRING%\\}", Laboratory.s_versionString);
    String doi = m_lab.getDoi();
    if (!doi.isEmpty())
    {
      file_contents = file_contents.replaceAll("\\{%DOI%\\}", doi + "/");
    }
    else
    {
      file_contents = file_contents.replaceAll("\\{%DOI%\\}", "");
    }
    file_contents = file_contents.replaceAll("\\{%FAVICON%\\}",
        TemplatePageCallback.getFavicon(TemplatePageCallback.IconType.BINOCULARS));
    response.setContents(file_contents);
    response.setCode(CallbackResponse.HTTP_OK);
    return response;
  }
}
