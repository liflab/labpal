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

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.petitpoucet.PartNode;

/**
 * Callback for to find a data point by its LDI
 * 
 * @author Sylvain Hallé
 *
 */
public class FindFormCallback extends TemplatePageCallback
{
  public FindFormCallback(LabPalServer s)
  {
    super(s, Method.GET, "/find", "find.ftlh", "top-menu-find");
    setTitle("Find");
    m_ignoreMethod = true;
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
      PartNode pn = m_server.getLaboratory().getExplanation(datapoint_id);
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
    return super.process(t);
  }
}
