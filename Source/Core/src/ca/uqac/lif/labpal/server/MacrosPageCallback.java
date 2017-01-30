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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.macro.Macro;
import ca.uqac.lif.labpal.macro.MacroMap;
import ca.uqac.lif.labpal.macro.MacroNode;
import ca.uqac.lif.labpal.macro.MacroScalar;

/**
 * Callback showing the list of macros defined in this lab
 * 
 * @author Sylvain Hallé
 *
 */
public class MacrosPageCallback extends TemplatePageCallback
{
	public MacrosPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/macros", lab, assistant);
	}

	@Override
	public String fill(String page, Map<String,String> params)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Macros");
		Set<String> to_highlight = new HashSet<String>();
		if (params.containsKey("highlight"))
		{
			String[] parts = params.get("highlight").split(",");
			for (String p : parts)
			{
				to_highlight.add(p);
			}
		}
		Collection<Macro> macros = m_lab.getMacros();
		if (macros.isEmpty())
		{
			out = out.replaceAll("\\{%MACROS%\\}", "<p>No macro is associated to this lab.</p>\n");
		}
		else
		{
			out = out.replaceAll("\\{%MACROS%\\}", getMacros(macros, to_highlight));			
		}
		out = out.replaceAll("\\{%ALL_MACROS%\\}", Matcher.quoteReplacement("<p><a title=\"Download all macros as a single LaTeX file\" href=\"all-macros-latex\"><button class=\"btn btn-all-tables\">Download all macros</button></a></p>"));
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.TULIP));
		return out;
	}

	/**
	 * Produces the list of macros
	 * @return A well-formatted HTML string showing of each of the lab's macros
	 */
	public String getMacros(Collection<Macro> macros, Set<String> to_highlight)
	{
		StringBuilder out = new StringBuilder();
		out.append("<dl class=\"macros\">\n");
		for (Macro m : macros)
		{
			if (m instanceof MacroScalar)
			{
				MacroScalar ms = (MacroScalar) m;
				String css_class = "";
				String dp_id = MacroNode.getDatapointId(ms, "");
				if (to_highlight.contains(dp_id))
				{
					css_class = " class=\"highlighted\"";
				}
				out.append("<dt>").append("<a class=\"anchor\" name=\"").append(ms.getId()).append("\"></a>");
				out.append("<a").append(css_class).append(" href=\"explain?id=").append(dp_id).append("\"><span class=\"macro-name\">").append(ms.getName()).append("</span></a>: <span class=\"macro-value\">").append(ms.getValue()).append("</span></dt>\n");
				out.append("<dd>").append(ms.getDescription()).append("</dd>\n");
			}
			else if (m instanceof MacroMap)
			{
				MacroMap mm = (MacroMap) m;
				List<String> names = mm.getNames();
				Map<String,JsonElement> values = mm.getValues();
				for (String name : names)
				{
					String css_class = "";
					String dp_id = MacroNode.getDatapointId(mm, name);
					if (to_highlight.contains(dp_id))
					{
						css_class = " class=\"highlighted\"";
					}
					out.append("<dt>").append("<a class=\"anchor\" name=\"").append(m.getId()).append("\"></a>");
					out.append("<a").append(css_class).append(" href=\"explain?id=").append(dp_id).append("\"><span class=\"macro-name\">").append(name).append("</span></a>: <span class=\"macro-value\">").append(values.get(name)).append("</span></dt>\n");
					out.append("<dd>").append(mm.getDescription(name)).append("</dd>\n");
				}
			}
		}
		out.append("</dl>\n");
		return out.toString();
	}

}
