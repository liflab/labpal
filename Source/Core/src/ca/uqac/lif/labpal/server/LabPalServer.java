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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CachedRequestCallback;
import ca.uqac.lif.jerrydog.InnerFileServer;
import ca.uqac.lif.jerrydog.RequestCallback;
import ca.uqac.lif.labpal.CliParser;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;

/**
 * Server supporting LabPal's web GUI
 *  
 * @author Sylvain Hallé
 *
 */
public class LabPalServer extends InnerFileServer
{
	/**
	 * The default port
	 */
	public static final transient int s_defaultPort = 21212;
	
	/**
	 * The time (in seconds) during which the client is allowed to cache
	 * static resources (such as images or JS files) locally
	 */
	protected static final transient int s_cacheInterval = 600;
	
	/**
	 * A number identifying the color scheme used in the GUI
	 */
	protected int m_colorScheme = 0;
	
	/**
	 * The predefined color schemes used for the GUI
	 */
	protected static final List<String[]> s_colorSchemes = loadSchemes();
	
	/**
	 * The instance of lab this server is responsible for
	 */
	protected transient Laboratory m_lab;
	
	/**
	 * The callback for the individual experiment page
	 */
	protected ExperimentPageCallback m_experimentPageCallback;

	/**
	 * The callback for the macros page
	 */
	private MacrosPageCallback m_macrosPageCallback;

	/**
	 * The callback for the experiments page
	 */
	private ExperimentsPageCallback m_experimentsPageCallback;
	
	/**
	 * The callback for the plots page
	 */
	private PlotsPageCallback m_plotsPageCallback;

	/**
	 * The callback for the tables page
	 */
	private TablesPageCallback m_tablesPageCallback;
	
	/**
	 * The callback for the table page
	 */
	private TablePageCallback m_tablePageCallback;
	
	/**
	 * The callback for the index page
	 */
	private HomePageCallback m_indexPageCallback;
	
	/**
	 * The callback for the status page
	 */
	private StatusPageCallback m_statusPageCallback;
	
	/**
	 * The callback for the help page
	 */
	private HelpPageCallback m_helpPageCallback;

	/**
	 * The callback for the dynamic CSS file
	 */
	private CssCallback m_cssCallback;
	
	/**
	 * The callback for an individual plot image
	 */
	private PlotImageCallback m_plotImageCallback;

	/**
	 * The callback for the LaTeX macros
	 */
	private AllMacrosLatexCallback m_allMacrosLatexCallback;
	
	/**
	 * Creates a new LabPal server
	 * @param args
	 * @param lab
	 * @param assistant
	 */
	public LabPalServer(ArgumentMap args, Laboratory lab, LabAssistant assistant)
	{
		super(LabPalServer.class, true, s_cacheInterval);
		m_lab = lab;
		setUserAgent("LabPal " + Laboratory.s_versionString);
		if (args.hasOption("port"))
		{
			setServerPort(Integer.parseInt(args.getOptionValue("port")));
		}
		else
		{
			setServerPort(s_defaultPort);
		}
		m_indexPageCallback = new HomePageCallback(lab, assistant);
		registerCallback(0, m_indexPageCallback);
		m_cssCallback = new CssCallback(this, lab, assistant);
		CachedRequestCallback css_callback = new CachedRequestCallback(m_cssCallback);
		css_callback.setCachingEnabled(true);
		css_callback.setCachingInterval(s_cacheInterval);
		registerCallback(0, css_callback);
		registerCallback(0, new MergeCallback(lab, assistant));
		registerCallback(0, new ReportResultsCallback(lab, assistant));
		m_statusPageCallback = new StatusPageCallback(lab, assistant);
		registerCallback(0, m_statusPageCallback);
		registerCallback(0, new EditParametersCallback(lab, assistant));
		m_experimentPageCallback = new ExperimentPageCallback(lab, assistant);
		registerCallback(0, m_experimentPageCallback);
		m_experimentsPageCallback = new ExperimentsPageCallback(lab, assistant);
		registerCallback(0, m_experimentsPageCallback);
		registerCallback(0, new AssistantPageCallback(lab, assistant));
		m_plotsPageCallback = new PlotsPageCallback(lab, assistant);
		registerCallback(0, m_plotsPageCallback);
		m_plotImageCallback = new PlotImageCallback(lab, assistant);
		registerCallback(0, m_plotImageCallback);
		registerCallback(0, new DownloadCallback(lab, assistant));
		registerCallback(0, new UploadCallback(this, lab, assistant));
		m_helpPageCallback = new HelpPageCallback(lab, assistant);
		registerCallback(0, m_helpPageCallback);
		registerCallback(0, new AllPlotsCallback(lab, assistant));
		registerCallback(0, new AllPlotsLatexCallback(lab, assistant));
		m_tablesPageCallback = new TablesPageCallback(lab, assistant);
		registerCallback(0, m_tablesPageCallback);
		m_tablePageCallback = new TablePageCallback(lab, assistant);
		registerCallback(0, m_tablePageCallback);
		registerCallback(0, new TableExportCallback(lab, assistant));
		registerCallback(0, new AllTablesCallback(lab, assistant));
		registerCallback(0, new ExplainCallback(lab, assistant));
		registerCallback(0, new ExplainImageCallback(lab, assistant));
		registerCallback(0, new FindFormCallback(lab, assistant));
		m_macrosPageCallback = new MacrosPageCallback(lab, assistant);
		registerCallback(0, m_macrosPageCallback);
		m_allMacrosLatexCallback = new AllMacrosLatexCallback(lab, assistant);
		registerCallback(0, m_allMacrosLatexCallback);
		registerCallback(0, new ExportStaticCallback(lab, assistant, this));
		registerCallback(0, new EditParametersFormCallback(lab, assistant));
	}
	
	/**
	 * Sets the the color scheme used in the GUI
	 * @param c A number identifying the color scheme
	 */
	public void setColorScheme(int c)
	{
		m_colorScheme = c % s_colorSchemes.size();
	}
	
	/**
	 * Changes the laboratory associated with each registered callback.
	 * This occurs when the user loads a new lab from a file.
	 * @param lab The new laboratory
	 */
	public void changeLab(Laboratory lab)
	{
		m_lab = lab;
		for (RequestCallback cb : m_callbacks)
		{
			if (cb instanceof WebCallback)
			{
				((WebCallback) cb).changeLab(lab);
			}
		}
	}
	
	/**
	 * Loads the set of color schemes from an internal file
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
				continue;
			String[] parts = line.split(",");
			lines.add(parts);
		}
		scanner.close();
		return lines;
	}
	
	/**
	 * Gets the array of hex colors corresponding to the current color
	 * scheme
	 * @return The array
	 */
	public String[] getColorScheme()
	{
		return s_colorSchemes.get(m_colorScheme);
	}
	
	/**
	 * Adds options to the command line parser. Currently this method
	 * does nothing when called.
	 * @param parser The parser
	 */
	public static void setupCli(CliParser parser)
	{
		// Currently the server receives nothing from the CLI
	}
	
	/**
	 * Exports the whole contents of the lab as a zipped bundle
	 * of static web pages.
	 * @return An array of bytes, with the contents of the zip file
	 * generated
	 */
	public byte[] exportToStaticHtml()
	{
		String file_contents, filename;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(bos);
		try
		{
			{
				// All the non-HTML pages in the root
				String root_path = "ca/uqac/lif/labpal/server/";
				String folder = "resource/";
				List<String> internal_files = FileHelper.getResourceListing(LabPalServer.class, root_path + folder, ".*", ".*html|.*~|images|screen\\.css$");
				for (String in_filename : internal_files)
				{
					byte[] contents = FileHelper.internalFileToBytes(LabPalServer.class, folder + in_filename);
					ZipEntry ze = new ZipEntry(in_filename);
					zos.putNextEntry(ze);
					zos.write(contents);
					zos.closeEntry();
				}
			}
			{
				// The screen.css file requires special attention
				file_contents = m_cssCallback.exportToStaticHtml("");
				filename = "screen.css";
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// Everything in images
				String root_path = "ca/uqac/lif/labpal/server/";
				String folder = "resource/images/";
				List<String> internal_files = FileHelper.getResourceListing(LabPalServer.class, root_path + folder, ".*", ".*html|.*~$");
				for (String in_filename : internal_files)
				{
					byte[] contents = FileHelper.internalFileToBytes(LabPalServer.class, folder + in_filename);
					ZipEntry ze = new ZipEntry("images/" + in_filename);
					zos.putNextEntry(ze);
					zos.write(contents);
					zos.closeEntry();
				}
			}
			{
				// Index page
				file_contents = m_indexPageCallback.exportToStaticHtml("");
				filename = "index.html";
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// Status page
				file_contents = m_statusPageCallback.exportToStaticHtml("");
				filename = "status.html";
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// Help page
				file_contents = m_helpPageCallback.exportToStaticHtml("");
				filename = "help.html";
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// Experiments page
				file_contents = m_experimentsPageCallback.exportToStaticHtml("");
				filename = "experiments.html";
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// Tables page
				file_contents = m_tablesPageCallback.exportToStaticHtml("");
				filename = "tables.html";
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// Plots page
				file_contents = m_plotsPageCallback.exportToStaticHtml("");
				filename = "plots.html";
				ZipEntry 	ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// Macros page
				file_contents = m_macrosPageCallback.exportToStaticHtml("");
				filename = "macros.html";
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// LaTeX macros page
				file_contents = m_allMacrosLatexCallback.exportToStaticHtml("");
				filename = "labpal-macros.tex";
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);
				zos.write(file_contents.getBytes());
				zos.closeEntry();
			}
			{
				// Experiments
				Set<Integer> exp_ids = m_lab.getExperimentIds();
				for (int exp_id : exp_ids)
				{
					file_contents = m_experimentPageCallback.exportToStaticHtml(exp_id);
					filename = "experiment/" + exp_id + ".html";
					ZipEntry ze = new ZipEntry(filename);
					zos.putNextEntry(ze);
					zos.write(file_contents.getBytes());
					zos.closeEntry();
				}
			}
			{
				// Plots in various formats
				Set<Integer> ids = m_lab.getPlotIds();
				byte[] byte_contents;
				for (int id : ids) // PNG
				{
					byte_contents = m_plotImageCallback.exportTo(id, "png");
					filename = "plot/" + id + ".png";
					ZipEntry ze = new ZipEntry(filename);
					zos.putNextEntry(ze);
					zos.write(byte_contents);
					zos.closeEntry();
				}
				for (int id : ids) // PDF
				{
					byte_contents = m_plotImageCallback.exportTo(id, "pdf");
					filename = "plot/" + id + ".pdf";
					ZipEntry ze = new ZipEntry(filename);
					zos.putNextEntry(ze);
					zos.write(byte_contents);
					zos.closeEntry();
				}
				for (int id : ids) // DUMB
				{
					byte_contents = m_plotImageCallback.exportTo(id, "dumb");
					filename = "plot/" + id + ".txt";
					ZipEntry ze = new ZipEntry(filename);
					zos.putNextEntry(ze);
					zos.write(byte_contents);
					zos.closeEntry();
				}
				for (int id : ids) // GP
				{
					byte_contents = m_plotImageCallback.exportTo(id, "gp");
					filename = "plot/" + id + ".gp";
					ZipEntry ze = new ZipEntry(filename);
					zos.putNextEntry(ze);
					zos.write(byte_contents);
					zos.closeEntry();
				}
			}
			zos.close();
		}
		catch (IOException e)
		{
			Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
		}
		return bos.toByteArray();
	}	
}
