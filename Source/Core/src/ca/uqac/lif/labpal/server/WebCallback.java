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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.RestCleanCallback;
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
	 * The pattern to create static URLs for experiments
	 */
	protected static final Pattern s_experimentPattern = Pattern.compile("\"experiment/(\\d+).*?\"");
	
	/**
	 * The pattern to create static URLs for plots
	 */
	protected static final Pattern s_plotPattern = Pattern.compile("\"plot/(\\d+)\"");
	
	/**
	 * The pattern to create static URLs for tables
	 */
	protected static final Pattern s_tablePattern = Pattern.compile("\"table/(\\d+).*?\"");
	
	/**
	 * The pattern for HREF links
	 */
	protected static final Pattern s_hrefPattern = Pattern.compile("href=\"/(.*?)\"");
	
	/**
	 * The pattern for SRC links
	 */
	protected static final Pattern s_srcPattern = Pattern.compile("src=\"/(.*?)\"");
	
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
	 * Replaces dynamic links in a page by static links. For example,
	 * the line <code>experiment/2?reset</code> will be replaced by
	 * <code>experiment/2.html</code>.
	 * @param contents The original contents of the page
	 * @return The page with static links
	 */
	protected static String createStaticLinks(String contents)
	{
		Matcher mat;
		mat = s_experimentPattern.matcher(contents);
		contents = mat.replaceAll("\"experiment/$1.html\"");
		mat = s_plotPattern.matcher(contents);
		contents = mat.replaceAll("\"plot/$1.html\"");
		mat = s_tablePattern.matcher(contents);
		contents = mat.replaceAll("\"table/$1.html\"");
		// Top menu
		contents = contents.replaceAll("href=\"/index\"", "href=\"/index.html\"");
		contents = contents.replaceAll("href=\"/experiments\"", "href=\"/experiments.html\"");
		contents = contents.replaceAll("href=\"/tables\"", "href=\"/tables.html\"");
		contents = contents.replaceAll("href=\"/plots\"", "href=\"/plots.html\"");
		contents = contents.replaceAll("href=\"/status\"", "href=\"/status.html\"");
		contents = contents.replaceAll("href=\"/assistant\"", "href=\"/assistant.html\"");
		contents = contents.replaceAll("href=\"/macros\"", "href=\"/macros.html\"");
		contents = contents.replaceAll("href=\"/find\"", "href=\"/find.html\"");
		contents = contents.replaceAll("href=\"/help\"", "href=\"/help.html\"");
		return contents;
	}
	
	/**
	 * Converts absolute URLs into relative URLs
	 * @param contents The original contents of the page
	 * @param path_to_root The relative path to the root from the
	 * page to be converted
	 * @return The page with static links
	 */
	protected static String relativizeUrls(String contents, String path_to_root)
	{
		Matcher mat;
		mat = s_hrefPattern.matcher(contents);
		contents = mat.replaceAll("href=\"" + path_to_root + "$1\"");
		mat = s_srcPattern.matcher(contents);
		contents = mat.replaceAll("src=\"" + path_to_root + "$1\"");
		return contents;
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
	public void bundle(ZipOutputStream zos) throws IOException
	{
		// Do nothing
		return;
	}
}
