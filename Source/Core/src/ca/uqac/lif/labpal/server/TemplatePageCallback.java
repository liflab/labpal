/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for a template page. This callback loads an internal HTML
 * file with the same name as the HTTP request, and replaces all
 * instructions of the form <tt>{!file.html!}</tt> by the contents of
 * <tt>file.html</tt> at that location (thus providing a basic templating
 * mechanism).
 * 
 * @author Sylvain Hallé
 *
 */
public class TemplatePageCallback extends WebCallback
{
	public static final transient String s_path = "resource";
	
	protected static final transient Pattern s_patternInclude = Pattern.compile("\\{!(.*?)!\\}");
	
	public static enum IconType {ERLENMEYER, TABLE, STATUS, GRAPH, HOME, HELP, ASSISTANT, BINOCULARS, TULIP};
	
	protected String m_filename = null;

	public TemplatePageCallback(String prefix, Laboratory lab, LabAssistant assistant)
	{
		super(prefix, lab, assistant);
		m_filename = s_path + m_path + ".html";
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		response.disableCaching();
		response.setContentType(ContentType.HTML);
		Map<String,String> params = getParameters(t);
		// Read file and put into response
		String file_contents = readTemplateFile();
		if (file_contents == null)
		{
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		String contents = render(file_contents, params);
		if (contents == null)
		{
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
		}
		else
		{
			response.setContents(contents);
			response.setCode(CallbackResponse.HTTP_OK);
		}
		return response;
	}
	
	/**
	 * Reads the template file associated to this page
	 * @return The contents of the template file
	 */
	protected String readTemplateFile()
	{
		String file_contents = FileHelper.internalFileToString(LabPalServer.class, m_filename);
		return file_contents;
	}
	
	public static final String resolveInclude(String s)
	{
		Matcher mat = s_patternInclude.matcher(s);
		Set<String> includes = new HashSet<String>();
		while (mat.find())
		{
			includes.add(mat.group(1));
		}
		for (String filename : includes)
		{
			String file_contents = FileHelper.internalFileToString(LabPalServer.class, s_path + "/" + filename);
			if (file_contents == null)
				continue;
			s = s.replace("{!" + filename + "!}", file_contents);
		}
		return s;
	}
		
	public final String render(String s, Map<String,String> params)
	{
		s = resolveInclude(s);
		s = fill(s, params);
		if (s == null)
		{
			return null;
		}
		s = s.replaceAll("\\{%VERSION_STRING%\\}", Laboratory.s_versionString);
		s = s.replaceAll("\\{%LAB_NAME%\\}", m_lab.getTitle());
		s = s.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.ERLENMEYER));
		s = s.replaceAll("\\{%.*?%\\}", "");
		s = s.replaceAll("\\{J.*?J\\}", "");
		return s;
	}
	
	protected String fill(String s, Map<String,String> params)
	{
		return s;
	}
	
	public static String getFavicon(IconType t)
	{
		switch (t)
		{
		case ERLENMEYER:
			return "images/erlenmeyer-48.png";
		case STATUS:
			return "images/status-48.png";
		case GRAPH:
			return "images/graph-48.png";
		case TABLE:
			return "images/table-48.png";
		case HELP:
			return "images/help-48.png";
		case HOME:
			return "images/home-48.png";
		case ASSISTANT:
			return "images/assistant-48.png";
		case BINOCULARS:
			return "images/find-48.png";
		case TULIP:
			return "images/tulip-48.png";
		default:
			return "images/erlenmeyer-48.png";
		
		}
	}
	
	/**
	 * Exports the page as a static HTML page
	 * @param path_to_root The relative path to the root of the lab
	 * @return The contents of the page
	 */
	public String exportToStaticHtml(String path_to_root)
	{
		String file = readTemplateFile();
		String contents = render(file, new HashMap<String,String>());
		contents = createStaticLinks(contents);
		contents = relativizeUrls(contents, path_to_root);
		return contents;
	}
}
