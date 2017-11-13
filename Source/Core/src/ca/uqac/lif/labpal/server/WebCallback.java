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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RestCleanCallback;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for an HTTP request in the ParkBench server.
 * 
 * @author Sylvain Hallé
 *
 */
public abstract class WebCallback extends RestCleanCallback
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
	 * Extracts parameters from the slash-separated list of arguments
	 * in an URL path
	 * @param parameters The map of parameters parsed
	 * @return The list of parameters
	 */
	protected static List<String> getParametersFromPath(Map<String,String> parameters)
	{
		if (!parameters.containsKey(""))
		{
			return new ArrayList<String>(0);
		}
		String[] parts = parameters.get("").split("/");
		List<String> list = new ArrayList<String>(parts.length);
		for (String part : parts)
		{
			if (!part.trim().isEmpty())
				list.add(part);
		}
		return list;
	}
	
	/**
	 * Creates an HTTP "bad request" response
	 * @param cbr The callback response to fill with data
	 * @param message The error message to return to the browser
	 */
	protected static void doBadRequest(CallbackResponse cbr, String message)
	{
		cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
		String file_contents = FileHelper.internalFileToString(LabPalServer.class, TemplatePageCallback.s_path + "/error-message.html");
		file_contents = TemplatePageCallback.resolveInclude(file_contents);
		file_contents = file_contents.replaceAll("\\{%TITLE%\\}", "Error uploading file");
		file_contents = file_contents.replaceAll("\\{%MESSAGE%\\}", message);
		file_contents = file_contents.replaceAll("\\{%VERSION_STRING%\\}", Laboratory.s_versionString);
		cbr.setContents(file_contents);
	}
	
	/**
	 * Adds contents to a zip archive of a bundle of all the server's
	 * pages. This is used by the "export to static HTML" feature.
	 * @param zos An output stream to add content to
	 * @throws IOException Thrown if writing to the output stream
	 * cannot be done
	 */
	public void addToZipBundle(ZipOutputStream zos) throws IOException
	{
		// Do nothing
		return;
	}
	
	/**
	 * Produces a status bar indicating the relative completion of the
	 * experiments in this lab.
	 * @return HTML code for the status bar
	 */
	public final String getHeaderBar()
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
				out.delete(0,out.length()+1);
				out.append("<div> Running experiment : #").append(id).append("</div>\n");
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
		
		//StringBuilder out = new StringBuilder();
		float scale = bar_width_px / num_ex;
		int num_remaining = num_ex - num_done - num_q - num_failed;
		
		out.append("<ul id=\"progress-bar\" style=\"float:left;margin-bottom:20px;width:").append(((float) num_ex) * scale).append("px;\">");
		out.append("<li class=\"done\" title=\"Done: ").append(num_done).append("\" style=\"width:").append(((float) num_done) * scale).append("px\"><span class=\"text-only\">Done: ").append(num_done).append("</span></li>");
		out.append("<li class=\"queued\" title=\"Queued: ").append(num_q).append("\" style=\"width:").append(((float) num_q) * scale).append("px\"><span class=\"text-only\">Queued: ").append(num_q).append("</span></li>");
		out.append("<li class=\"warning\" title=\"Warning: ").append(num_warn).append("\" style=\"width:").append(((float) num_warn) * scale).append("px\"><span class=\"text-only\">Warnings: ").append(num_warn).append("</span></li>");
		out.append("<li class=\"failed\" title=\"Failed/cancelled: ").append(num_failed).append("\" style=\"width:").append(((float) num_failed) * scale).append("px\"><span class=\"text-only\">Failed/cancelled: ").append(num_failed).append("</span></li>");
		out.append("<li class=\"other\" title=\"Other: ").append(num_remaining).append("\" style=\"width:").append(((float) num_remaining) * scale).append("px\"><span class=\"text-only\">Other: ").append(num_remaining).append("</span></li>");
		out.append("</ul>");
		out.append("<div>").append(num_done).append("/").append(num_ex).append("</div>");
		out.append("<div style=\"clear:both\"></div>");
		if (num_q !=0)
			{
			   return out.toString();
			}
		else return "";
		
	}
	
}
