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

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Exports the whole contents of the lab's web interface
 * as a bundle of static HTML files.
 * @author Sylvain Hallé
 */
public class ExportStaticCallback extends WebCallback
{
	/**
	 * The server this callback is attached to
	 */
	protected final LabPalServer m_server;

	public ExportStaticCallback(Laboratory lab, LabAssistant assistant, LabPalServer server)
	{
		super("/export-static", lab, assistant);
		m_server = server;
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse cbr = new CallbackResponse(t);
		cbr.setCode(CallbackResponse.HTTP_OK);
		byte[] contents = m_server.exportToStaticHtml();
		cbr.setContentType(ContentType.ZIP);
		cbr.setContents(contents);
		cbr.setAttachment(Server.urlEncode(m_lab.getTitle()) + ".zip");
		return cbr;
	}

}
