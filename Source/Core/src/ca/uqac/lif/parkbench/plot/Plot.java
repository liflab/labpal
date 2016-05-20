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
import ca.uqac.lif.parkbench.FileHelper;
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

	/**
	 * The fill style used for the plot
	 */
	public static enum FillStyle {SOLID, NONE, PATTERN};

	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#E41A1C">&#x25A0;</span>
	 * <span style="color:#377EB8">&#x25A0;</span>
	 * <span style="color:#4DAF4A">&#x25A0;</span>
	 * <span style="color:#984EA3">&#x25A0;</span>
	 * <span style="color:#FF7F00">&#x25A0;</span>
	 * <span style="color:#FFFF33">&#x25A0;</span>
	 * <span style="color:#A65628">&#x25A0;</span>
	 * <span style="color:#F781BF">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set1.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 * 
	 */
	public static final transient Palette QUALITATIVE_1;

	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#66C2A5">&#x25A0;</span>
	 * <span style="color:#FC8D62">&#x25A0;</span>
	 * <span style="color:#8DA0CB">&#x25A0;</span>
	 * <span style="color:#E78AC3">&#x25A0;</span>
	 * <span style="color:#A6D854">&#x25A0;</span>
	 * <span style="color:#FFD92F">&#x25A0;</span>
	 * <span style="color:#E5C494">&#x25A0;</span>
	 * <span style="color:#B3B3B3">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set2.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 */
	public static final transient Palette QUALITATIVE_2; 

	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#8DD3C7">&#x25A0;</span>
	 * <span style="color:#FFFFB3">&#x25A0;</span>
	 * <span style="color:#BEBADA">&#x25A0;</span>
	 * <span style="color:#FB8072">&#x25A0;</span>
	 * <span style="color:#80B1D3">&#x25A0;</span>
	 * <span style="color:#FDB462">&#x25A0;</span>
	 * <span style="color:#B3DE69">&#x25A0;</span>
	 * <span style="color:#FCCDE5">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set3.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 */
	public static final transient Palette QUALITATIVE_3;

	/**
	 * A 16-color preset palette for qualitative data, corresponding to the
	 * 16 EGA colors.
	 */
	public static final transient Palette EGA;

	/**
	 * The bytes of a blank image, used as a placeholder when no plot can
	 * be drawn by Gnuplot
	 */
	private static final transient byte[] s_blankImage = FileHelper.internalFileToBytes(Plot.class, "blank.png");

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
	protected static transient final boolean s_gnuplotPresent = FileHelper.commandExists(s_path);

	/**
	 * The palette used to draw the graph
	 */
	protected transient Palette m_palette = null;

	/**
	 * The fill style used to draw the graph
	 */
	protected transient FillStyle m_fillStyle = FillStyle.SOLID;

	/**
	 * The time to wait before polling GnuPlot's result
	 */
	protected static transient long s_waitInterval = 100;

	static {
		// Setup of discrete palettes
		// Found from https://github.com/aschn/gnuplot-colorbrewer
		QUALITATIVE_1 = new DiscretePalette("#E41A1C", "#377EB8", "#4DAF4A", "#984EA3", "#FF7F00", "#FFFF33", "#A65628", "#F781BF");
		QUALITATIVE_2 = new DiscretePalette("#66C2A5", "#FC8D62", "#8DA0CB", "#E78AC3", "#A6D854", "#FFD92F", "#E5C494", "#B3B3B3");
		QUALITATIVE_3 = new DiscretePalette("#8DD3C7", "#FFFFB3", "#BEBADA", "#FB8072", "#80B1D3", "#FDB462", "#B3DE69", "#FCCDE5");
		EGA = new DiscretePalette("#5555FF", "#55FF55", "#55FFFF", "#FF5555", "#FF55FF", "#FFFF55", "#0000AA", "#00AA00", "#00AAAA", "#AA0000", "#AA00AA", "#AA5500", "#AAAAAA", "#555555", "#FFFFFF", "#000000");
	}

	/**
	 * Creates a new plot
	 */
	Plot()
	{
		super();
		m_title = "Untitled";
		m_id = s_idCounter++;
		setPalette(EGA);
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
	 * Assigns this plot to a laboratory
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
	 * Sets the palette to display the graph
	 * @param p The palette. Set to <tt>null</tt> to use the default palette.
	 * @return This plot
	 */
	public Plot setPalette(Palette p)
	{
		m_palette = p;
		return this;
	}

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
	 * Sets the fill style
	 * @return This plot
	 */
	public final Plot setFillStyle(FillStyle s)
	{
		m_fillStyle = s;
		return this;
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
		if (image == null || image.length == 0)
		{
			// Gnuplot could not produce a picture; return the blank image
			image = s_blankImage;
		}
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
	 * Produces a header that is common to all plots generated by the
	 * application
	 * @param term The terminal to display this plot
	 * @return The header
	 */
	public StringBuilder getHeader(Terminal term, String lab_name)
	{
		StringBuilder out = new StringBuilder();
		out.append("# ----------------------------------------------------------------").append(FileHelper.CRLF);
		out.append("# File generated by ParkBench ").append(Laboratory.s_versionString).append(FileHelper.CRLF);
		out.append("# Date:     ").append(String.format("%1$te-%1$tm-%1$tY", Calendar.getInstance())).append(FileHelper.CRLF);
		out.append("# Lab name: ").append(lab_name).append(FileHelper.CRLF);
		out.append("# ----------------------------------------------------------------").append(FileHelper.CRLF);
		out.append("set title \"").append(m_title).append("\"").append(FileHelper.CRLF);
		out.append("set datafile separator \"").append(s_datafileSeparator).append("\"").append(FileHelper.CRLF);
		out.append("set datafile missing \"").append(s_datafileMissing).append("\"").append(FileHelper.CRLF);
		out.append("set terminal ").append(getTerminalName(term)).append(FileHelper.CRLF);
		switch (m_fillStyle)
		{
		case PATTERN:
			out.append("set style fill pattern").append(FileHelper.CRLF);
			break;
		case SOLID:
			out.append("set style fill solid").append(FileHelper.CRLF);
			break;
		default:
			// Do nothing
		}
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

	/**
	 * Gets the fill color associated with a number, based on the palette
	 * defined for this plot.
	 * @param color_nb The color number
	 * @return An empty string if no palette is defined, otherwise the
	 *   <tt>fillcolor</tt> expression corresponding to the color
	 */
	protected String getFillColor(int color_nb)
	{
		if (m_palette == null || m_fillStyle != FillStyle.SOLID)
		{
			return "";
		}
		return "fillcolor rgb \"" + m_palette.getHexColor(color_nb) + "\"";
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || ! (o instanceof Plot))
		{
			return false;
		}
		return m_id == ((Plot) o).m_id;
	}
	
	@Override
	public int hashCode()
	{
		return m_id;
	}

}
