/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

import com.sun.net.httpserver.HttpExchange;

/**
 * Callback to download the lab's data.
 * 
 * @author Sylvain Hallé
 *
 */
public class DownloadCallback extends WebCallback
{
	public DownloadCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/download", lab, assistant);
	}

	/**
	 * Whether to zip the response. Currently, downloading as a zip
	 * works OK, but uploading as a zip does not work.
	 */
	public static final boolean s_zip = false;

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		String lab_contents = m_lab.saveToString();
		CallbackResponse response = new CallbackResponse(t);
		String filename = Server.urlEncode(m_lab.getTitle());
		if (s_zip)
		{
			// zip contents of JSON
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(bos);
			ZipEntry ze = new ZipEntry("Status.json");
			try
			{
				zos.putNextEntry(ze);
				zos.write(lab_contents.getBytes());
				zos.closeEntry();
				zos.close();
			}
			catch (IOException e)
			{
				Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
			}
			response.setContents(bos.toByteArray());
			response.setContentType(CallbackResponse.ContentType.ZIP);
			filename += ".zip";
		}
		else
		{
			// Send in clear text
			response.setContents(lab_contents);
			response.setContentType(CallbackResponse.ContentType.JSON);
			filename += "." + Laboratory.s_fileExtension;
		}
		// Tell the browser to download the document rather than display it
		response.setAttachment(filename);
		return response;
	}

}
