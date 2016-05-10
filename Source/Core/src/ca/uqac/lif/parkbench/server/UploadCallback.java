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

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.parkbench.FileHelper;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

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
			cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
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
				cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
				return cbr;
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
			cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
			return cbr;
		}
		Laboratory new_lab = m_lab.loadFromString(json);
		if (new_lab == null)
		{
			// Baaaad request
			cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
			
			return cbr;			
		}
		m_server.changeLab(new_lab);
		cbr.setCode(CallbackResponse.HTTP_OK);
		return cbr;
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
