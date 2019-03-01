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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.NumberHelper;

import com.sun.net.httpserver.HttpExchange;

public class EditParametersCallback extends WebCallback
{

  public EditParametersCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/edit-parameters", lab, assistant);
    setMethod(Method.POST);
  }

  @Override
  public CallbackResponse process(HttpExchange t)
  {
    CallbackResponse cbr = new CallbackResponse(t);
    Map<String, String> params = getParameters(t);
    if (!params.containsKey("exp-id"))
    {
      doBadRequest(cbr, "No experiment ID was passed to the page");
      return cbr;
    }
    int exp_id = Integer.parseInt(params.get("exp-id").trim());
    JsonMap new_parameters = new JsonMap();
    Experiment e = m_lab.getExperiment(exp_id);
    if (e == null)
    {
      doBadRequest(cbr, "Experiment #" + exp_id + " cannot be found");
      return cbr;
    }
    for (String key : params.keySet())
    {
      if (!key.startsWith("fld-"))
        continue;
      String param_name = key.substring(4);
      String value = null;
      try
      {
        value = URLDecoder.decode(params.get(key), "UTF-8");
      }
      catch (UnsupportedEncodingException e1)
      {
        // Not supposed to happen
        doBadRequest(cbr, e1.getMessage());
        return cbr;
      }
      if (NumberHelper.isNumeric(value))
      {
        Number num = NumberHelper.toPrimitiveNumber(value);
        new_parameters.put(param_name, num);
      }
      else
      {
        new_parameters.put(param_name, value);
      }
    }
    try
    {
      e.editCallback(new_parameters);
    }
    catch (ExperimentException ex)
    {
      doBadRequest(cbr, ex.getMessage());
      return cbr;
    }
    cbr.setCode(CallbackResponse.HTTP_REDIRECT);
    cbr.setHeader("Location", "/experiment/" + exp_id);
    return cbr;
  }

}
