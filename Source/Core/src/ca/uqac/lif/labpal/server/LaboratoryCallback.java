package ca.uqac.lif.labpal.server;

import java.util.HashMap;
import java.util.Map;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.labpal.Laboratory;

public abstract class LaboratoryCallback extends RestCallback
{
	protected LabPalServer m_server;
	
	public LaboratoryCallback(LabPalServer s, Method m, String path)
	{
		super(m, path);
		m_server = s;
	}
	
	public void doBadRequest(CallbackResponse cbr, String message)
	{
		Map<String,Object> input = new HashMap<String,Object>();
		input.put("title", "Error");
		input.put("errormessage", message);
		input.put("versionstring", Laboratory.formatVersion());
		cbr.setContents(TemplatePageCallback.render(input, "error.ftlh"));
		cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
	}
}
