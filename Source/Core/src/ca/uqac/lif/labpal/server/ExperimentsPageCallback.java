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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import bsh.EvalError;
import bsh.Interpreter;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentGroup;
import ca.uqac.lif.labpal.experiment.Experiment.Status;

public class ExperimentsPageCallback extends TemplatePageCallback
{
	public ExperimentsPageCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-experiments");
	}

	@Override
	public void fillInputModel(HttpExchange h, Map<String,Object> input) throws PageRenderingException
	{
		super.fillInputModel(h, input);
		input.put("title", "Experiments");
		if (input.containsKey("filter"))
		{
			String filter = (String) input.get("filter");
			try 
			{
			    filter = java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8.name());
			} 
			catch (UnsupportedEncodingException e) 
			{
			    // not going to happen - value came from JDK's own StandardCharsets
			}
			input.put("filter", filter);
			input.put("groups", getGroups(h, filter.trim()));
		}
		else
		{
			input.put("groups", m_server.m_lab.getExperimentGroups());
		}
	}
	
	protected List<ExperimentGroup> getGroups(HttpExchange h, String query)
	{
		if (query == null || query.trim().isEmpty())
		{
			return m_server.m_lab.getExperimentGroups();
		}
		List<ExperimentGroup> l_groups = new ArrayList<ExperimentGroup>();
		Interpreter interpreter = new Interpreter();
		for (ExperimentGroup eg : m_server.m_lab.getExperimentGroups())
		{
			ExperimentGroup new_eg = new ExperimentGroup(eg.getName(), eg.getDescription());
			new_eg.setId(eg.getId());
			for (Experiment e : eg.getObjects())
			{
				try 
				{
					fillInterpreter(interpreter, e);
					Object o = interpreter.eval(query);
					if (Boolean.TRUE.equals(o))
					{
						new_eg.add(e);
					}
				}
				catch (EvalError ee) 
				{
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}
			}
			if (!new_eg.getObjects().isEmpty())
			{
				l_groups.add(new_eg);
			}
		}
		return l_groups;
	}
	
	protected static void fillInterpreter(Interpreter i, Experiment e) throws EvalError
	{
		Status s = e.getStatus();
		i.set("e", e);
		i.set("id", e.getId());
		i.set("ready", s == Status.READY);
		i.set("failed", s == Status.FAILED);
		i.set("done", s == Status.DONE);
	}
}
