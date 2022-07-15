package ca.uqac.lif.labpal.server;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.labpal.Laboratory;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public abstract class LaboratoryCallback extends RestCallback
{
	protected static String s_templateBaseDir = "resource";

	protected static String s_errorTemplate = "error.ftlh";
	
	protected static Configuration s_config;
	
	/**
	 * A reference to the server this class is a callback for.
	 */
	protected LabPalServer m_server;

	static
	{
		s_config = new Configuration(Configuration.VERSION_2_3_29);
		s_config.setClassForTemplateLoading(TemplatePageCallback.class, s_templateBaseDir);
		s_config.setDefaultEncoding("UTF-8");
		s_config.setLocale(Locale.US);
		s_config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}
	
	public LaboratoryCallback(LabPalServer s, Method m, String path)
	{
		super(m, path);
		m_server = s;
	}
	
	public LabPalServer getServer()
	{
		return m_server;
	}
	
	@Override
	public boolean fire(HttpExchange t)
	{
		URI u = t.getRequestURI();
		String path = u.getPath();
		String method = t.getRequestMethod();
		return ((m_ignoreMethod || method.compareToIgnoreCase(methodToString(m_method)) == 0)) 
				&& path.startsWith(m_path);
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
