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
package ca.uqac.lif.parkbench;

import java.util.Vector;

/**
 * A two-dimensional plot showing (x,y) values for multiple data series.
 */
public class Scatterplot extends TwoDeePlot
{
	/**
	 * Whether to draw each data series with lines between each data point
	 */
	protected boolean m_withLines = false;
	
	/**
	 * Whether to draw each data series with marks for each data point
	 */
	protected boolean m_withPoints = true;
	
	
	/**
	 * Creates an empty scatterplot
	 */
	public Scatterplot()
	{
		super();
	}
	
	/**
	 * Tells the plot to draw each data series with lines between each
	 * data point
	 * @return This plot
	 */
	public Scatterplot withLines()
	{
		return withLines(true);
	}
	
	/**
	 * Tells the plot to draw each data series with a mark for each
	 * data point
	 * @return This plot
	 */
	public Scatterplot withPoints()
	{
		return withPoints(true);
	}
	
	/**
	 * Tells whether to draw each data series with lines between each
	 * data point
	 * @param b True to draw lines, false otherwise
	 * @return This plot
	 */
	public Scatterplot withLines(boolean b)
	{
		m_withLines = b;
		return this;
	}
	
	/**
	 * Tells whether to draw each data series with a mark for each
	 * data point
	 * @param b True to draw points, false otherwise
	 * @return This plot
	 */
	public Scatterplot withPoints(boolean b)
	{
		m_withPoints = b;
		return this;
	}
	
	@Override
	public String toGnuplot(Terminal term)
	{
		Vector<String> series = m_table.getSeriesNames();
		String csv_values = m_table.toCsv(series);
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
		out.append(getHeader(term));
		out.append("plot");
		for (int i = 0; i < series.size(); i++)
		{
			if (i > 0)
			{
				out.append(", ");
			}
			String s_name = series.get(i);
			out.append(" '-' using 0:").append(i + 1).append(" title '").append(s_name).append("'").append(point_string);
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
