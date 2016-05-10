package ca.uqac.lif.parkbench.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

import com.sun.net.httpserver.HttpExchange;

public class DownloadCallback extends ParkBenchCallback
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
