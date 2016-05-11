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

import java.util.Map;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

/**
 * Callback for the home page, showing the lab's description
 * 
 * @author Sylvain Hallé
 *
 */
public class HomePageCallback extends TemplatePageCallback
{
	/**
	 * The description associated to the lab
	 */
	protected final transient String m_labDescription;
	
	public HomePageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/index", lab, assistant);
		String description = lab.getDescription();
		if (description == null || description.isEmpty())
		{
			description = "<h1>" + lab.getTitle() + "</h1>\n<p>No description was entered for this lab.</p>";
		}
		m_labDescription = description;
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", htmlEscape(m_lab.getTitle()));
		out = out.replaceAll("\\{%SEL_HOME%\\}", "selected");
		// We deliberately DON'T escape the description, as it is already valid HTML 
		out = out.replaceAll("\\{%LAB_DESCRIPTION%\\}", m_labDescription);
		return out;
	}	
}
