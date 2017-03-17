package ca.uqac.lif.labpal.server;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

public class CssCallback extends WebCallback 
{
	protected LabPalServer m_server;
	
	public CssCallback(LabPalServer server, Laboratory lab, LabAssistant assistant)
	{
		super("/screen.css", lab, assistant);
		m_server = server;
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		response.setCode(CallbackResponse.HTTP_OK);
		response.setContentType(ContentType.CSS);
		String file_contents = FileHelper.internalFileToString(LabPalServer.class, "resource/screen.css");
		String[] color_scheme = m_server.getColorScheme();
		for (int i = 0; i < color_scheme.length; i++)
		{
			file_contents = file_contents.replace("{%COLOR " + (i + 1) +"%}", color_scheme[i]);
		}
		response.setContents(file_contents);
		return response;
	}

}
