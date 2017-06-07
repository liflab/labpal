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

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ca.uqac.lif.jerrydog.CachedRequestCallback;
import ca.uqac.lif.jerrydog.InnerFileServer;
import ca.uqac.lif.jerrydog.RequestCallback;
import ca.uqac.lif.labpal.CliParser;
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
	 * Creates a new LabPal server
	 * @param args
	 * @param lab
	 * @param assistant
	 */
	public LabPalServer(ArgumentMap args, Laboratory lab, LabAssistant assistant)
	{
		super(LabPalServer.class, true, s_cacheInterval);
		setUserAgent("LabPal " + Laboratory.s_versionString);
		if (args.hasOption("port"))
		{
			setServerPort(Integer.parseInt(args.getOptionValue("port")));
		}
		else
		{
			setServerPort(s_defaultPort);
		}
		registerCallback(0, new HomePageCallback(lab, assistant));
		CachedRequestCallback css_callback = new CachedRequestCallback(new CssCallback(this, lab, assistant));
		css_callback.setCachingEnabled(true);
		css_callback.setCachingInterval(s_cacheInterval);
		registerCallback(0, css_callback);
		registerCallback(0, new StatusPageCallback(lab, assistant, args.hasOption("no-env-check")));
		registerCallback(0, new ExperimentPageCallback(lab, assistant));
		registerCallback(0, new ExperimentsPageCallback(lab, assistant));
		registerCallback(0, new AssistantPageCallback(lab, assistant));
		registerCallback(0, new PlotsPageCallback(lab, assistant));
		registerCallback(0, new PlotImageCallback(lab, assistant));
		registerCallback(0, new DownloadCallback(lab, assistant));
		registerCallback(0, new UploadCallback(this, lab, assistant));
		registerCallback(0, new HelpPageCallback(lab, assistant));
		registerCallback(0, new AllPlotsCallback(lab, assistant));
		registerCallback(0, new AllPlotsLatexCallback(lab, assistant));
		registerCallback(0, new TablesPageCallback(lab, assistant));
		registerCallback(0, new TablePageCallback(lab, assistant));
		registerCallback(0, new TableExportCallback(lab, assistant));
		registerCallback(0, new AllTablesCallback(lab, assistant));
		registerCallback(0, new ExplainCallback(lab, assistant));
		registerCallback(0, new ExplainImageCallback(lab, assistant));
		registerCallback(0, new FindFormCallback(lab, assistant));
		registerCallback(0, new MacrosPageCallback(lab, assistant));
		registerCallback(0, new AllMacrosLatexCallback(lab, assistant));
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
	
	public static void setupCli(CliParser parser)
	{
		
	}
}
