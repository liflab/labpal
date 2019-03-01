/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2018 Sylvain Hallé

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
import java.io.InputStream;

import ca.uqac.lif.azrael.SerializerException;
import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

import com.sun.net.httpserver.HttpExchange;

/**
 * Merges the current lab with the one sent in the HTTP request
 * 
 * @author Sylvain Hallé
 */
public class MergeCallback extends WebCallback
{
  public static final String s_path = "/merge";

  public MergeCallback(Laboratory lab, LabAssistant assistant)
  {
    super(s_path, lab, assistant);
    setMethod(Method.POST);
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse cbr = new CallbackResponse(t);
    cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
    InputStream is = t.getRequestBody();
    try
    {
      byte[] payload = HttpUtilities.streamToBytes(is);
      Laboratory lab_to_merge = m_lab.getFromZip(payload);
      m_lab.mergeWith(lab_to_merge);
    }
    catch (IOException e)
    {
      cbr.setContents("The contents of the request could not be read");
      return cbr;
    }
    catch (SerializerException e)
    {
      cbr.setContents(e.getMessage());
      return cbr;
    }
    catch (JsonParseException e)
    {
      cbr.setContents(e.getMessage());
      return cbr;
    }
    cbr.setCode(CallbackResponse.HTTP_OK);
    return cbr;
  }

}
