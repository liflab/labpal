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

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.jerrydog.InnerFileCallback;

/**
 * Special callback for the CSS stylesheet. It is handled as a FreeMaker
 * template instead of a static text file, since some color names are
 * dynamically populated based on server settings.
 * @author Sylvain Hallé
 */
public class JavaScriptCallback extends InnerFileCallback
{

	public JavaScriptCallback(String location, Class<?> context)
	{
		super(location, context);
	}
	
	@Override
	public boolean fire(HttpExchange t)
	{
		URI u = t.getRequestURI();
		String path = u.getPath();
		return (path.endsWith(".js"));
	}
	
	@Override
	public CallbackResponse process(HttpExchange h)
	{
		// Override parent to change content type to JS
		CallbackResponse cbr = super.process(h);
		cbr.setContentType(ContentType.JS);
		return cbr;
	}
}
