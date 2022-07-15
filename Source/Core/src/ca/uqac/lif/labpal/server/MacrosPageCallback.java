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

import java.util.Map;

/**
 * A callback that produces a JSON string indicating the status and progression
 * of each table in the lab. 
 * @author Sylvain Hallé
 */
public class MacrosPageCallback extends TemplatePageCallback
{
	public MacrosPageCallback(LabPalServer server, Method m, String path, String template_location, String highlight)
	{
		super(server, m, path, template_location, highlight);
	}
	
	@Override
	public void fillInputModel(String uri, Map<String,String> req_parameters, Map<String,Object> input, Map<String,byte[]> parts) throws PageRenderingException
	{
		super.fillInputModel(uri, req_parameters, input, parts);
		input.put("title", "Macros");
		input.put("groups", m_server.m_lab.getMacroGroups());
	}
}
