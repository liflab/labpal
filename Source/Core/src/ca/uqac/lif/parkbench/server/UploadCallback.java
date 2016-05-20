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
package ca.uqac.lif.parkbench.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.azrael.SerializerException;
import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.parkbench.FileHelper;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

/**
 * Callback for loading a new laboratory from an uploaded file.
 * <strong>NOTE:</strong> currently, the file can only be uploaded
 * in clear-text (i.e. JSON). Uploading of zipped files (or any
 * binary file for that matter) does not work.
 * 
 * @author Sylvain Hallé
 *
 */
public class UploadCallback extends ParkBenchCallback
{	
	protected ParkbenchServer m_server;

	public UploadCallback(ParkbenchServer server, Laboratory lab, LabAssistant assistant)
	{
		super("/upload", lab, assistant);
		setMethod(Method.POST);
		m_server = server;
	}

	/*@Override
	public boolean fire(HttpExchange t)
	{
		return t.getRequestMethod().compareToIgnoreCase("POST") == 0 &&
				t.getRequestURI().getPath().compareTo("/upload") == 0;
	}*/

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse cbr = new CallbackResponse(t);
		Map<String,byte[]> parts = getParts(t);
		if (parts == null || !parts.containsKey("filename"))
		{
			// Baaaad request
			doBadRequest(cbr, "No file was uploaded");
			return cbr;
		}
		byte[] part = parts.get("filename");
		String json = null;
		if (DownloadCallback.s_zip)
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(part);
			System.out.println(FileHelper.readToString(bis));
			ZipInputStream zis = new ZipInputStream(bis);
			ZipEntry entry;
			byte[] contents = null;
			try
			{
				while((entry = zis.getNextEntry()) != null)
				{
					//String name = entry.getName();
					contents = extractFile(zis);
					break;
				}
			} 
			catch (IOException e) 
			{
				// Baaaad request
				doBadRequest(cbr, "Could not extract a lab from the file");
			}
			if (contents != null)
			{
				json = new String(contents);
			}
		}
		else
		{
			if (part != null)
			{
				json = new String(part).trim();
			}
		}
		if (json == null)
		{
			// Baaaad request
			doBadRequest(cbr, "No JSON was found in the uploaded file");
			return cbr;
		}
		if (json.isEmpty())
		{
			// Baaaad request
			doBadRequest(cbr, "No file was uploaded");
			return cbr;
		}
		Laboratory new_lab = null;
		try 
		{
			new_lab = m_lab.loadFromString(json);
		} 
		catch (SerializerException e) 
		{
			// Baaaad request
			doBadRequest(cbr, "The file's contents could not be loaded into the "
					+ "current laboratory. This can occur when you try loading the data from a different "
					+ "lab. " + e.getMessage());
			return cbr;
		}
		catch (JsonParseException e) 
		{
			// Baaaad request
			doBadRequest(cbr, "The file's contents could not be loaded into the "
					+ "current laboratory. This can occur when you try loading the data from a different "
					+ "lab. " + e.getMessage());
			return cbr;
		}
		if (new_lab == null)
		{
			// Baaaad request
			doBadRequest(cbr, "The file's contents could not be loaded into the "
					+ "current laboratory. This can occur when you try loading the data from a different "
					+ "lab.");
			return cbr;
		}
		new_lab.setAssistant(m_assistant);
		m_server.changeLab(new_lab);
		String file_contents = FileHelper.internalFileToString(ParkbenchServer.class, TemplatePageCallback.s_path + "/upload-ok.html");
		file_contents = TemplatePageCallback.resolveInclude(file_contents);
		file_contents = file_contents.replaceAll("\\{%TITLE%\\}", "File uploaded");
		cbr.setCode(CallbackResponse.HTTP_OK);
		cbr.setContents(file_contents);
		return cbr;
	}
	
	protected void doBadRequest(CallbackResponse cbr, String message)
	{
		cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
		String file_contents = FileHelper.internalFileToString(ParkbenchServer.class, TemplatePageCallback.s_path + "/upload-nok.html");
		file_contents = TemplatePageCallback.resolveInclude(file_contents);
		file_contents = file_contents.replaceAll("\\{%TITLE%\\}", "Error uploading file");
		file_contents = file_contents.replaceAll("\\{%MESSAGE%\\}", message);
		file_contents = file_contents.replaceAll("\\{%VERSION_STRING%\\}", Laboratory.s_versionString);
		cbr.setContents(file_contents);
	}

	/**
	 * Extracts a zip entry (file entry)
	 * @param zipIn The zip input stream
	 * @return Byte array
	 * @throws IOException
	 */
	private byte[] extractFile(ZipInputStream zipIn) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] bytesIn = new byte[4096];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) 
		{
			bos.write(bytesIn, 0, read);
		}
		bos.close();
		return bos.toByteArray();
	}

	/**
	 * Gets the parts of a multipart message.
	 * <strong>Caveat emptor:</strong> this method works correctly with
	 * text, but not with binary files.
	 * @param t The exchange
	 * @return The parts
	 */
	protected static Map<String,byte[]> getParts(HttpExchange t)
	{
		InputStream is = t.getRequestBody();
		Scanner sc = new Scanner(is);
		Map<String,byte[]> out = new HashMap<String,byte[]>();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String filename = "";
		String boundary = "";
		sc.useDelimiter("\\n");
		try
		{
			while (sc.hasNextLine())
			{
				String line = sc.nextLine();
				if (boundary.isEmpty())
				{
					boundary = line;
					continue;
				}
				if (line.startsWith("Content-Disposition"))
				{
					Pattern pat = Pattern.compile("\\bname=\"(.*?)\"");
					Matcher mat = pat.matcher(line);
					if (mat.find())
					{
						filename = mat.group(1);
					}
					continue;
				}
				if (line.startsWith("Content-Type"))
				{
					continue;
				}
				if (line.startsWith(boundary))
				{
					out.put(filename, bos.toByteArray());
					bos.reset();
					filename = "";
					continue;
				}
				bos.write(line.getBytes());
				bos.write((char) 13); // The scanner trimmed this byte when reading
			}
		}
		catch (IOException ioe)
		{
			// Do nothing
		}
		sc.close();
		return out;
	}

}
