/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * A page exposing the entire lab as a template variable.
 * 
 * @author Sylvain Hallé
 */
public class TemplatePageCallback extends RestCallback
{
	protected static String s_templateBaseDir = "resource";
	
	protected static String s_errorTemplate = "error.ftlh";

	protected String m_templateLocation;

	protected String m_title;

	protected LabPalServer m_server;

	private static Configuration s_config;

	protected String m_menuHighlight;

	static
	{
		s_config = new Configuration(Configuration.VERSION_2_3_29);
		s_config.setClassForTemplateLoading(TemplatePageCallback.class, s_templateBaseDir);
		s_config.setDefaultEncoding("UTF-8");
		s_config.setLocale(Locale.US);
		s_config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	public TemplatePageCallback(LabPalServer server, Method m, String path, String template_location, String menu_highlight)
	{
		super(m, path);
		m_server = server;
		m_templateLocation = template_location;
		m_menuHighlight = menu_highlight;
		m_ignoreMethod = true;
	}

	public TemplatePageCallback(LabPalServer server, Method m, String path, String template_location) 
	{
		this(server, m, path, template_location, "");
	}

	public TemplatePageCallback setTitle(String title)
	{
		m_title = title;
		return this;
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

	@Override
	public CallbackResponse process(HttpExchange h)
	{
		Map<String,Object> input = new HashMap<String,Object>();
		input.put("title", m_title);
		CallbackResponse cbr = new CallbackResponse(h);
		cbr.setCode(CallbackResponse.HTTP_OK);
		cbr.setContentType(ContentType.HTML);
		String template_location = m_templateLocation;
		try
		{
			fillInputModel(h, input);
		}
		catch (PageRenderingException e)
		{
			cbr.setCode(e.getCode());
			template_location = s_errorTemplate;
			input.put("errormessage", e.getMessage());
			input.put("title", e.getTitle());
		}
		try 
		{
			Template temp = s_config.getTemplate(template_location);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(baos);
			temp.process(input, osw);
			cbr.setContents(baos.toByteArray());
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

	protected void fillInputModel(HttpExchange h, Map<String,Object> input) throws PageRenderingException
	{
		Map<String,String> m = getParameters(h);
		for (Map.Entry<String,String> e : m.entrySet())
		{
			Object o = liftObject(e.getKey(), e.getValue());
			input.put(e.getKey(), o);
		}
		input.put("versionstring", Laboratory.formatVersion());
		input.put("lab", m_server.getLaboratory());
		input.put("menuhighlight", m_menuHighlight);
	}

	protected Object liftObject(String key, Object value)
	{
		return value;
	}
}
