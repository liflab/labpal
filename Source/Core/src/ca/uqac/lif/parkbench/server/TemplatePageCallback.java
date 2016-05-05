package ca.uqac.lif.parkbench.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;
import ca.uqac.lif.parkbench.PackageFileReader;

public class TemplatePageCallback extends ParkBenchCallback
{
	protected String m_pagePrefix;
	
	protected static final String s_path = "resource";

	public TemplatePageCallback(String prefix, Laboratory lab, LabAssistant assistant)
	{
		super(lab, assistant);
		m_pagePrefix = prefix;
	}

	@Override
	public boolean fire(HttpExchange h)
	{
		return h.getRequestURI().getPath().startsWith(m_pagePrefix);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		//Give the right content-type to the browser by giving it what it's looking for
		Headers headers = t.getRequestHeaders();
		String accept_Header = headers.get("Accept").get(0);
		response.setContentType(accept_Header.split(",")[0]);
		// Read file and put into response
		String filename = s_path + m_pagePrefix + ".html";
		System.out.println("Reading " + filename);
		String file_contents = PackageFileReader.readPackageFile(ParkbenchServer.class, filename);
		
		if (file_contents == null)
		{
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		response.setContents(render(file_contents));
		response.setCode(CallbackResponse.HTTP_OK);
		return response;
	}
	
	public String render(String s)
	{
		return s;
	}

}
