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

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for the home page, showing the lab's description
 * 
 * @author Sylvain Hallé
 *
 */
public class HomePageCallback extends TemplatePageCallback
{
	protected static final transient Pattern s_patternExecute = Pattern.compile("\\{J(.*?)J\\}");
	
	/**
	 * The relative URL of the home page
	 */
	public static final String URL = "/index";
	
	/**
	 * The description associated to the lab
	 */
	protected final transient String m_labDescription;
	
	public HomePageCallback(Laboratory lab, LabAssistant assistant)
	{
		super(URL, lab, assistant);
		String description = lab.getDescription();
		if (description == null || description.isEmpty())
		{
			// Use the help page if no description was given 
			description = FileHelper.internalFileToString(LabPalServer.class, "resource/help.html");
			description = description.replace("{!header.inc.html!}", "");
			description = description.replace("{!footer.inc.html!}", "");
		}
		m_labDescription = description;
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", htmlEscape(m_lab.getTitle()));
		out = out.replaceAll("\\{%SEL_HOME%\\}", "selected");
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.HOME));
		// We deliberately DON'T escape the description, as it is already valid HTML 
		out = out.replaceAll("\\{%LAB_DESCRIPTION%\\}", m_labDescription);
		out = resolveClassText(out);
		return out;
	}	
	
	/**
	 * Replaces all the bits of text of the form <tt>{J xyz J}</tt> by
	 * a call to the static method <tt>xyz.getClassText()</tt>. If class
	 * xyz does not exist or static method <tt>getClassText()</tt> does
	 * not exist, the empty string is used for the replacement.
	 * @param s The input HTML string
	 * @return The same string with the replacements made
	 */
	public final String resolveClassText(String s)
	{
		Matcher mat = s_patternExecute.matcher(s);
		Set<String> includes = new HashSet<String>();
		while (mat.find())
		{
			includes.add(mat.group(1));
		}
		for (String filename : includes)
		{
			try 
			{
				Class<?> clazz = m_lab.findClass(filename.trim());
				java.lang.reflect.Method method = clazz.getMethod("getClassText");
				Object o_desc = method.invoke(null);
				String description = (String) o_desc; 
				if (description == null)
					continue;
				s = s.replace("{J" + filename + "J}", description);
			} 
			catch (ClassNotFoundException e) 
			{
				// Silently fail
				e.printStackTrace();
			}
			catch (NoSuchMethodException e) 
			{
				// Silently fail
				e.printStackTrace();
			} 
			catch (SecurityException e) 
			{
				// Silently fail
				e.printStackTrace();
			}
			catch (IllegalAccessException e) 
			{
				// Silently fail
				e.printStackTrace();
			} 
			catch (IllegalArgumentException e) 
			{
				// Silently fail
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				// Silently fail
				e.printStackTrace();
			}
		}
		return s;
	}

}
