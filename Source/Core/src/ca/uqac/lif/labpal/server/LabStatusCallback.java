/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * A callback that produces a JSON string indicating the status and progression
 * of the lab as a whole. 
 * @author Sylvain Hallé
 */
public class LabStatusCallback extends LaboratoryCallback
{
	public LabStatusCallback(LabPalServer server, Method m, String path)
	{
		super(server, m, path);
	}
	
	@Override
	public boolean fire(HttpExchange t)
	{
		URI u = t.getRequestURI();
		String path = u.getPath();
		String method = t.getRequestMethod();
		return ((m_ignoreMethod || method.compareToIgnoreCase(methodToString(m_method)) == 0)) 
				&& path.startsWith(m_path);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse cbr = new CallbackResponse(t);
		cbr.setCode(CallbackResponse.HTTP_OK);
		cbr.setContentType(ContentType.JSON);
		List<Experiment> running = new ArrayList<Experiment>();
		int num_ex = 0, num_running = 0, num_q = 0, num_failed = 0, num_done = 0, num_warn = 0, num_interrupted = 0;
		for (Experiment ex : m_server.getLaboratory().getExperiments())
	    {
	      num_ex++;
	      switch (ex.getStatus())
	      {
	      case RUNNING:
	        num_running++;
	        running.add(ex);
	        break;
	      case DONE:
	        num_done++;
	        break;
	      case FAILED:
	        num_failed++;
	        break;
	      case INTERRUPTED:
	        num_interrupted++;
	        break;
	      default:
	        if (m_server.getLaboratory().isQueued(ex))
	        {
	          num_q++;
	        }
	        break;
	      }
	    }
		Collections.sort(running);
		JsonMap map = new JsonMap();		
		JsonList ids = new JsonList();
		for (int i = 0; i < running.size(); i++)
		{
			ids.add(running.get(i).getId());
		}
		map.put("ids", ids);
		map.put("total", num_ex);
		map.put("running", num_running);
		map.put("done", num_done);
		map.put("done-warning", num_warn);
		map.put("interrupted", num_interrupted);
		map.put("queued", num_q);
		map.put("failed", num_failed);
		cbr.setContents(map.toString());
		return cbr;
	}
}
