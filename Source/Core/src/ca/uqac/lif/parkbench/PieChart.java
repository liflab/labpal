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

/**
 * Two-dimensional pie chart.
 * <p>
 * <strong>Example usage.</strong> 
 * Suppose you have a set of experiments, each with two
 * parameters:
 * <ul>
 * <li><tt>market</tt> is the name of a market (video, audio, etc.)</li>
 * <li><tt>share</tt> is the market share (in %) for this market</li>
 * </ul>
 * We wish to create a bar diagram where each bar represents a market,
 * its height corresponds to the share, with one group of bars for each
 * browser. To is done by writing:
 * <pre>
 * BarPlot plot = new BarPlot();
 * ...
 * plot.useForX("market").useForY("share");
 * </pre>
 * This will create a pie with one sector for each market, whose size
 * is proportional to the corresponding value of share.
 * <p>
 * Note that Gnuplot does not support pie charts; the drawing routine
 * implemented here was found on
 * <a href="http://gnuplot-surprising.blogspot.ca/2012/11/plot-pie-chart-using-gnuplot.html">StackOverflow</a>.
 * @author Sylvain Hallé
 *
 */

public class PieChart extends TwoDeePlot
{

	@Override
	public String toGnuplot(Terminal term, String lab_title)
	{
		String csv_values = m_table.toCsv(false);
		// Build GP string from table
		StringBuilder out = new StringBuilder();
		out.append(getHeader(term, lab_title));
		m_table.getXValues();
		for (int i = 0; i < 5; i++)
		{
			out.append(csv_values).append("\nend\n");
		}
		return out.toString();
	}

}
