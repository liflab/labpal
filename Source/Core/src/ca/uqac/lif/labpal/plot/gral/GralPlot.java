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
package ca.uqac.lif.labpal.plot.gral;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.table.DataTable;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.TableTransformation;
import ca.uqac.lif.labpal.table.TempTable;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;

/**
 * Top-level class for plots drawn using the GRAL library.
 * @author Sylvain Hallé
 */
public class GralPlot extends Plot
{	
	/**
	 * Creates a new plot from a table
	 * @param t The table
	 */
	public GralPlot(Table t)
	{
		super(t);
	}

	/**
	 * Creates a new plot from a table, applying a transformation
	 *  to this table
	 * @param t The table
	 * @param transformation A table transformation. This transformation
	 *  will be applied to the table before plotting.
	 */
	public GralPlot(Table t, TableTransformation transformation)
	{
		super(t, transformation);
	}

	/**
	 * Gets the MIME type string associated to an image format
	 * @param t The format
	 * @return The MIME string
	 */
	public static String getTypeName(ImageType t)
	{
		switch (t)
		{
		case PNG:
			return "image/png";
		case PDF:
			return "application/pdf";
		case DUMB:
		case CACA:
			return "text/plain";
		}
		return "image/png";
	}

	/**
	 * Runs GnuPlot on a file and returns the resulting graph
	 * @param term The terminal (i.e. PNG, etc.) to use for the image
	 * @param with_caption Set to true to ignore the plot's caption when
	 *   rendering
	 * @return The (binary) contents of the image produced by Gnuplot
	 */
	public final byte[] getImage(ImageType term, boolean with_caption)
	{
		de.erichseifert.gral.plots.Plot plot = getPlot();
		if (!with_caption)
		{
			// Override caption with empty string
			plot.getTitle().setText("");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String type_string = getTypeName(term);
		DrawableWriter wr = DrawableWriterFactory.getInstance().get(type_string);
		try
		{
			wr.write(plot, baos, 640, 480);
			baos.flush();
			byte[] bytes = baos.toByteArray();
			baos.close();
			return bytes;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets a Plot object from this LabPlot
	 * @return The plot
	 */
	public final de.erichseifert.gral.plots.Plot getPlot()
	{
		TempTable dt = m_table.getDataTable();
		return getPlot(processTable(dt));
	}

	/**
	 * Gets a Plot object from a data source
	 * @return The plot
	 */
	public de.erichseifert.gral.plots.Plot getPlot(DataTable source)
	{
		return null;
	}

	/**
	 * Customize an existing plot. Override this method to tweak the settings
	 * of a stock plot.
	 * @param plot The plot
	 */
	public void customize(de.erichseifert.gral.plots.Plot plot)
	{
		// Do nothing
	}

}
