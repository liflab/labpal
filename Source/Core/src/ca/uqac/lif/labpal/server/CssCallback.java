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

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;

/**
 * Special callback for the CSS stylesheet. It is handled as a FreeMaker
 * template instead of a static text file, since some color names are
 * dynamically populated based on server settings.
 * @author Sylvain Hallé
 */
public class CssCallback extends TemplatePageCallback
{

	public CssCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location);
	}
	
	@Override
	public CallbackResponse process(HttpExchange h)
	{
		// Override parent to change content type to CSS
		m_title = "";
		CallbackResponse cbr = super.process(h);
		cbr.setContentType(ContentType.CSS);
		return cbr;
	}

	@Override
	public void fillInputModel(HttpExchange h, Map<String,Object> input) 
	{
		String[] color_scheme = m_server.getColorScheme();
		for (int i = 0; i < color_scheme.length; i++)
		{
			input.put("color" + (i + 1), color_scheme[i]);
		}
	}
}
