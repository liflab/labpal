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

import java.io.IOException;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.json.JsonPrinter;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.Persistent.PersistenceException;

/**
 * Callback to download the lab's data.
 * @author Sylvain Hallé
 */
public class DownloadCallback extends LaboratoryCallback
{
	/**
	 * Whether to zip the response
	 */
	public static final boolean s_zip = true;

	public DownloadCallback(LabPalServer server)
	{
		super(server, Method.GET, "/download");
		m_ignoreMethod = true;
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
		CallbackResponse response = new CallbackResponse(t);
		String filename = Server.urlEncode(m_server.getLaboratory().getName());
		try
		{
			if (s_zip)
			{
				// zip contents of JSON
				byte[] bytes = m_server.getLaboratory().saveToZip();
				response.setContents(bytes);
				response.setContentType(Laboratory.s_mimeType);
				filename += "." + Laboratory.s_fileExtension;
			}
			else
			{
				// Send in clear text
				Object o = m_server.getLaboratory().saveState();
				String lab_contents = new JsonPrinter().print(o).toString();
				response.setContents(lab_contents);
				response.setContentType(CallbackResponse.ContentType.JSON);
				filename += ".json";
			}
		}
		catch (PrintException | IOException | PersistenceException | FileSystemException e)
		{
			// Baaad request
			response.setCode(CallbackResponse.HTTP_BAD_REQUEST);
			response.setContents("The lab's contents could not be saved");
			return response;
		}
		// Tell the browser to download the document rather than display it
		response.setAttachment(filename);
		return response;
	}
}
