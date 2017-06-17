/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

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

import java.util.regex.Pattern;

import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for an HTTP request in the ParkBench server.
 * 
 * @author Sylvain Hallé
 *
 */
public abstract class WebCallback extends RestCallback
{
	/**
	 * The current laboratory
	 */
	protected Laboratory m_lab;
	
	/**
	 * The lab assistant associated to the laboratory
	 */
	protected LabAssistant m_assistant;
	
	/**
	 * A regex pattern to identify parameter names with subscripts
	 */
	protected static final Pattern s_subscriptPattern = Pattern.compile("(\\w+)_(\\w+)");
	
	/**
	 * Creates a new callback
	 * @param path The path in the HTTP request to respond to
	 * @param lab The laboratory
	 * @param assistant The assistant
	 */
	public WebCallback(String path, Laboratory lab, LabAssistant assistant)
	{
		super(Method.GET, path);
		m_lab = lab;
		m_assistant = assistant;
	}
	
	/**
	 * Changes the laboratory associated with this callback
	 * @param lab The new laboratory
	 */
	public void changeLab(Laboratory lab)
	{
		m_lab = lab;
	}
	
	/**
	 * Escapes characters to HTML entities
	 * @param s The input string
	 * @return The escaped string
	 */
	public static String htmlEscape(String s)
	{
		if (s == null)
			return s;
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}
	
	/**
	 * Renders the name of a parameter, attempting to "beautify" it in some
	 * cases.
	 * @param key The parameter to render
	 * @return
	 */
	public static String beautifyParameterName(String key)
	{
		String out = htmlEscape(key);
		out = out.replaceAll("(\\w+)_(\\w+)", "$1<sub>$2</sub>");
		return out;
	}

}
