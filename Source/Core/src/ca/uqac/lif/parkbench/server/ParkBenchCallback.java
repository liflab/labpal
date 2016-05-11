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
package ca.uqac.lif.parkbench.server;

import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

/**
 * Callback for an HTTP request in the ParkBench server.
 * 
 * @author Sylvain Hallé
 *
 */
public abstract class ParkBenchCallback extends RestCallback
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
	 * Creates a new callback
	 * @param path The path in the HTTP request to respond to
	 * @param lab The laboratory
	 * @param assistant The assistant
	 */
	public ParkBenchCallback(String path, Laboratory lab, LabAssistant assistant)
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

}
