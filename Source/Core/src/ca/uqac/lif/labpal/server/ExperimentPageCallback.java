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
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;

public class ExperimentPageCallback extends TemplatePageCallback
{
	/**
	 * The pattern to extract the experiment ID from the URL.
	 */
	protected static final Pattern s_idPattern = Pattern.compile("experiment/(\\d+)");

	public ExperimentPageCallback(LabPalServer server, Method m, String path, String template_location)
	{
		super(server, m, path, template_location, "top-menu-experiments");
	}

	@Override
	public void fillInputModel(String uri, Map<String,String> req_parameters, Map<String,Object> input, Map<String,byte[]> parts) throws PageRenderingException
	{
		int id = fetchId(s_idPattern, uri);
		Experiment e = m_server.getLaboratory().getExperiment(id);
		if (e == null)
		{
			throw new PageRenderingException(CallbackResponse.HTTP_NOT_FOUND, "Not found", "No such experiment");
		}
		super.fillInputModel(uri, req_parameters, input, parts);
		input.put("outparams", formatTable(input, e, e.getOutputParameters()));
		if (uri.contains("output/html"))
		{
			input.put("outputonly", true);
		}
		else
		{
			if (uri.contains("reset"))
			{
				e.reset();
			}
			input.put("id", id);
			input.put("title", "Experiment " + id);
			input.put("inparams", formatTable(input, e, e.getInputParameters()));
			input.put("exceptionexplainable", false);
			Exception ex = e.getException();
			if (ex != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ex.printStackTrace(new PrintStream(baos));
				input.put("exceptionmessage", baos.toString());
				if (ex instanceof ExplanationQueryable)
				{
					// This exception can be explained
					input.put("exceptionexplainable", true);
				}
			}
		}
	}

	protected static String formatTable(Map<String,Object> input, Experiment e, Map<String,Object> params)
	{
		Set<String> to_highlight = new HashSet<String>();
    if (input.containsKey("highlight"))
    {
      to_highlight = getKeysToHighlight((String) input.get("highlight"));
    }
    String outparams = "";
    if (params.isEmpty())
    {
    	outparams = "<p>No parameter is defined.</p>";
    }
    else
    {
    	outparams = renderHtml(params, "", e, to_highlight).toString();
    }
    return outparams;
	}

	protected static Map<String,String> formatParameters(Map<String,Object> params)
	{
		Map<String,String> formatted = new HashMap<String,String>();
		for (Map.Entry<String,Object> e : params.entrySet())
		{
			formatted.put(e.getKey(), e.getValue().toString());
		}
		return formatted;
	}

	/**
	 * Creates HTML code displaying (recursively) the experiment's parameters
	 * 
	 * @param e
	 *          The current JSON element in the parameters
	 * @param path
	 *          The path in the experiment's parameters from the root
	 * @param exp
	 *          The experiment
	 * @param to_highlight
	 *          A set of datapoint IDs to highlight
	 * @return A well-formatted HTML structure showing the parameters
	 */
	public static StringBuilder renderHtml(Object e, String path, Experiment exp,
			Set<String> to_highlight)
	{
		StringBuilder out = new StringBuilder();
		if (e instanceof Number)
		{
			out.append(((Number) e));
		}
		else if (e instanceof List)
		{
			out.append("<table class=\"json-table\">\n");
			int el_cnt = 0;
			for (Object v : (List<?>) e)
			{
				String path_append = path + ":" + el_cnt;
				String css_class_key = "";
				String css_class_value = "";
				if (containsExactly(to_highlight, path_append))
				{
					css_class_value += " class=\"highlighted\"";
				}
				if (containsPrefix(to_highlight, path_append))
				{
					css_class_key += " class=\"highlighted\"";
				}
				out.append("<tr><th").append(css_class_key).append(">").append(el_cnt).append("</th>");
				out.append("<td").append(css_class_value).append(">");
				out.append(renderHtml(v, path_append, exp, to_highlight));
				out.append("</td></tr>\n");
				el_cnt++;
			}
			out.append("</table>\n");
		}
		else if (e instanceof Map)
		{
			Map<?,?> m = (Map<?,?>) e;
			out.append("<table class=\"json-table\">\n");
			for (Object k : m.keySet())
			{
				String path_append = "";
				if (!path.isEmpty())
				{
					path_append += ".";
				}
				path_append += k;
				out.append("<tr>");
				String css_class_key = "";
				String css_class_value = "";
				if (containsExactly(to_highlight, path_append))
				{
					css_class_value += " class=\"highlighted\"";
				}
				if (containsPrefix(to_highlight, path_append))
				{
					css_class_key += " highlighted";
				}
				String p_desc = exp.getDescription(path_append);
				if (p_desc.isEmpty())
				{
					out.append("<th class=\"" + css_class_key + "\">").append(htmlEscape(k.toString())).append("</th>");
				}
				else
				{
					out.append("<th class=\"with-desc").append(css_class_key).append("\" title=\"")
					.append(htmlEscape(p_desc)).append("\">").append(htmlEscape(k.toString())).append("</th>");
				}
				out.append("<td " + css_class_value + ">");
				Object v = m.get(k);
				out.append(renderHtml(v, path_append, exp, to_highlight));
				out.append("</td></tr>\n");
			}
			out.append("</table>\n");
		}
		else
		{
			// Fallback for unknown types 
			out.append(e.toString());
		}
		return out;
	}

	/**
	 * Gets the set of keys that should be highlighted in the table of experiment
	 * results.
	 * 
	 * @param highlight
	 *          The key to highlight
	 * @return A set of keys to highlight
	 */
	protected static Set<String> getKeysToHighlight(String highlight)
	{
		Set<String> to_highlight = new HashSet<String>();
		String[] ids = highlight.split(",");
		for (String id : ids)
		{
			to_highlight.add(id);
		}
		return to_highlight;
	}

	/**
	 * Checks if a set of strings contains exactly one specific string
	 * 
	 * @param set
	 *          The set
	 * @param key
	 *          The string
	 * @return {@code true} if the set contains the key, {@code false} otherwise
	 */
	protected static boolean containsExactly(Set<String> set, String key)
	{
		return set.contains(key);
	}

	/**
	 * Checks if string is the prefix of a string in some set
	 * 
	 * @param set
	 *          The set
	 * @param key
	 *          The string
	 * @return {@code true} if the set has an element with {@code key} as its
	 *         prefix, {@code false} otherwise
	 */
	protected static boolean containsPrefix(Set<String> set, String key)
	{
		for (String s : set)
		{
			if (s.startsWith(key))
			{
				return true;
			}
		}
		return false;
	}

}

