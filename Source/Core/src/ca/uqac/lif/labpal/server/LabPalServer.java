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

import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.claim.Claim;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.table.Table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.JarFile;
import ca.uqac.lif.jerrydog.EmptyHttpExchange;
import ca.uqac.lif.jerrydog.InnerFileCallback;
import ca.uqac.lif.jerrydog.RequestCallback.Method;

/**
 * A web server allowing the interaction with the contents of a lab. In addition
 * to the live interaction with a lab that the interface provides, the LabPal
 * server also offers the feature of saving all its interface as a set of
 * static HTML files organized along a folder structure.
 *  
 * @author Sylvain Hallé
 */
public class LabPalServer extends Server
{
	/**
	 * The instance of lab this server is responsible for.
	 */
	/*@ non_null @*/ protected final Laboratory m_lab;

	/**
	 * A number identifying the color scheme used in the GUI
	 */
	protected int m_colorScheme = 0;

	/**
	 * The predefined color schemes used for the GUI
	 */
	/*@ non_null @*/ protected static final List<String[]> s_colorSchemes = loadSchemes();

	/**
	 * Creates a new server instance
	 * @param lab
	 */
	public LabPalServer(Laboratory lab)
	{
		super();
		m_lab = lab;
		setServerPort(21212);
		setUserAgent("LabPal " + Laboratory.formatVersion());
		registerCallback(new TemplatePageCallback(this, Method.GET, "/help", "help.ftlh", "top-menu-help").setTitle("Help"));
		registerCallback(new FindFormCallback(this));
		registerCallback(new StatusPageCallback(this, Method.GET, "/status", "status.ftlh").setTitle("Status"));
		registerCallback(new ExperimentPageCallback(this, Method.GET, "/experiment/", "experiment.ftlh"));
		registerCallback(new ExperimentsStatusCallback(this, Method.GET, "/experiments/status"));
		registerCallback(new ExperimentsPageCallback(this, Method.GET, "/experiments", "experiments.ftlh"));
		registerCallback(new LabStatusCallback(this, Method.GET, "/lab/status"));
		registerCallback(new AssistantStatusCallback(this, Method.GET, "/assistant/status"));
		registerCallback(new AssistantPageCallback(this, Method.GET, "/assistant", "assistant.ftlh"));
		registerCallback(new AllTablesCallback(this));
		registerCallback(new TablesStatusCallback(this, Method.GET, "/tables/status"));
		registerCallback(new TablePageCallback(this, Method.GET, "/table/", "table.ftlh"));
		registerCallback(new TemplatePageCallback(this, Method.GET, "/tables", "tables.ftlh", "top-menu-tables").setTitle("Tables"));
		registerCallback(new AllPlotsCallback(lab));
		registerCallback(new AllPlotsLatexCallback(this));
		registerCallback(new PlotsStatusCallback(this, Method.GET, "/plots/status"));
		registerCallback(new TemplatePageCallback(this, Method.GET, "/plots", "plots.ftlh", "top-menu-plots").setTitle("Plots"));
		registerCallback(new PlotImageCallback(lab));
		registerCallback(new PlotsStatusCallback(this, Method.GET, "/plots/status"));
		registerCallback(new PlotPageCallback(this, Method.GET, "/plot/", "plot.ftlh"));
		registerCallback(new TemplatePageCallback(this, Method.GET, "/index", "index.ftlh", "top-menu-home").setTitle("Home"));
		registerCallback(new ExplainImageCallback(this));
		registerCallback(new ExplainCallback(this, Method.GET, "/explain", "explain.ftlh").setTitle("Explanation"));
		registerCallback(new AllClaimsCallback(this));
		registerCallback(new ClaimsPageCallback(this, Method.GET, "/claims", "claims.ftlh").setTitle("Claims"));
		registerCallback(new ClaimPageCallback(this, Method.GET, "/claim/", "claim.ftlh"));
		registerCallback(new DownloadCallback(this));
		registerCallback(new UploadCallback(this, "status.ftlh"));
		registerCallback(new CssCallback(this, Method.GET, "/screen.css", "screen.css.ftlh"));
		registerCallback(new JavaScriptCallback("resource", LabPalServer.class));
		registerCallback(new InnerFileCallback("resource", LabPalServer.class));
	}

	/**
	 * Gets the laboratory that this server is taked with controlling.
	 * @return The lab instance
	 */
	/*@ pure non_null @*/ public Laboratory getLaboratory()
	{
		return m_lab;
	}

	/**
	 * Sets the color scheme used to display the pages of the web interface.
	 * @param scheme An integer between 0 and 3 corresponding to the color scheme
	 * to be used
	 * @return This server
	 */
	/*@ non_null @*/ public LabPalServer setColorScheme(int scheme)
	{
		m_colorScheme = scheme;
		return this;
	}

	/**
	 * Loads the set of color schemes from an internal file
	 * 
	 * @return A list of arrays with hex colors
	 */
	protected static List<String[]> loadSchemes()
	{
		Scanner scanner = new Scanner(LabPalServer.class.getResourceAsStream("schemes.csv"));
		List<String[]> lines = new ArrayList<String[]>();
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.isEmpty() || !line.startsWith("#"))
			{
				continue;
			}
			String[] parts = line.split(",");
			lines.add(parts);
		}
		scanner.close();
		return lines;
	}

	/**
	 * Gets the array of hex colors corresponding to the current color scheme
	 * 
	 * @return The array
	 */
	public String[] getColorScheme()
	{
		return s_colorSchemes.get(m_colorScheme);
	}
	
	public void saveToEpub(FileSystem fs) throws FileSystemException
	{
		JarFile inner = new JarFile(LabPalServer.class);
		inner.open();
		inner.chdir("epub");
		FileUtils.copy(inner, fs);
		String contents_ops = new String(FileUtils.toBytes(inner.readFrom("OEBPS/content.opf")));
		FileUtils.copy(FileUtils.toStream(populateContents(contents_ops)), fs.writeTo("OEBPS/content.opf"));
		inner.close();
		fs.pushd("OEBPS");
		saveToHtml(fs);
		fs.popd();
	}
	
	protected String populateContents(String contents_opf) throws FileSystemException
	{
		StringBuilder out = new StringBuilder();
		JarFile inner = new JarFile(LabPalServer.class);
		inner.open();
		for (String imgname : inner.ls("resource/images"))
		{
			out.append("<item id=\"" + imgname + "\" href=\"images/" + imgname + "\" media-type=\"image/png\" />\n");
		}
		inner.close();
		for (Experiment e : m_lab.getExperiments())
		{
			out.append("<item id=\"experiment-" + e.getId() + "\" href=\"experiment_" + e.getId() + ".html\" media-type=\"application/xhtml+xml\" />\n");
		}
		for (Table e : m_lab.getTables())
		{
			out.append("<item id=\"table-" + e.getId() + "\" href=\"table/" + e.getId() + ".html\" media-type=\"application/xhtml+xml\" />\n");
		}
		for (Plot e : m_lab.getPlots())
		{
			out.append("<item id=\"plot-" + e.getId() + "\" href=\"plot/" + e.getId() + ".html\" media-type=\"application/xhtml+xml\" />\n");
			out.append("<item id=\"plot" + e.getId() + ".png\" href=\"plot/" + e.getId() + ".png\" media-type=\"image/png\" />\n");
		}
		return contents_opf.replaceAll("%OTHER_FILES%", out.toString());
	}

	/**
	 * Saves the content of a lab as a set of files in a file system.
	 * @param fs The file system where to save the lab's contents
	 */
	public void saveToHtml(FileSystem fs) throws FileSystemException
	{
		try
		{
			{
				// Copy resource files
				FileSystem inner = new JarFile(LabPalServer.class);
				inner.open();
				inner.chdir("resource");
				FileUtils.copy(inner, fs);
				inner.close();
			}
			{
				// Copy main pages
				savePage("/screen.css", fs.writeTo("screen.css"), "");
				savePage("/index", fs.writeTo("index.html"), "");
				savePage("/status", fs.writeTo("status.html"), "");
				savePage("/assistant", fs.writeTo("assistant.html"), "");
				savePage("/experiments", fs.writeTo("experiments.html"), "");
				savePage("/tables", fs.writeTo("tables.html"), "");
				savePage("/plots", fs.writeTo("plots.html"), "");
				savePage("/macros", fs.writeTo("macros.html"), "");
				savePage("/claims", fs.writeTo("claims.html"), "");
				savePage("/help", fs.writeTo("help.html"), "");
			}
			{
				// Copy experiment pages
				//fs.mkdir("experiment");
				//fs.pushd("experiment");
				for (Experiment e : m_lab.getExperiments())
				{
					savePage("/experiment/" + e.getId(), fs.writeTo("experiment_" + e.getId() + ".html"), "../");
				}
				//fs.popd();
			}
			{
				// Copy table pages
				fs.mkdir("table");
				fs.pushd("table");
				for (Table e : m_lab.getTables())
				{
					savePage("/table/" + e.getId(), fs.writeTo(e.getId() + ".html"), "../");
				}
				fs.popd();
			}
			{
				// Copy plot pages
				fs.mkdir("plot");
				fs.pushd("plot");
				for (Plot e : m_lab.getPlots())
				{
					savePage("/plot/" + e.getId(), fs.writeTo(e.getId() + ".html"), "../");
					saveFile("/plot/" + e.getId() + "/png", fs.writeTo(e.getId() + ".png"));
				}
				fs.popd();
			}
			{
				// Copy claims pages
				fs.mkdir("claim");
				fs.pushd("claim");
				for (Claim e : m_lab.getClaims())
				{
					savePage("/claim/" + e.getId(), fs.writeTo(e.getId() + ".html"), "../");
				}
				fs.popd();
			}
		}
		catch (IOException e1)
		{
			throw new FileSystemException(e1);
		}
	}

	/**
	 * Saves a page of the web interface as a static HTML file. After fetching
	 * the file, the method converts all paths to known resources on the server
	 * to their corresponding path and filename in the static version of the
	 * interface.
	 * @param url The URL of the page on the server
	 * @param os An output stream where the static HTML file is to be written
	 * @param rel_path The path of the server's root relative to the page
	 * @throws IOException Thrown if the HTTP request could not be handled
	 * @throws FileSystemException Thrown if the file could not be written
	 */
	protected void savePage(String url, OutputStream os, String rel_path) throws IOException, FileSystemException
	{
		OfflineHttpExchange t = new OfflineHttpExchange(url);
		handle(t);
		String fixed_page = relativizePaths(new String(t.getResponse()), rel_path);
		FileUtils.copy(FileUtils.toStream(fixed_page.getBytes()), os);
		os.close();
	}
	
	protected void saveFile(String url, OutputStream os) throws IOException, FileSystemException
	{
		OfflineHttpExchange t = new OfflineHttpExchange(url);
		handle(t);
		FileUtils.copy(FileUtils.toStream(t.getResponse()), os);
		os.close();
	}
	
	/**
	 * Converts paths of all known resources to a relative path and corresponding
	 * filename. For example, from the <tt>/claims</tt> page, the URL
	 * <tt>/experiment/1</tt>, pointing to experiment #1, becomes
	 * <tt>../experiment/1.html</tt>, which is the appropriate filename and
	 * relative location of that page in the static version of the interface.
	 * @param page To page to convert paths
	 * @return The converted page
	 */
	protected String relativizePaths(String page, String path)
	{
		// Delete meta tags that make no sense for a static page
		page = page.replaceAll("<meta http-equiv=\"Cache-Control\".*?>", "");
		page = page.replaceAll("<meta http-equiv=\"Pragma\".*?>", "");
		page = page.replaceAll("<meta http-equiv=\"Expires\".*?>", "");
		
		// Correct paths
		page = page.replaceAll("href=\"/index", "href=\"" + path + "index.html");
		page = page.replaceAll("href=\"/status", "href=\"" + path + "status.html");
		page = page.replaceAll("href=\"/assistant", "href=\"" + path + "assistant.html");
		page = page.replaceAll("href=\"/experiments", "href=\"" + path + "experiments.html");
		page = page.replaceAll("href=\"/tables", "href=\"" + path + "tables.html");
		page = page.replaceAll("href=\"/plots", "href=\"" + path + "plots.html");
		page = page.replaceAll("href=\"/macros", "href=\"" + path + "macros.html");
		page = page.replaceAll("href=\"/claims", "href=\"" + path + "claims.html");
		page = page.replaceAll("href=\"/find", "href=\"" + path + "find.html");
		page = page.replaceAll("href=\"/help", "href=\"" + path + "help.html");
		//page = page.replaceAll("href=\"/experiment/(\\d+)", "href=\"" + path + "experiment/$1.html");
		page = page.replaceAll("href=\"/experiment/(\\d+)", "href=\"" + path + "experiment_$1.html");
		page = page.replaceAll("href=\"/table/(\\d+)", "href=\"" + path + "table/$1.html");
		page = page.replaceAll("href=\"/macro/(\\d+)", "href=\"" + path + "macro/$1.html");
		page = page.replaceAll("href=\"/plot/(\\d+)/png", "href=\"" + path + "plot/$1.png");
		page = page.replaceAll("src=\"/plot/(\\d+)/png", "src=\"" + path + "plot/$1.png");
		page = page.replaceAll("href=\"/plot/(\\d+)", "href=\"" + path + "plot/$1.html");
		page = page.replaceAll("href=\"/", "href=\"" + path);
		page = page.replaceAll("url\\('/", "url('" + path);
		return page;
	}

	protected static class OfflineHttpExchange extends EmptyHttpExchange
	{
		/**
		 * The body of the request; only used with POST.
		 */
		/*@ non_null @*/ protected byte[] m_requestBody;
		
		/**
		 * The request method.
		 */
		/*@ non_null @*/ protected String m_requestMethod;
		
		public OfflineHttpExchange(String url)
		{
			super(url);
			m_requestBody = new byte[0];
			m_requestMethod = "GET";
		}

		public byte[] getResponse()
		{
			return m_responseBody.toByteArray();
		}
		
		public void setRequestBody(/*@ non_null @*/ byte[] contents)
		{
			m_requestBody = contents;
		}
		
		@Override
		public InputStream getRequestBody()
		{
			return new ByteArrayInputStream(m_requestBody);
		}
		
		@Override
		public String getRequestMethod()
		{
			return m_requestMethod;
		}
	}
}
