package ca.uqac.lif.parkbench.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RequestCallback;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class LoadCallback extends RequestCallback
{	
	protected ParkbenchServer m_server;
	
	protected Laboratory m_lab;
	
	protected LabAssistant m_assistant;
	
	public LoadCallback(ParkbenchServer server, Laboratory lab, LabAssistant assistant)
	{
		super();
		m_server = server;
		m_lab = lab;
		m_assistant = assistant;
	}

	@Override
	public boolean fire(HttpExchange t)
	{
		return t.getRequestMethod().compareToIgnoreCase("POST") == 0 &&
				t.getRequestURI().getPath().compareTo("/upload") == 0;
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse cbr = new CallbackResponse(t);
		InputStream is = t.getRequestBody();
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;
		byte[] contents = null;
		try
		{
			while((entry = zis.getNextEntry()) != null)
			{
				String name = entry.getName();
				contents = extractFile(zis, name);
				break;
			}
		} 
		catch (IOException e) 
		{
			// Baaaad request
			cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
			return cbr;
		}
		if (contents == null)
		{
			// Baaaad request
			cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
			return cbr;
		}
		String json = new String(contents);
		Laboratory new_lab = m_lab.loadFromString(json);
		if (new_lab == null)
		{
			// Baaaad request
			cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
			return cbr;			
		}
		m_lab = new_lab;
		return cbr;
	}
	
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @return Byte array
     * @throws IOException
     */
    private byte[] extractFile(ZipInputStream zipIn, String filePath) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
        return bos.toByteArray();
    }

}
