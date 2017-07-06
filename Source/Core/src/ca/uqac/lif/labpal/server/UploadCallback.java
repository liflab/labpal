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
		Map<String,byte[]> parts = getParts(t);
		
		if (parts == null || parts.isEmpty())
		{ 
			// Baaaad request
			doBadRequest(cbr, "No file was uploaded");
			return cbr;
		}
		String filename = "";
		for (String fn : parts.keySet())
		{
			filename = fn;
			break;
		}
		byte[] lab_file_contents = parts.get(filename); 
		String json = null;
		if (filename.endsWith(".zip") || filename.endsWith(".labo"))
		{
			// This is a zipped file
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(lab_file_contents));
			ZipEntry entry;
			byte[] contents = null;
			try
			{
				entry = zis.getNextEntry();
				while (entry != null)
				{
					//String name = entry.getName();
					contents = extractFile(zis);
					// We assume the zip to contain a single file 
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
			json = new String(lab_file_contents);
			System.out.println(json);
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
	protected static Map<String,byte[]> getParts(HttpExchange t)
	{
		Map<String,byte[]> parts_map = new HashMap<String,byte[]>();
		List<byte[]> bin_parts = getBinaryParts(t);
		for (byte[] part : bin_parts)
		{
			String name = getPartName(part);
			byte[] content = trimByteArray(getPartContent(part));
			parts_map.put(name, content);
		}
		return parts_map;
	}

	/**
	 * Gets the parts of a multipart message.
	 * @param t The exchange
	 * @return The parts
	 */
	protected static List<byte[]> getBinaryParts(HttpExchange t)
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
		byte last = 0;
		for (int i = 0; i < part.length; i++)
		{
			if (part[i] == 13)
			{
				num_cr++;
			}
			else if (part[i] == 10)
			{
				if (last != 13)
				{
					num_cr++;
				}
			}
			else
			{
				num_cr = 0;
			}
			last = part[i];
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
	
	/**
	 * Gets the filename corresponding to this part of a
	 * multi-part request
	 * @param part The part
	 * @return The filename
	 */
	protected static String getPartName(byte[] part)
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(part);
		Scanner sc = new Scanner(bais);
		while (sc.hasNextLine())
		{
			String line = sc.nextLine();
			if (line.startsWith("Content-Disposition"))
			{
				Pattern pat = Pattern.compile("\\bfilename=\"(.*?)\"");
				Matcher mat = pat.matcher(line);
				if (mat.find())
				{
					sc.close();
					return mat.group(1);
				}
			}
		}
		sc.close();
		return "";
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
	 * Trims an array of bytes from any bytes in the beginning and the end that
	 * are either {@code CR} or {@code LF}. This is used to
	 * remove carriage returns from a field in an HTTP request
	 * @param in_array The array of bytes
	 * @return The new array of bytes without the first and last two bytes
	 */
	protected static byte[] trimByteArray(byte[] in_array)
	{
		
		int left = 0, right = in_array.length;
		for (int i = 0; i < in_array.length; i++)
		{
			if (in_array[i] == 10 || in_array[i] == 13)
			{
				left++;
			}
			else
			{
				break;
			}
		}
		for (int i = in_array.length - 1; i >= 0; i--)
		{
			if (in_array[i] == 10 || in_array[i] == 13)
			{
				right = i;
			}
			else
			{
				break;
			}
		}
		byte[] file_contents = new byte[right - left];
		for (int i = left; i < right; i++)
		{
			file_contents[i - left] = in_array[i];
		}
		return file_contents;
	}

}
