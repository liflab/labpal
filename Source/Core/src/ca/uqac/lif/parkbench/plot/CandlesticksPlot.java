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

import ca.uqac.lif.parkbench.FileHelper;
import ca.uqac.lif.parkbench.table.Table;
import ca.uqac.lif.parkbench.table.Tabular;

/**
 * Two-dimensional bar diagram, also called a "clustered histogram".
 * <p>
 * <strong>Example usage.</strong> 
 * Suppose you have a set of experiments, each with three
 * parameters:
 * <ul>
 * <li><tt>name</tt> is the name of a web browser (Firefox, IE, etc.)</li>
 * <li><tt>market</tt> is the name of a market (video, audio, etc.)</li>
 * <li><tt>share</tt> is the market share (in %) for this browser in this market</li>
 * </ul>
 * We wish to create a bar diagram where each bar represents a market,
 * its height corresponds to the share, with one group of bars for each
 * browser. To is done by writing:
 * <pre>
 * BarPlot plot = new BarPlot();
 * ...
 * plot.useForX("browser").useForY("share").groupBy("market");
 * </pre>
 * This will create a histogram that looks like this:
 * <pre>
 * |                     # video
 * |                     $ audio
 * |                     @ text
 * |    $
 * |    $@         @
 * |   #$@        $@
 * |   #$@       #$@
 * +----+---------+-----&gt;
 *   Firefox     IE
 * </pre>
 * @author Sylvain Hallé
 *
 */
public class CandlesticksPlot extends TwoDeePlot
{	
	/**
	 * The width of the box in the histogram. A value of -1 means the
	 * default setting will be used.
	 */
	protected float m_boxWidth = 0.75f;
	
	public CandlesticksPlot(Table t)
	{
		super(t);
	}
	
	/**
	 * Sets the box width of the histogram. This is equivalent to the
	 * <tt>boxwidth</tt> setting of Gnuplot.
	 * @param w The width (generally a value between 0 and 1)
	 * @return This plot
	 */
	public CandlesticksPlot boxWidth(float w)
	{
		m_boxWidth = w;
		return this;
	}

	@Override
	public String toGnuplot(Terminal term, String lab_title)
	{
		Tabular tabu = m_table.getTabular();
		String csv_values = tabu.toCsv();
		// Build GP string from table
		StringBuilder out = new StringBuilder();
		out.append(getHeader(term, lab_title));
		out.append("unset key").append(FileHelper.CRLF);
		out.append("set xrange [0:").append(tabu.getWidth()).append("]").append(FileHelper.CRLF);
		if (m_boxWidth > 0)
		{
			out.append("set boxwidth ").append(m_boxWidth).append(FileHelper.CRLF);
		}
		out.append("plot '-' using 7:4:3:5:4:xticlabels(1) with candlesticks whiskerbars").append(FileHelper.CRLF);
		// In Gnuplot, if we use the special "-" filename, we must repeat
		// the data as many times as we use it in the plot command; it does not remember it
		for (int i = 0; i < 1; i++)
		{
			out.append(csv_values).append("end").append(FileHelper.CRLF);
		}
		return out.toString();
	}

}
