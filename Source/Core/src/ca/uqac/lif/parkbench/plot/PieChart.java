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

import ca.uqac.lif.parkbench.table.ValueTable;
import ca.uqac.lif.parkbench.table.Tabular;

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
 * PieChart plot = new PieChart();
 * ...
 * plot.useForX("market").useForY("share");
 * </pre>
 * This will create a pie with one sector for each market, whose size
 * is proportional to the corresponding value of share.
 * <p>
 * Note that Gnuplot does not support pie charts; the drawing routine
 * implemented here was found on
 * <a href="http://gnuplot-surprising.blogspot.ca/2012/11/plot-pie-chart-using-gnuplot.html">Gnuplot
 * Surprising</a>. Also note that currently, this chart only displays the
 * "pie", but has no legend or labels.
 * 
 * @author Sylvain Hallé
 *
 */
public class PieChart extends TwoDeePlot
{
	public PieChart(ValueTable t)
	{
		super(t);
	}
	
	@Override
	public PieChart setPalette(Palette p)
	{
		if (p != null)
		{
			// A pie chart needs to have a palette explicitly declared
			super.setPalette(p);
		}
		return this;
	}

	@Override
	public String toGnuplot(Terminal term, String lab_title)
	{
		Tabular tab = m_table.getTabular();
		tab.normalizeColumns();
		// Build GP string from table
		StringBuilder out = new StringBuilder();
		out.append(getHeader(term, lab_title));
		out.append("set style fill solid 1.0 border -1\n");
		out.append("unset border\nunset tics\nunset key\n");
		int i = 1;
		float arc_start = 0;
		float arc_end = 0;
		for (String x : m_table.getXValues())
		{
			String s_val = tab.get(x, "");
			float f_val = Float.parseFloat(s_val);
			arc_end = arc_start + f_val * 360;
			out.append("set object ").append(i).append(" circle at screen 0.5,0.5 size screen 0.3 arc [").append(arc_start).append(":").append(arc_end).append("] ").append(getFillColor(i - 1)).append(" front\n");
			arc_start = arc_end;
			i++;
		}
		out.append("plot x with lines lc rgb \"#ffffff\"\n");
		return out.toString();
	}

}
