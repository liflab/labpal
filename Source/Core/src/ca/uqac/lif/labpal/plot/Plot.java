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
package ca.uqac.lif.labpal.plot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.table.DataTable;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.TableTransformation;

/**
 * A representation of data into a picture
 * @author Sylvain Hallé
 */
public abstract class Plot
{
	/**
	 * The image type used for displaying the plot
	 */
	public static enum ImageType {PNG, DUMB, PDF, CACA};
	
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
	
	static {
		// Setup of discrete palettes
		// Found from https://github.com/aschn/gnuplot-colorbrewer
		QUALITATIVE_1 = new DiscretePalette("#E41A1C", "#377EB8", "#4DAF4A", "#984EA3", "#FF7F00", "#FFFF33", "#A65628", "#F781BF");
		QUALITATIVE_2 = new DiscretePalette("#66C2A5", "#FC8D62", "#8DA0CB", "#E78AC3", "#A6D854", "#FFD92F", "#E5C494", "#B3B3B3");
		QUALITATIVE_3 = new DiscretePalette("#8DD3C7", "#FFFFB3", "#BEBADA", "#FB8072", "#80B1D3", "#FDB462", "#B3DE69", "#FCCDE5");
		EGA = new DiscretePalette("#5555FF", "#55FF55", "#55FFFF", "#FF5555", "#FF55FF", "#FFFF55", "#0000AA", "#00AA00", "#00AAAA", "#AA0000", "#AA00AA", "#AA5500", "#AAAAAA", "#555555", "#FFFFFF", "#000000");
	}
	
	/**
	 * The table this plot is based on
	 */
	protected Table m_table;
	
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
	private static int s_idCounter = 1;
	
	/**
	 * A lock for accessing the counter
	 */
	private static Lock s_counterLock = new ReentrantLock();
	
	/**
	 * The lab that contains the experiments this plot is
	 * drawing
	 */
	protected transient Laboratory m_lab;
	
	/**
	 * The palette used to draw the data series for this plot
	 */
	protected Palette m_palette;
	
	/**
	 * A table transformation to apply before plotting
	 */
	protected TableTransformation m_transformation = null;
	
	/**
	 * Whether the plot shows a key
	 */
	protected transient boolean m_hasKey = true;
	
	/**
	 * The bytes of a blank image, used as a placeholder when no plot can
	 * be drawn
	 */
	public static final transient byte[] s_blankImage = FileHelper.internalFileToBytes(Plot.class, "blank.png");

	/**
	 * Creates a new plot from a table
	 * @param table The table
	 * @param transformation A transformation to apply to the table before
	 *   plotting
	 */
	public Plot(Table table, TableTransformation transformation)
	{
		super();
		m_table = table;
		m_title = table.getTitle();
		if (m_title.matches("Table \\d+"))
		{
			// Replace "Table n" by "Plot n" as the default name
			m_title = m_title.replace("Table", "Plot");
		}
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
		m_transformation = transformation;
		setPalette(EGA);
	}

	/**
	 * Creates a new plot from a table
	 * @param table The table
	 */
	public Plot(Table table)
	{
		this(table, null);
	}
	
	/**
	 * Creates a new plot
	 * @param title
	 * @param a
	 */
	protected Plot(Table t, String title, Laboratory a)
	{
		this(t);
		m_title = title;
		m_lab = a;
	}
	
	/**
	 * Gets the plot's ID
	 * @return The ID
	 */
	public final int getId()
	{
		return m_id;
	}
	
	/**
	 * Gets the plot's title
	 * @return The title
	 */
	public final String getTitle()
	{
		return m_title;
	}
	
	/**
	 * Sets the plot's title
	 * @param title The title
	 * @return This plot
	 */
	public final Plot setTitle(String title)
	{
		m_title = title;
		return this;
	}
	
	/**
	 * Assigns this plot to a laboratory
	 * @param a The assistant
	 * @return This plot
	 */
	public final Plot assignTo(Laboratory a)
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
	public final Plot setPalette(Palette p)
	{
		m_palette = p;
		return this;
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
	
	/**
	 * Sets if this plot shows a key when it has multiple data series
	 * @param b Set to {@code true} to enable the key, {@code false}
	 * otherwise
	 * @return This plot
	 */
	public Plot setKey(boolean b)
	{
		m_hasKey = b;
		return this;
	}
	
	/**
	 * Determines if this plot shows a key when it has multiple data series
	 * @return {@code true} if the key is enabled, {@code false} otherwise
	 */
	public boolean hasKey()
	{
		return m_hasKey;
	}
	
	/**
	 * Gets an image from this plot
	 * @param type The image type to produce
	 * @param with_caption Set to false to remove the caption from the image
	 *   (even if a caption is defined for the plot)
	 * @return An array of bytes containing the image, or {@code null} if
	 *   the image cannot be produced
	 */
	public abstract byte[] getImage(ImageType type, boolean with_caption);

	/**
	 * Gets an image from this plot
	 * @param type The image type to produce
	 * @return An array of bytes containing the image, or {@code null} if
	 *   the image cannot be produced
	 */
	public final byte[] getImage(ImageType type)
	{
		return getImage(type, true);
	}
	
	/**
	 * Transforms a data table before being plotted. A plot can override this
	 * method to perform pre-processing of the table.
	 * @param table The original table
	 * @return The transformed table
	 */
	public DataTable processTable(DataTable table)
	{
		if (m_transformation == null)
		{
			return table;
		}
		return m_transformation.transform(table);
	}

	/**
	 * Generates a suitable file extension for a given image type
	 * @param type The image type
	 * @return The extension
	 */
	public static final String getTypeExtension(ImageType type)
	{
		switch (type)
		{
		case CACA:
			return "txt";
		case DUMB:
			return "txt";
		case PDF:
			return "pdf";
		case PNG:
			return "png";
		default:
			return "";
		
		}
	}
}
