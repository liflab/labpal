/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé
  
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
package ca.uqac.lif.parkbench.plot;

import java.util.Calendar;

import ca.uqac.lif.parkbench.CommandRunner;
import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.Laboratory;

/**
 * A plot is responsible for converting data extracted from experiments
 * into a graphical representation.
 * 
 * @author Sylvain Hallé
 */
public abstract class Plot
{
	/**
	 * The terminal used for displaying the plot
	 */
	public static enum Terminal {PNG, DUMB, PDF, CACA};
	
	public static String[] s_paletteQualitative = {"red", "orange", "forest-green", "dark-magenta"};

	/**
	 * The plot's title
	 */
	protected String m_title;

	/**
	 * The plot's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing plot IDs
	 */
	protected static int s_idCounter = 1;
	
	/**
	 * The symbol used to separate data values in a file
	 */
	public static final transient String s_datafileSeparator = ",";
	
	/**
	 * The symbol used to represent missing values in a file
	 */
	public static final transient String s_datafileMissing = "?";

	/**
	 * The lab that contains the experiments this plot is
	 * drawing
	 */
	protected transient Laboratory m_lab;

	/**
	 * The path to launch GnuPlot
	 */
	protected static transient String s_path = "gnuplot";
	
	/**
	 * Whether gnuplot is present on the system
	 */
	protected static transient final boolean s_gnuplotPresent = checkGnuplot();

	/**
	 * The time to wait before polling GnuPlot's result
	 */
	protected static transient long s_waitInterval = 100;

	/**
	 * Creates a new plot
	 */
	Plot()
	{
		super();
		m_title = "Untitled";
		m_id = s_idCounter++;
	}

	/**
	 * Creates a new plot
	 * @param title
	 * @param a
	 */
	Plot(String title, Laboratory a)
	{
		this();
		m_title = title;
		m_lab = a;
		m_id = s_idCounter++;
	}

	/**
	 * Assigns this plot to a lab assistant
	 * @param a The assistant
	 * @return This plot
	 */
	public Plot assignTo(Laboratory a)
	{
		m_lab = a;
		a.add(this);
		return this;
	}

	/**
	 * Adds an experiment to the plot
	 * @param e The experiment
	 * @return This plot
	 */
	public abstract Plot add(Experiment e);

	/**
	 * Sets the plot's title
	 * @param t The title
	 * @return This plot
	 */
	public Plot setTitle(String t)
	{
		m_title = t;
		return this;
	}

	/**
	 * Gets the plot's title
	 * @return The title
	 */
	public String getTitle()
	{
		return m_title;
	}

	public static String getTerminalName(Terminal t)
	{
		switch (t)
		{
		case PNG:
			return "png";
		case PDF:
			return "pdf";
		case DUMB:
			return "dumb";
		case CACA:
			return "caca";
		}
		return "dumb";
	}

	/**
	 * Generates a stand-alone Gnuplot file for this plot
	 * @param term The terminal used to display the plot
	 * @return The Gnuplot file contents
	 */
	public final String toGnuplot(Terminal term)
	{
		return toGnuplot(term, "");
	}

	/**
	 * Generates a stand-alone Gnuplot file for this plot
	 * @param term The terminal used to display the plot
	 * @param lab_title The title of the lab. This is only used in the 
	 *   auto-generated comments in the file's header
	 * @return The Gnuplot file contents
	 */
	public abstract String toGnuplot(Terminal term, String lab_title);

	/**
	 * Runs GnuPlot on a file and returns the resulting graph
	 * @param term The terminal (i.e. PNG, etc.) to use for the image
	 * @return The (binary) contents of the image produced by Gnuplot
	 */
	public final byte[] getImage(Terminal term)
	{
		String instructions = toGnuplot(term);
		byte[] image = null;
		String[] command = {s_path};
		CommandRunner runner = new CommandRunner(command, instructions);
		runner.start();
		// Wait until the command is done
		while (runner.isAlive())
		{
			// Wait 0.1 s and check again
			try
			{
				Thread.sleep(s_waitInterval);
			}
			catch (InterruptedException e)
			{
				// This happens if the user cancels the command manually
				runner.stopCommand();
				runner.interrupt();
				return null;
			}
		}
		image = runner.getBytes();
		return image;
	}

	/**
	 * Gets the plot's ID
	 * @return The ID
	 */
	public int getId()
	{
		return m_id;
	}
	
	/**
	 * Checks if Gnuplot is present by attempting to run it
	 * @return true if Gnuplot is present, false otherwise
	 */
	protected static boolean checkGnuplot()
	{
		// Check if Gnuplot is present
		String[] args = {s_path, "--version"};
		CommandRunner runner = new CommandRunner(args);
		runner.run();
		return runner.getErrorCode() == 0;
	}
	
	/**
	 * Produces a header that is common to all plots generated by the
	 * application
	 * @param term The terminal to display this plot
	 * @return The header
	 */
	public StringBuilder getHeader(Terminal term, String lab_name)
	{
		StringBuilder out = new StringBuilder();
		out.append("# ----------------------------------------------------------------\n");
		out.append("# File generated by ParkBench ").append(Laboratory.s_versionString).append("\n");
		out.append("# Date:     ").append(String.format("%1$te-%1$tm-%1$tY", Calendar.getInstance())).append("\n");
		out.append("# Lab name: ").append(lab_name).append("\n");
		out.append("# ----------------------------------------------------------------\n");
		out.append("set title \"").append(m_title).append("\"\n");
		out.append("set datafile separator \"").append(s_datafileSeparator).append("\"\n");
		out.append("set datafile missing \"").append(s_datafileMissing).append("\"\n");
		out.append("set terminal ").append(getTerminalName(term)).append("\n");
		return out;
	}
	
	/**
	 * Checks if Gnuplot is present in the system
	 * @return true if Gnuplot is present, false otherwise
	 */
	public static boolean isGnuplotPresent()
	{
		return s_gnuplotPresent;
	}
}