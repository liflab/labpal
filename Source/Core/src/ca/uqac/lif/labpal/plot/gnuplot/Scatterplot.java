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
package ca.uqac.lif.labpal.plot.gnuplot;

import java.util.Vector;

import ca.uqac.lif.labpal.plot.TwoDimensionalPlot;
import ca.uqac.lif.labpal.table.DataTable;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.TableTransformation;

/**
 * Scatterplot with default settings. Given a table, this class will draw
 * an x-y scatterplot with the first column as the values for the "x" axis,
 * and every remaining column as a distinct data series plotted on the "y"
 * axis.
 *   
 * @author Sylvain Hallé
 */
public class Scatterplot extends GnuPlot implements ca.uqac.lif.labpal.plot.Scatterplot
{
	/**
	 * The caption of the X axis
	 */
	protected String m_captionX = null;
	
	/**
	 * The caption of the Y axis
	 */
	protected String m_captionY = null;
	
	/**
	 * Whether to draw each data series with lines between each data point
	 */
	protected boolean m_withLines = false;
	
	/**
	 * Whether to draw each data series with marks for each data point
	 */
	protected boolean m_withPoints = true;
	
	/**
	 * Creates a scatterplot
	 * @param table
	 */
	public Scatterplot(Table table)
	{
		super(table);
	}
	
	/**
	 * Creates a scatterplot
	 * @param table
	 * @param transformation
	 */
	public Scatterplot(Table table, TableTransformation transformation)
	{
		super(table, transformation);
	}
	
	@Override
	public Scatterplot withLines()
	{
		return withLines(true);
	}
	
	@Override
	public Scatterplot withPoints()
	{
		return withPoints(true);
	}
	
	@Override
	public Scatterplot withLines(boolean b)
	{
		m_withLines = b;
		return this;
	}
	
	@Override
	public Scatterplot withPoints(boolean b)
	{
		m_withPoints = b;
		return this;
	}


	@Override
	public TwoDimensionalPlot setCaption(Axis axis, String caption)
	{
		if (axis == Axis.X)
		{
			m_captionX = caption;
		}
		else
		{
			m_captionY = caption;
		}
		return this;
	}

	@Override
	public String toGnuplot(ImageType term, String lab_title)
	{
		DataTable tab = processTable(m_table.getConcreteTable());
		String[] columns = tab.getColumnNames();
		Vector<String> series = new Vector<String>();
		for (int i = 1; i < columns.length; i++)
		{
			series.add(columns[i]);
		}
		String csv_values = tab.toCsv(s_datafileSeparator, s_datafileMissing);
		String point_string = " with points";
		if (m_withLines)
		{
			if (m_withPoints)
			{
				point_string = " with linespoints";
			}
			else
			{
				point_string = " with lines";
			}
		}
		// Build GP string from table
		StringBuilder out = new StringBuilder();
		out.append(getHeader(term, lab_title));
		out.append("set xlabel \"").append(m_captionX).append("\"\n");
		out.append("set ylabel \"").append(m_captionY).append("\"\n");
		out.append("plot");
		for (int i = 0; i < series.size(); i++)
		{
			if (i > 0)
			{
				out.append(", ");
			}
			String s_name = series.get(i);
			out.append(" '-' using 1:").append(i + 2).append(" title '").append(s_name).append("'").append(point_string);
		}
		out.append("\n");
		// In Gnuplot, if we use the special "-" filename, we must repeat
		// the data as many times as we use it in the plot command; it does not remember it
		for (int i = 0; i < series.size(); i++)
		{
			out.append(csv_values).append("end\n");
		}
		return out.toString();
	}

}
