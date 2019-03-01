/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

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
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.Claim;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.ResultReporter;
import ca.uqac.lif.labpal.ResultReporter.ReporterException;

/**
 * Callback for the home page, showing various statistics and basic data about
 * the current lab.
 * 
 * @author Sylvain Hallé
 *
 */
public class StatusPageCallback extends TemplatePageCallback
{
  /**
   * If the lab's environment requirements are not met, the error message is
   * stored here. Since environment checks can be long, this check is done only
   * once, and the result is cached for future calls to this class.
   */
  protected final transient String m_environmentMessage;

  /**
   * The description associated to the lab
   */
  protected final transient String m_labDescription;

  public StatusPageCallback(Laboratory lab, LabAssistant assistant)
  {
    super("/status", lab, assistant);
    m_environmentMessage = lab.isEnvironmentOk();
    m_labDescription = lab.getDescription();
  }

  @Override
  public String fill(String page, Map<String, String> params, boolean is_offline)
  {
    String out = page.replaceAll("\\{%TITLE%\\}", m_lab.getTitle());
    // out = out.replaceAll("\\{%LAB_NAME%\\}", m_lab.getTitle());
    out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.STATUS));
    out = out.replaceAll("\\{%LAB_DESCRIPTION%\\}", m_labDescription);
    out = out.replaceAll("\\{%LAB_ASSISTANT%\\}", m_assistant.getName());
    out = out.replaceAll("\\{%LAB_AUTHOR%\\}", m_lab.getAuthorName());
    out = out.replaceAll("\\{%LAB_SEED%\\}", Integer.toString(m_lab.getRandomSeed()));
    out = out.replaceAll("\\{%HOSTNAME%\\}", m_lab.getHostName());
    out = out.replaceAll("\\{%SPEED_FACTOR%\\}", String.format("%.2f", Laboratory.s_parkMips));
    out = out.replaceAll("\\{%DATA_POINTS%\\}", Integer.toString(m_lab.countDataPoints()));
    out = out.replaceAll("\\{%SEL_HOME%\\}", "selected");
    out = out.replaceAll("\\{%OS_NAME%\\}", System.getProperty("os.name"));
    out = out.replaceAll("\\{%OS_ARCH%\\}", System.getProperty("os.arch"));
    out = out.replaceAll("\\{%OS_VERSION%\\}", System.getProperty("os.version"));
    String doi = m_lab.getDoi();
    if (!doi.isEmpty())
    {
      out = out.replaceAll("\\{%DOI%\\}",
          "<tr><th title=\"The Digital Object Identifier assigned to this lab\">DOI</th><td>"
              + Matcher.quoteReplacement(htmlEscape(doi)) + "</td></tr>\n");
    }
    out = out.replaceAll("\\{%PROGRESS_BAR%\\}", getBar());
    if (m_environmentMessage != null)
    {
      out = out.replaceAll("\\{%ENVIRONMENT_MESSAGE%\\}",
          "<p class=\"message error\">" + "<span>The lab's environment requirements are not met. "
              + m_environmentMessage
              + " This means you may not be able to run the experiments propertly.</span></p>");
    }
    String serialization_message = getSerializationMessage();
    if (serialization_message != null)
    {
      out = out.replaceAll("\\{%SERIALIZATION_MESSAGE%\\}",
          "<p class=\"message info\">" + "<span>" + serialization_message + "</span></p>");
    }
    out = out.replaceAll("\\{%REPORTING_DIV%\\}", getReportingDiv());
    out = out.replaceAll("\\{%CLAIM_DIV%\\}", getClaimDiv());
    return out;
  }

  /**
   * Produces the part of the page that shows the reporting status of the lab
   * 
   * @return The HTML code for this part of the page
   */
  protected String getReportingDiv()
  {
    ResultReporter rep = m_lab.getReporter();
    StringBuilder rep_out = new StringBuilder();
    if (rep != null && rep.getUrl() != null && !rep.getUrl().isEmpty())
    {
      rep_out.append("<div>\n");
      rep_out.append("<h2>Reporting results</h2>\n");
      rep_out
          .append(
              "<p>This lab is instructed to periodically report its results to <a href=\"http://")
          .append(rep.getUrl()).append("/index\">").append(rep.getUrl()).append("</a></p>\n");
      rep_out.append(
          "<a id=\"btn-report-results\" class=\"btn-24\" href=\"/report-results\"><span>Send an update now</span></a>\n");
      List<ReporterException> exceptions = rep.getExceptions();
      if (!exceptions.isEmpty())
      {

        rep_out
            .append("<p class=\"message info\"><span>The lab has problems reporting its results:");
        rep_out.append("<ul>\n");
        for (ReporterException re : exceptions)
        {
          rep_out.append("<li>").append(re.getMessage()).append("</li>\n");
        }
        rep_out.append("</ul></p>\n");
      }
      rep_out.append("</div>\n");
    }
    return rep_out.toString();
  }

  /**
   * Produces the part of the page that shows the status of each claim
   * 
   * @return The HTML code for this part of the page
   */
  protected String getClaimDiv()
  {
    StringBuilder out = new StringBuilder();
    out.append("<div>\n");
    Set<Map.Entry<Integer, Claim.Result>> entries = m_lab.getClaimResults().entrySet();
    if (entries.isEmpty())
    {
      out.append("<p>There are no claims made for this lab.</p>\n");
    }
    else
    {
      out.append(
          "<a class=\"btn-24 btn-compute-claims\" href=\"/claims/compute\"><span>Re-evaluate</span></a>\n");
      StringBuilder claim_list = new StringBuilder();
      claim_list.append("<table class=\"claim-table\">\n");
      for (Map.Entry<Integer, Claim.Result> e : entries)
      {
        int c_id = e.getKey();
        Claim c = m_lab.getClaim(c_id);
        claim_list.append("<tr>");
        Claim.Result result = e.getValue();
        claim_list.append("<td>");
        claim_list.append(ClaimCallback.getClaimIcon(result));
        claim_list.append("</td>");
        claim_list.append("<th><a href=\"claim/").append(c_id).append("\">").append(c_id)
            .append("</a></th>");
        claim_list.append("<td><a href=\"claim/").append(c_id).append("\">").append(c.getName())
            .append("</a></td>");
        claim_list.append("</tr>\n");
      }
      claim_list.append("</table>\n");
      out.append(claim_list);
    }
    out.append("</div>\n");
    return out.toString();
  }

  /**
   * Produces a status bar indicating the relative completion of the experiments
   * in this lab.
   * 
   * @return HTML code for the status bar
   */
  public String getBar()
  {
    // Width of the bar, in pixels
    final float bar_width_px = 400;
    int num_ex = 0, num_q = 0, num_failed = 0, num_done = 0, num_warn = 0;
    StringBuilder out = new StringBuilder();
    for (int id : m_lab.getExperimentIds())
    {
      num_ex++;
      Experiment ex = m_lab.getExperiment(id);
      switch (ex.getStatus())
      {
      case RUNNING:
        out.delete(0, out.length() + 1);
        out.append("<div> Running experiment : #").append(ex.getId()).append("</div>\n");
        break;
      case DONE:
        num_done++;
        break;
      case FAILED:
        num_failed++;
        break;
      case DONE_WARNING:
        num_warn++;
        break;
      default:
        if (m_assistant.isQueued(id))
        {
          num_q++;
        }
        break;
      }
    }

    // StringBuilder out = new StringBuilder();
    float scale = bar_width_px / num_ex;
    int num_remaining = num_ex - num_done - num_q - num_failed;

    out.append("<ul id=\"progress-bar\" style=\"float:left;margin-bottom:20px;width:")
        .append(((float) num_ex) * scale).append("px;\">");
    out.append("<li class=\"done\" title=\"Done: ").append(num_done).append("\" style=\"width:")
        .append(((float) num_done) * scale).append("px\"><span class=\"text-only\">Done: ")
        .append(num_done).append("</span></li>");
    out.append("<li class=\"queued\" title=\"Queued: ").append(num_q).append("\" style=\"width:")
        .append(((float) num_q) * scale).append("px\"><span class=\"text-only\">Queued: ")
        .append(num_q).append("</span></li>");
    out.append("<li class=\"warning\" title=\"Warning: ").append(num_warn)
        .append("\" style=\"width:").append(((float) num_warn) * scale)
        .append("px\"><span class=\"text-only\">Warnings: ").append(num_warn)
        .append("</span></li>");
    out.append("<li class=\"failed\" title=\"Failed/cancelled: ").append(num_failed)
        .append("\" style=\"width:").append(((float) num_failed) * scale)
        .append("px\"><span class=\"text-only\">Failed/cancelled: ").append(num_failed)
        .append("</span></li>");
    out.append("<li class=\"other\" title=\"Other: ").append(num_remaining)
        .append("\" style=\"width:").append(((float) num_remaining) * scale)
        .append("px\"><span class=\"text-only\">Other: ").append(num_remaining)
        .append("</span></li>");
    out.append("</ul>");
    out.append("<div>").append(num_done).append("/").append(num_ex).append("</div>");
    out.append("<div style=\"clear:both\"></div>");
    return out.toString();
  }

  /**
   * Produces an error message if the lab contains a class without a no-args
   * constructor
   * 
   * @return An error message explaining the fact, or {@code null} if everything
   *         is OK
   */
  public String getSerializationMessage()
  {
    List<Class<?>> warning_classes = new LinkedList<Class<?>>();
    for (Class<?> c : m_lab.getSerializableClasses())
    {
      if (!hasEmptyConstructor(c))
      {
        warning_classes.add(c);
      }
    }
    if (warning_classes.isEmpty())
    {
      return null;
    }
    StringBuilder out = new StringBuilder();
    if (warning_classes.size() == 1)
    {
      out.append("The class ").append(warning_classes.get(0).getSimpleName())
          .append(", which was defined by the lab's author, does not have an empty constructor. ");
    }
    else
    {
      out.append(
          "The following classes, which were defined by the lab's author, do not have an empty constructor: ");
      boolean first = true;
      for (Class<?> c : warning_classes)
      {
        if (first)
        {
          first = false;
        }
        else
        {
          out.append(", ");
        }
        out.append(c.getSimpleName());
      }
      out.append(". ");
    }
    out.append(
        "It is possible to save the lab's state to a file, but loading will produce an error message and will not be possible.");
    return out.toString();
  }

  /**
   * Checks if a class has a constructor with no arguments
   * 
   * @param clazz
   *          The class
   * @return {@code true} if the class has a no-args constructor, {@code false}
   *         otherwise
   */
  protected static boolean hasEmptyConstructor(Class<?> clazz)
  {
    for (Constructor<?> constructor : clazz.getDeclaredConstructors())
    {
      if (constructor.getParameterTypes().length == 0)
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addToZipBundle(ZipOutputStream zos) throws IOException
  {
    ZipEntry ze = new ZipEntry("status.html");
    zos.putNextEntry(ze);
    zos.write(exportToStaticHtml("").getBytes());
    zos.closeEntry();
  }

}
