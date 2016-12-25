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
package ca.uqac.lif.labpal.plot;

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.table.CandlesticksTransform;
import ca.uqac.lif.labpal.table.ConcreteTable;
import ca.uqac.lif.labpal.table.Table;

/**
 * Two-dimensional candlesticks diagram, also called "box-and-whiskers".
 * <p>
 * <strong>Example usage.</strong> 
 * Suppose you have a table like this:
 * <table>
 * <tr><th>#</th><th>A</th><th>B</th><th>C</th></tr>
 * <tr><td>Foo</td><td>1</td><td>4</td><td>9</td></tr>
 * <tr><td>Bar</td><td>10</td><td>5</td><td>8</td></tr>
 * <tr><td>Baz</td><td>3</td><td>2</td><td>3</td></tr>
 * </table>
 * We wish to create a candlesticks diagram displaying, for each column A, B, C,
 * a box representing the minimum value, first quartile, median, third quartile
 * and max value.
 * <pre>
 * Table t = ...
 * CandlesticksPlot plot = new CandlesticksPlot(t);
 * ...
 * plot.useForX("browser").useForY("share").groupBy("market");
 * </pre>
 * This will create a histogram that looks like this:
 * <pre>
 * |          ---
 * |    ---    |
 * |    +-+    |
 * |    | |   +-+
 * |    +-+   +-+
 * |    | |   | |
 * |     |    ---
 * |    ---   
 * |   
 * +-----+-----+-----+-----&gt;
 *      A      B     C
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
	
	/**
	 * Creates a new candlesticks plot
	 * @param t The table from which the data will be taken
	 */
	public CandlesticksPlot(Table t)
	{
		super(t);
		setFillStyle(FillStyle.NONE);
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
		CandlesticksTransform ct = new CandlesticksTransform(m_table);
		ConcreteTable tabu = ct.getConcreteTable();
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
		out.append("plot '-' using 7:3:2:6:5:xticlabels(1) with candlesticks whiskerbars, '-' using 7:4:4:4:4 with candlesticks notitle").append(FileHelper.CRLF);
		// In Gnuplot, if we use the special "-" filename, we must repeat
		// the data as many times as we use it in the plot command; it does not remember it
		for (int i = 0; i < 2; i++)
		{
			out.append(csv_values).append("end").append(FileHelper.CRLF);
		}
		return out.toString();
	}

}
