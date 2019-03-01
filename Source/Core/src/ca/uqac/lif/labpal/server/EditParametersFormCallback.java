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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Group;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.LabPalTui;
import ca.uqac.lif.petitpoucet.NodeFunction;

/**
 * Callback showing a form to edit the parameters of one specific experiment.
 * 
 * @author Sylvain Hallé
 *
 */
public class EditParametersFormCallback extends TemplatePageCallback
{
  public EditParametersFormCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/experiment/edit", lab, assistant);
    // Override default filename
    m_filename = s_path + "/edit-parameters.html";
  }

  @Override
  public String fill(String page, Map<String, String> params, boolean is_offline)
  {
    List<String> path_parts = getParametersFromPath(params);
    int experiment_nb = -1;
    if (!path_parts.isEmpty())
    {
      experiment_nb = Integer.parseInt(path_parts.get(0));
    }
    else if (params.containsKey("id"))
    {
      experiment_nb = Integer.parseInt(params.get("id"));
    }
    else
    {
      return "";
    }
    Experiment e = m_lab.getExperiment(experiment_nb);
    if (e == null)
    {
      return "";
    }
    Set<String> to_highlight = new HashSet<String>();
    if (params.containsKey("highlight"))
    {
      to_highlight = getKeysToHighlight(params.get("highlight"));
    }
    String out = page.replaceAll("\\{%TITLE%\\}", "Experiment #" + experiment_nb);
    out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.ERLENMEYER));
    out = out.replaceAll("\\{%EXP_NB%\\}", Integer.toString(experiment_nb));
    out = out.replaceAll("\\{%EXP_STATUS%\\}",
        ExperimentsPageCallback.getStatusLabel(e, m_assistant));
    out = out.replaceAll("\\{%EXP_ESTIMATE%\\}",
        LabPalTui.formatEta(e.getDurationEstimate(Laboratory.s_parkMips)));
    if (e.getEndTime() > 0)
    {
      out = out.replaceAll("\\{%EXP_DURATION%\\}",
          LabPalTui.formatEta((e.getEndTime() - e.getStartTime()) / 1000f));
    }
    out = out.replaceAll("\\{%EXP_BY%\\}", Matcher.quoteReplacement(htmlEscape(e.getWhoRan())));
    out = out.replaceAll("\\{%EXP_DATA%\\}", Matcher
        .quoteReplacement(renderHtml(e.getInputParameters(), "", e, to_highlight).toString()));
    String description = e.getDescription();
    out = out.replaceAll("\\{%EXP_DESCRIPTION%\\}",
        Matcher.quoteReplacement("<div class=\"description\">" + description + "</div>"));
    if (e.isEditable())
    {
      out = out.replaceAll("\\{%EXP_EDIT_BUTTON%\\}",
          Matcher.quoteReplacement("<a class=\"btn-24 btn-edit\" href=\"experiment/edit/"
              + e.getId()
              + " title=\"Modify the input parameters of this experiment\">Edit parameters</a>"));
    }
    Set<Group> groups = m_lab.getGroups(experiment_nb);
    String group_description = "";
    for (Group g : groups)
    {
      group_description += g.getDescription();
    }
    if (!group_description.trim().isEmpty())
    {
      out = out.replaceAll("\\{%GROUP_DESC%\\}", Matcher.quoteReplacement(
          "<div class=\"around-pulldown\">\n<h3 class=\"pulldown\">Generic description</h3>\n<div class=\"pulldown-contents\">"
              + group_description + "</div></div>"));
    }
    return out;
  }

  /**
   * Creates HTML code displaying (recursively) the experiment's input parameters
   * as editable fields
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
  public static StringBuilder renderHtml(JsonElement e, String path, Experiment exp,
      Set<String> to_highlight)
  {
    StringBuilder out = new StringBuilder();
    boolean is_editable_field = exp.isEditable(path);
    if (e instanceof JsonString)
    {
      String s = ((JsonString) e).stringValue();
      if (is_editable_field)
      {
        out.append("<input type=\"text\" name=\"fld-").append(path).append("\" value=\"").append(s)
            .append("\" />");
      }
      else
      {
        out.append("<input type=\"hidden\" name=\"fld-").append(path).append("\" value=\"")
            .append(s).append("\" />");
        out.append(s);
      }
    }
    else if (e instanceof JsonNumber)
    {
      Number num = ((JsonNumber) e).numberValue();
      if (is_editable_field)
      {
        out.append("<input type=\"text\" name=\"fld-").append(path).append("\" value=\"")
            .append(num).append("\" />");
      }
      else
      {
        out.append("<input type=\"hidden\" name=\"fld-").append(path).append("\" value=\"")
            .append(num).append("\" />");
        out.append(num);
      }
    }
    else if (e instanceof JsonList)
    {
      out.append("<table class=\"json-table\">\n");
      int el_cnt = 0;
      for (JsonElement v : (JsonList) e)
      {
        String path_append = path + "[" + el_cnt + "]";
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
          out.append("<th class=\"" + css_class_key + "\">").append(htmlEscape(k)).append("</th>");
        }
        else
        {
          out.append("<th class=\"with-desc").append(css_class_key).append("\" title=\"")
              .append(htmlEscape(p_desc)).append("\">").append(htmlEscape(k)).append("</th>");
        }
        out.append("<td " + css_class_value + ">");
        JsonElement v = m.get(k);
        out.append(renderHtml(v, path_append, exp, to_highlight));
        out.append("</td></tr>\n");
      }
      out.append("</table>\n");
    }
    return out;
  }

  /**
   * Gets the set of keys that should be highlighted in the table of experiment
   * results
   * 
   * @param highlight
   *          The key to highlight
   * @return The set of keys to highlight
   */
  protected Set<String> getKeysToHighlight(String highlight)
  {
    Set<String> to_highlight = new HashSet<String>();
    String[] ids = highlight.split(",");
    for (String id : ids)
    {
      String[] parts = id.split(Pattern.quote(NodeFunction.s_separator));
      to_highlight.add(parts[1]);
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
   * @return {@code true} if the set contains the string, {@code false} otherwise
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
   * @return {@code true} if the set contains a string that has {@code key} as its
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

  /**
   * Exports the page for an experiment
   * 
   * @param experiment_id
   *          The ID of the experiement whose page is to be rendered
   * @return The HTML contents of the page
   */
  public String exportToStaticHtml(int experiment_id)
  {
    String file = readTemplateFile();
    Map<String, String> params = new HashMap<String, String>();
    params.put("id", Integer.toString(experiment_id));
    String contents = render(file, params, true);
    contents = createStaticLinks(contents);
    contents = relativizeUrls(contents, "../");
    return contents;
  }
}
