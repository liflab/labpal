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

import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.GraphvizRenderer;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.provenance.DotProvenanceTreeRenderer;
import ca.uqac.lif.petitpoucet.ProvenanceNode;

/**
 * Callback producing an image explaining the provenance of a data point, as an
 * image in various formats.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>dl=1</tt>: to download the image instead of displaying it. This will
 * prompt the user to save the file in its browser</li>
 * <li><tt>id=x</tt>: mandatory; the ID of the data point to create the graph
 * from</li>
 * <li><tt>format=x</tt>: the requested image format. Currenly supports pdf and
 * png
 * </ul>
 * 
 * @author Sylvain Hallé
 *
 */
public class ExplainImageCallback extends WebCallback
{
  public ExplainImageCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/provenance-graph", lab, assistant);
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse response = new CallbackResponse(t);
    Map<String,String> params = getParameters(t);
    String datapoint_id = params.get("id");
    ProvenanceNode node = m_lab.getDataTracker().explain(datapoint_id);
    if (node == null)
    {
      response.setContents(
          "<html><body><h1>Not Found</h1><p>This data point does not seem to exist.</p></body></html>");
      response.setCode(CallbackResponse.HTTP_NOT_FOUND);
      return response;
    }
    DotProvenanceTreeRenderer renderer = new DotProvenanceTreeRenderer();
    if (!GraphvizRenderer.s_dotPresent)
    {
      // Asking for an image, but DOT not available: stop right here
      response.setContents(
          "<html><body><h1>Not Found</h1><p>DOT is not present on this system, so the picture cannot be shown.</p></body></html>");
      response.setCode(CallbackResponse.HTTP_NOT_FOUND);
      return response;
    }
    String extension = "svg";
    response.setContentType("image/svg+xml");
    if (params.containsKey("format") && params.get("format").compareToIgnoreCase("pdf") == 0)
    {
      response.setContentType(ContentType.PDF);
      extension = "pdf";
    }
    byte[] image = renderer.toImage(node, extension);
    if (image == null)
    {
      response.setContents(
          "<html><body><h1>Internal Server Error</h1><p>The image cannot be displayed.</p></body></html>");
      response.setCode(CallbackResponse.HTTP_INTERNAL_SERVER_ERROR);
      return response;
    }
    response.setContents(image);
    response.setCode(CallbackResponse.HTTP_OK);
    if (params.containsKey("dl"))
    {
      response.setAttachment(
          Server.urlEncode(node.getNodeFunction().getDataPointId() + "." + extension));
    }
    return response;
  }
}
