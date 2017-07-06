/*
  LabPal, a versatile benchmark environment
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for loading a new laboratory from an uploaded file.
 * <strong>NOTE:</strong> currently, the file can only be uploaded
 * in clear-text (i.e. JSON). Uploading of zipped files (or any
 * binary file for that matter) does not work.
 * 
 * @author Sylvain Hallé
 *
 */
public class UploadCallback extends WebCallback
{	
	protected LabPalServer m_server;

	public UploadCallback(LabPalServer server, Laboratory lab, LabAssistant assistant)
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
		List<byte[]> parts = getParts(t);
		
		if (parts == null || parts.isEmpty())
		{ 
			// Baaaad request
			doBadRequest(cbr, "No file was uploaded");
			return cbr;
		}
		byte[] lab_file_contents = getPartContent(parts.get(0));
		String filenameClone = getPartName(parts.get(0));
		//upload file name 
		String json = null;
		//if (DownloadCallback.s_zip)
		if (filenameClone.endsWith(".zip") || filenameClone.endsWith(".labo"))
		{
			// This is a zipped file
			StringBuilder downloadsUrl = new StringBuilder();
			downloadsUrl.append(m_lab.getDownloadsUrl()).append("/");
			downloadsUrl.append(filenameClone);
			//ByteArrayInputStream bis = new ByteArrayInputStream(part);
			//System.out.println(FileHelper.readToString(bis));
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(lab_file_contents));
			ZipEntry entry;
			byte[] contents = null;
			try
			{

				//while((entry = zis.getNextEntry()) != null)
				entry = zis.getNextEntry();
				while (entry != null)
				{
					//String name = entry.getName();
					contents = extractFile(zis);
					break;
				}
			} 
			catch (IOException e) 
			{
				// Baaaad request
				doBadRequest(cbr, "Could not extract a lab from the file. The message is: <pre>" + e.getMessage() + "</pre>");
				return cbr;
			}
			if (contents != null)
			{
				json = new String(contents);
			}
		}
		else
		{
			json = new String(lab_file_contents).trim();
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
		String file_contents = FileHelper.internalFileToString(LabPalServer.class, TemplatePageCallback.s_path + "/upload-ok.html");
		file_contents = TemplatePageCallback.resolveInclude(file_contents);
		file_contents = file_contents.replaceAll("\\{%TITLE%\\}", "File uploaded");
		cbr.setCode(CallbackResponse.HTTP_OK);
		cbr.setContents(file_contents);
		return cbr;
	}

	protected void doBadRequest(CallbackResponse cbr, String message)
	{
		cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
		String file_contents = FileHelper.internalFileToString(LabPalServer.class, TemplatePageCallback.s_path + "/upload-nok.html");
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
	 * @param t The exchange
	 * @return The parts
	 */
	protected static List<byte[]> getParts(HttpExchange t)
	{
		InputStream is = t.getRequestBody();
		List<byte[]> byte_parts = null;
		try
		{
			byte[] byte_contents = streamToBytes(is);
			// Split the array of bytes based on the delimiter
			byte_parts = splitArray(byte_contents);
		}
		catch (IOException e)
		{
			// TODO: something better than that
			e.printStackTrace();
		}
		return byte_parts;
	}
	
	/**
	 * Gets the array of bytes corresponding to the contents of a part in a
	 * multi-part request
	 * @param part The part
	 * @return The byte array with the contents of that part
	 */
	protected static byte[] getPartContent(byte[] part)
	{
		int num_cr = 0;
		for (int i = 0; i < part.length; i++)
		{
			if (part[i] == 13)
			{
				num_cr++;
			}
			else
			{
				num_cr = 0;
			}
			if (num_cr == 2)
			{
				byte[] content = new byte[part.length - i - 1];
				for (int j = i + 1; j < part.length; j++)
				{
					content[j - i - 1] = part[j];
				}
				return content;
			}
		}
		return null;
	}
	
	protected static List<byte[]> splitArray(byte[] array)
	{
		List<byte[]> parts = new LinkedList<byte[]>();
		byte[] boundary = null;
		int last = 0;
		for (int i = 0; i < array.length && (boundary == null || i < array.length - boundary.length); i++)
		{
			if (boundary == null)
			{
				if (array[i] != 13)
					continue;
				else
				{
					boundary = new byte[i - 1];
					for (int j = 0; j < i - 1; j++)
					{
						boundary[j] = array[j];
					}
					last = i;
					continue;
				}
			}
			if (isDelimiter(array, boundary, i))
			{
				// A delimiter starts here
				if (i - last > 0)
				{
					byte[] new_part = new byte[i - last];
					for (int j = last; j < i; j++)
					{
						new_part[j - last] = array[j];
					}
					parts.add(new_part);
				}
				// Skip forward by the length of the delimiter
				i += boundary.length - 1; //-1 since the loop will increment i
			}
		}
		return parts;
	}
	
	protected static boolean isDelimiter(byte[] array, byte[] boundary, int position)
	{
		for (int i = 0; i < boundary.length; i++)
		{
			if (array[position + i] != boundary[i])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Converts an input stream into a byte array
	 * @param is The input stream
	 * @return The byte array
	 * @throws IOException
	 */
	protected static byte[] streamToBytes(InputStream is) throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		while ((nRead = is.read(data, 0, data.length)) != -1) 
		{
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	/**
	 * Gets the parts of a multipart message.
	 * <strong>Caveat emptor:</strong> this method works correctly with
	 * text, but not with binary files.
	 * @param t The exchange
	 * @return The parts
	 */
	protected static Map<String,byte[]> getPartsOld(HttpExchange t)
	{
		InputStream is = t.getRequestBody();
		Scanner sc = new Scanner(is, "ASCII");
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
				byte[] line_bytes = line.getBytes("ASCII");
				bos.write(line_bytes);
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

	/**
	 * Trims an array of bytes from its first and last byte. This is used to
	 * remove carriage returns from a field in an HTTP request
	 * @param in_array The array of bytes
	 * @return The new array of bytes without the first and last bytes
	 */
	protected static byte[] trimByteArray(byte[] in_array)
	{
		byte[] file_contents = new byte[in_array.length - 2];
		for (int i = 1; i < in_array.length - 1; i++)
		{
			file_contents[i - 1] = in_array[i];
		}
		return file_contents;
	}

}
