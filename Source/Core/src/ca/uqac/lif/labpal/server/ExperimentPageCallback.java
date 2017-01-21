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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;
import ca.uqac.lif.labpal.Group;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.LabPalTui;

/**
 * Callback to display the details of one specific experiment.
 * 
 * @author Sylvain Hallé
 *
 */
public class ExperimentPageCallback extends TemplatePageCallback
{
	protected static final SimpleDateFormat s_dateFormat = new SimpleDateFormat();
	
	public ExperimentPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/experiment", lab, assistant);
	}
	
	@Override
	public String fill(String page, Map<String,String> params)
	{
		if (!params.containsKey("id"))
			return "";
		
		int experiment_nb = Integer.parseInt(params.get("id"));
		Experiment e = m_lab.getExperiment(experiment_nb);
		if (e == null)
		{
			return "";
		}
		if (params.containsKey("reset"))
		{
			e.reset();
		}
		if (params.containsKey("clean"))
		{
			e.clean();
		}
		Set<String> to_highlight = new HashSet<String>();
		if (params.containsKey("highlight"))
		{
			to_highlight = getKeysToHighlight(params.get("highlight"));
		}
		String out = page.replaceAll("\\{%TITLE%\\}", "Experiment #" + experiment_nb);
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.ERLENMEYER));
		out = out.replaceAll("\\{%EXP_NB%\\}", Integer.toString(experiment_nb));
		out = out.replaceAll("\\{%EXP_START%\\}", formatDate(e.getStartTime()));
		out = out.replaceAll("\\{%EXP_END%\\}", formatDate(e.getEndTime()));
		out = out.replaceAll("\\{%EXP_STATUS%\\}", ExperimentsPageCallback.getStatusLabel(e, m_assistant));
		out = out.replaceAll("\\{%EXP_ESTIMATE%\\}", LabPalTui.formatEta(e.getDurationEstimate(Laboratory.s_parkMips)));
		if (e.getEndTime() > 0)
		{
			out = out.replaceAll("\\{%EXP_DURATION%\\}", LabPalTui.formatEta((e.getEndTime() - e.getStartTime()) / 1000f));
		}
		out = out.replaceAll("\\{%EXP_BY%\\}", Matcher.quoteReplacement(htmlEscape(e.getWhoRan())));
		out = out.replaceAll("\\{%EXP_DATA%\\}", Matcher.quoteReplacement(renderHtml(e.getAllParameters(), "", e, to_highlight).toString()));
		String description = e.getDescription();
		out = out.replaceAll("\\{%EXP_DESCRIPTION%\\}", Matcher.quoteReplacement("<div class=\"description\">" + description + "</div>"));
		String timeout_string = "No timeout";
		if (e.getMaxDuration() > 0)
		{
			timeout_string = (e.getMaxDuration() / 1000) + " s";
		}
		out = out.replaceAll("\\{%EXP_TIMEOUT%\\}", timeout_string);
		String error_msg = e.getErrorMessage();
		if (!error_msg.isEmpty())
		{
			out = out.replaceAll("\\{%FAIL_MSG%\\}", Matcher.quoteReplacement("<h2>Error message</h2><pre>" + error_msg + "</pre>"));
		}
		if (e.hasWarnings())
		{
			StringBuilder warning_msg_build = new StringBuilder();
			for (ExperimentException ex : e.getWarnings())
			{
				warning_msg_build.append("<div>");
				warning_msg_build.append(ex.getMessage());
				warning_msg_build.append("</div>\n");
			}
			out = out.replaceAll("\\{%WARNINGS%\\}", Matcher.quoteReplacement("<h2>Warnings</h2>" + warning_msg_build.toString() + ""));
		}
		Set<Group> groups = m_lab.getGroups(experiment_nb);
		String group_description = "";
		for (Group g : groups)
		{
			group_description += g.getDescription();
		}
		if (!group_description.trim().isEmpty())
		{
			out = out.replaceAll("\\{%GROUP_DESC%\\}", Matcher.quoteReplacement("<div class=\"around-pulldown\">\n<h3 class=\"pulldown\">Generic description</h3>\n<div class=\"pulldown-contents\">" + group_description + "</div></div>"));
		}
		return out;
	}
	
	/**
	 * Formats the date
	 * @param timestamp A Unix timestamp 
	 * @return A formatted date
	 */
	protected static String formatDate(long timestamp)
	{
		if (timestamp < 0)
		{
			return "";
		}
		return s_dateFormat.format(new Date(timestamp));
	}
	
	/**
	 * Creates HTML code displaying (recursively) the experiment's parameters
	 * @param e The current JSON element in the parameters
	 * @param path The path in the experiment's parameters from the root
	 * @param exp The experiment
	 * @param to_highlight A set of datapoint IDs to highlight 
	 * @return A well-formatted HTML structure showing the parameters 
	 */
	public static StringBuilder renderHtml(JsonElement e, String path, Experiment exp, Set<String> to_highlight)
	{
		StringBuilder out = new StringBuilder();
		if (e instanceof JsonString)
		{
			out.append(((JsonString) e).stringValue());
		}
		else if (e instanceof JsonNumber)
		{
			out.append(((JsonNumber) e).numberValue());
		}
		else if (e instanceof JsonMap)
		{
			JsonMap m = (JsonMap) e;
			out.append("<table class=\"json-table\">\n");
			for (String k : m.keySet())
			{
				String path_append = "";
				if (!path.isEmpty())
				{
					path_append += ".";
				}
				path_append += k;
				out.append("<tr>");
				String css_class = "";
				if (to_highlight.contains(k))
				{
					css_class = " class=\"highlighted\"";
				}
				String p_desc = exp.getDescription(path_append);
				if (p_desc.isEmpty())
				{
					out.append("<th" + css_class + ">").append(htmlEscape(k)).append("</th>");
				}
				else
				{
					if (css_class.isEmpty())
					{
						out.append("<th class=\"with-desc\" title=\"").append(htmlEscape(p_desc)).append("\">").append(htmlEscape(k)).append("</th>");
					}
					else
					{
						out.append("<th class=\"with-desc highlighted\" title=\"").append(htmlEscape(p_desc)).append("\">").append(htmlEscape(k)).append("</th>");
					}
				}
				out.append("<td " + css_class + ">");
				JsonElement v = m.get(k);
				out.append(renderHtml(v, path_append, exp, to_highlight));
				out.append("</td></tr>\n");
			}
			out.append("</table>\n");
		}
		return out;
	}
	
	protected Set<String> getKeysToHighlight(String highlight)
	{
		Set<String> to_highlight = new HashSet<String>();
		String[] ids = highlight.split(",");
		for (String id : ids)
		{
			String[] parts = id.split(":");
			to_highlight.add(parts[1]);
		}
		return to_highlight;
	}
}
