package ca.uqac.lif.parkbench.server;

import java.util.Map;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;
import ca.uqac.lif.parkbench.Plot;
import ca.uqac.lif.parkbench.Plot.Terminal;

import com.sun.net.httpserver.HttpExchange;

public class PlotImageCallback extends ParkBenchCallback
{
	public PlotImageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/plot", lab, assistant);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		Map<String,String> params = getParameters(t);
		int plot_id = Integer.parseInt(params.get("id"));
		Plot p = m_lab.getPlot(plot_id);
		if (p == null)
		{
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		if (params.get("format").compareToIgnoreCase("gp") == 0)
		{
			response.setContents(p.toGnuplot(Terminal.PDF, m_lab.getTitle()));
			response.setCode(CallbackResponse.HTTP_OK);
			response.setAttachment(Server.urlEncode(p.getTitle() + ".gp"));
			return response;
		}
		Terminal term = Terminal.PNG;
		response.setContentType(ContentType.PNG);
		if (params.get("format").compareToIgnoreCase("pdf") == 0)
		{
			term = Terminal.PDF;
			response.setContentType(ContentType.PDF);
		}
		if (params.get("format").compareToIgnoreCase("dumb") == 0)
		{
			term = Terminal.DUMB;
			response.setContentType(ContentType.TEXT);
		}
		byte[] image = p.getImage(term);
		if (image == null)
		{
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;			
		}
		response.setContents(image);
		response.setCode(CallbackResponse.HTTP_OK);
		if (params.containsKey("dl"))
		{
			response.setAttachment(Server.urlEncode(p.getTitle() + "." + Plot.getTerminalName(term)));
		}
		return response;
	}

}
