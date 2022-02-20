package ca.uqac.lif.labpal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreemarkerCallback extends RestCallback
{
	protected String m_templateLocation;
	
	protected String m_title;
	
	private static Configuration s_config;
	
	static
	{
		s_config = new Configuration(Configuration.VERSION_2_3_29);
        s_config.setClassForTemplateLoading(FreemarkerCallback.class, "server");
		s_config.setDefaultEncoding("UTF-8");
		s_config.setLocale(Locale.US);
		s_config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}
	
	public FreemarkerCallback(Method m, String path, String template_location) 
	{
		super(m, path);
		m_templateLocation = template_location;
	}
	
	public FreemarkerCallback setTitle(String title)
	{
		m_title = title;
		return this;
	}

	@Override
	public CallbackResponse process(HttpExchange h)
	{
		Map<String,Object> input = getInputModel(h);
		input.put("title", m_title);
		CallbackResponse cbr = new CallbackResponse(h);
		cbr.setContentType(ContentType.HTML);
		try
		{
			
			Template temp = s_config.getTemplate(m_templateLocation);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(baos);
			temp.process(input, osw);
			cbr.setContents(baos.toByteArray());
			cbr.setCode(CallbackResponse.HTTP_OK);
		}
		catch (IOException | TemplateException e) 
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			cbr.setContents(baos.toByteArray());
			cbr.setCode(CallbackResponse.HTTP_INTERNAL_SERVER_ERROR);
		}
		return cbr;
	}
	
	protected Map<String,Object> getInputModel(HttpExchange h)
	{
		Map<String,String> m = getParameters(h);
		Map<String,Object> m_o = new HashMap<String,Object>(m.size());
		for (Map.Entry<String,String> e : m.entrySet())
		{
			Object o = liftObject(e.getKey(), e.getValue());
			m_o.put(e.getKey(), o);
		}
		return m_o;
	}
	
	protected Object liftObject(String key, Object value)
	{
		return value;
	}
}
