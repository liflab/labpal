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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.Persistent.PersistenceException;

/**
 * Callback to upload the lab's data.
 * @author Sylvain Hallé
 */
public class UploadCallback extends StatusPageCallback
{
	public UploadCallback(LabPalServer server, String template_location)
	{
		super(server, Method.POST, "/upload", template_location);
		m_ignoreMethod = true;
		m_getParts = true;
	}

	@Override
	public void fillInputModel(String uri, Map<String,String> req_parameters, Map<String,Object> input, Map<String,byte[]> req_parts) throws PageRenderingException
	{
		Laboratory new_lab = m_server.getLaboratory();
		if (req_parts == null || req_parts.isEmpty())
		{
			// Baaaad request
			throw new PageRenderingException(CallbackResponse.HTTP_BAD_REQUEST, "Upload error", "No file was uploaded");
		}
		String filename = null;
		for (String fn : req_parts.keySet())
		{
			filename = fn;
			break;
		}
		byte[] lab_file_contents = req_parts.get(filename);
		if (filename == null || filename.isEmpty() || lab_file_contents == null)
		{
			// Baaaad request
			throw new PageRenderingException(CallbackResponse.HTTP_BAD_REQUEST, "Upload error", "No file was uploaded");
		}
		String json = null;
		try
		{
			if (filename.endsWith(".zip") || filename.endsWith("." + Laboratory.s_fileExtension))
			{
				new_lab.loadFromZipFile(new ByteArrayInputStream(lab_file_contents));
			}
			else
			{
				// JSON sent in clear in the request
				json = new String(lab_file_contents);
				if (json.isEmpty())
				{
					// Baaaad request
					throw new PageRenderingException(CallbackResponse.HTTP_BAD_REQUEST, "Upload error", "No file was uploaded");
				}
				new_lab.loadFromJsonString(json);
			}
		}
		catch (PersistenceException | IOException e)
		{
			// Baaaad request
			throw new PageRenderingException(CallbackResponse.HTTP_BAD_REQUEST, "Upload error", "The file's contents could not be loaded into the current laboratory. This can occur when you try loading the data from a different lab. " + e.getMessage());
		}
		super.fillInputModel(uri, req_parameters, input, req_parts);
		input.put("title", "Status");
		input.put("serialization_message", "<p class=\"message info\"><span>The file was loaded successfully.</span></p>");
	}
}
