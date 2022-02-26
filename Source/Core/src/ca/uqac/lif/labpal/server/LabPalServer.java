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

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ca.uqac.lif.jerrydog.InnerFileCallback;
import ca.uqac.lif.jerrydog.RequestCallback.Method;

/**
 * A web server allowing the interaction with the contents of a lab.
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
		registerCallback(new TemplatePageCallback(this, Method.GET, "/find", "find.ftlh", "top-menu-find").setTitle("Find"));
		registerCallback(new StatusPageCallback(this, Method.GET, "/status", "status.ftlh").setTitle("Status"));
		registerCallback(new ExperimentPageCallback(this, Method.GET, "/experiment/", "experiment.ftlh"));
		registerCallback(new ExperimentsStatusCallback(this, Method.GET, "/experiments/status"));
		registerCallback(new ExperimentsPageCallback(this, Method.GET, "/experiments", "experiments.ftlh"));
		registerCallback(new LabStatusCallback(this, Method.GET, "/lab/status"));
		registerCallback(new AssistantStatusCallback(this, Method.GET, "/assistant/status"));
		registerCallback(new AssistantPageCallback(this, Method.GET, "/assistant", "assistant.ftlh"));
		registerCallback(new TablesStatusCallback(this, Method.GET, "/tables/status"));
		registerCallback(new TablePageCallback(this, Method.GET, "/table/", "table.ftlh"));
		registerCallback(new TemplatePageCallback(this, Method.GET, "/tables", "tables.ftlh", "top-menu-tables").setTitle("Tables"));
		registerCallback(new AllPlotsCallback(lab));
		registerCallback(new PlotsStatusCallback(this, Method.GET, "/plots/status"));
		registerCallback(new TemplatePageCallback(this, Method.GET, "/plots", "plots.ftlh", "top-menu-plots").setTitle("Plots"));
		registerCallback(new PlotImageCallback(lab));
		registerCallback(new PlotsStatusCallback(this, Method.GET, "/plots/status"));
		registerCallback(new PlotPageCallback(this, Method.GET, "/plot/", "plot.ftlh"));
		registerCallback(new TemplatePageCallback(this, Method.GET, "/index", "index.ftlh", "top-menu-home").setTitle("Home"));
		registerCallback(new CssCallback(this, Method.GET, "/screen.css", "screen.css.ftlh"));
		registerCallback(new JavaScriptCallback("resource", LabPalServer.class));
		registerCallback(new InnerFileCallback("resource", LabPalServer.class));
	}

	/*@ pure non_null @*/ public Laboratory getLaboratory()
	{
		return m_lab;
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
				continue;
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
}
