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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.Claim;
import ca.uqac.lif.labpal.Claim.Explanation;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.ProvenanceNode;

/**
 * Callback to display the details of one specific experiment.
 * 
 * @author Sylvain Hallé
 *
 */
public class ClaimCallback extends TemplatePageCallback
{
  /**
   * Creates a new callback for the "Experiments" page.
   * 
   * @param lab
   *          The lab this page belongs to
   * @param assistant
   *          The assistant used to run this lab
   */
  public ClaimCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/claim", lab, assistant);
  }

  @Override
  public String fill(String page, Map<String, String> params, boolean is_offline)
  {
    List<String> path_parts = getParametersFromPath(params);
    int claim_nb = -1;
    String claim_paragraph = "The last time it was checked, this claim was <strong>true</strong> on the contents of the lab.";
    if (!path_parts.isEmpty())
    {
      claim_nb = Integer.parseInt(path_parts.get(0));
    }
    else if (params.containsKey("id"))
    {
      claim_nb = Integer.parseInt(params.get("id"));
    }
    else
    {
      return "";
    }
    Claim e = m_lab.getClaim(claim_nb);
    if (e == null)
    {
      return "";
    }
    if (params.containsKey("compute") || path_parts.contains("compute"))
    {
      m_lab.computeClaim(claim_nb);
    }
    String out = page.replaceAll("\\{%TITLE%\\}", "Claim #" + claim_nb);
    out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.ERLENMEYER));
    out = out.replaceAll("\\{%CLAIM_NB%\\}", Integer.toString(claim_nb));
    out = out.replaceAll("\\{%CLAIM_NAME%\\}", Matcher.quoteReplacement(e.getName()));
    String description = e.getDescription();
    out = out.replaceAll("\\{%CLAIM_DESCRIPTION%\\}",
        Matcher.quoteReplacement("<div class=\"description\">" + description + "</div>"));
    Claim.Result res = m_lab.getClaimResults().get(claim_nb);
    out = out.replaceAll("\\{%CLAIM_ICON%\\}", Matcher.quoteReplacement(getClaimIcon(res)));
    StringBuilder clm_explanations = new StringBuilder();
    Set<Explanation> exps = e.getExplanation();
    if (!exps.isEmpty())
    {
      claim_paragraph = "The last time it was checked, this claim was <strong>false</strong> on the contents of the lab.";
      clm_explanations.append(
          "<h3>Why?</h3>\n\n<p>Below are one or more explanations for the fact that this claim does not hold.</p>\n\n");
      clm_explanations.append("<ul class=\"explanation\">\n");
      for (Explanation exp : exps)
      {
        clm_explanations.append("<li>\n");
        clm_explanations.append(formatExplanation(exp));
        clm_explanations.append("</li>\n");
      }
      clm_explanations.append("</ul>\n");
    }
    out = out.replaceAll("\\{%CLAIM_PARAGRAPH%\\}", Matcher.quoteReplacement(claim_paragraph));
    out = out.replaceAll("\\{%CLAIM_EXPLANATIONS%\\}",
        Matcher.quoteReplacement(clm_explanations.toString()));
    return out;
  }

  /**
   * Formats an explanation for HTML display
   * 
   * @param exp
   *          The explanation
   * @return An HTML formatted string
   */
  protected String formatExplanation(Explanation exp)
  {
    StringBuilder out = new StringBuilder();
    out.append("<div class=\"explanation-text\">").append(exp.getDescription()).append("</div>");
    List<Object> objects = exp.getObjects();
    if (!objects.isEmpty())
    {
      out.append("<ul>\n");
      for (Object o : objects)
      {
        if (o instanceof NodeFunction)
        {
          NodeFunction dv = (NodeFunction) o;
          out.append("<li class=\"explanation-").append(ExplainCallback.getDataPointIconClass(dv))
              .append("\">");
          out.append("<a href=\"").append(ExplainCallback.getDataPointUrl(dv)).append("\">")
              .append(dv.getDataPointId()).append("</a>");
          ;
          out.append("</li>");
        }
        else if (o instanceof ProvenanceNode)
        {
          ProvenanceNode pn = (ProvenanceNode) o;
          out.append("<li class=\"explanation-")
              .append(ExplainCallback.getDataPointIconClass(pn.getNodeFunction())).append("\">");
          out.append("<a href=\"").append(ExplainCallback.getDataPointUrl(pn)).append("\">")
              .append(pn).append("</a>");
          ;
          out.append("</li>");
        }
      }
      out.append("</li>\n");
      out.append("</ul>\n");
    }
    return out.toString();
  }

  /**
   * Gets the status icon for a claim based on its result
   * 
   * @param result
   *          The claim's result
   * @return An HTML string for the corresponding icon
   */
  public static String getClaimIcon(Claim.Result result)
  {
    StringBuilder claim_list = new StringBuilder();
    switch (result)
    {
    case OK:
      claim_list.append("<div class=\"status-icon status-done\"><span>OK</span></div>");
      break;
    case WARNING:
      claim_list.append("<div class=\"status-icon status-exclamation\"><span>WARNING</span></div>");
      break;
    case FAIL:
      claim_list.append("<div class=\"status-icon status-failed\"><span>FAIL</span></div>");
      break;
    default:
      claim_list.append("<div class=\"status-icon status-unknown\"><span>UNKNOWN</span></div>");
      break;
    }
    return claim_list.toString();
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

  @Override
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {
    List<Claim> claims = m_lab.getClaims();
    for (Claim exp_id : claims)
    {
      String file_contents = exportToStaticHtml(exp_id.getId());
      String filename = "claim/" + exp_id + ".html";
      ZipEntry ze = new ZipEntry(filename);
      zos.putNextEntry(ze);
      zos.write(file_contents.getBytes());
      zos.closeEntry();
    }
  }
}
