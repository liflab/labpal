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
public class ClusteredHistogram extends GnuPlot implements TwoDimensionalPlot
{
	/**
	 * The caption of the X axis
	 */
	protected String m_captionX = "";
	
	/**
	 * The caption of the Y axis
	 */
	protected String m_captionY = "";

	/**
	 * Whether the histogram is of type "row stacked".
	 * @see {@link #rowStacked()}
	 */
	protected boolean m_rowStacked = false;
	
	/**
	 * The width of the box in the histogram. A value of -1 means the
	 * default setting will be used.
	 */
	protected float m_boxWidth = 0.75f;
	
	/**
	 * Creates a new bar plot from a table
	 * @param t The table
	 */
	public ClusteredHistogram(Table t)
	{
		super(t);
	}
	
	/**
	 * Creates a new bar plot from a table
	 * @param table
	 * @param transformation
	 */
	public ClusteredHistogram(Table table, TableTransformation transformation)
	{
		super(table, transformation);
	}
	
	/**
	 * Sets whether the histogram is of type "row stacked".
	 * Using the example given above, the rowstacked setting will rather
	 * produce this plot:
	 * <pre>
   * |                     # video
   * |                     $ audio
   * |    @                @ text
   * |    @         @
   * |    $         @ 
   * |    $         $ 
   * |    #         # 
   * +----+---------+-----&gt;
   *   Firefox     IE
	 * </pre> 
	 * @return This plot
	 */
	public ClusteredHistogram rowStacked()
	{
		m_rowStacked = true;
		return this;
	}
	
	/**
	 * Sets the box width of the histogram. This is equivalent to the
	 * <tt>boxwidth</tt> setting of Gnuplot.
	 * @param w The width (generally a value between 0 and 1)
	 * @return This plot
	 */
	public ClusteredHistogram boxWidth(float w)
	{
		m_boxWidth = w;
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
		// Build GP string from table
		StringBuilder out = new StringBuilder();
		out.append(getHeader(term, lab_title));
		out.append("set xtics rotate out\n");
		out.append("set style data histogram\n");
		out.append("set xlabel \"").append(m_captionX).append("\"\n");
		out.append("set ylabel \"").append(m_captionY).append("\"\n");
		if (m_rowStacked)
		{
			out.append("set style histogram rowstacked\n");
		}
		else
		{
			out.append("set style histogram clustered gap 1\n");
		}
		if (m_boxWidth > 0)
		{
			out.append("set boxwidth ").append(m_boxWidth).append("\n");
		}
		out.append("set auto x\n");
		out.append("set yrange [0:*]\n");
		out.append("set style fill border rgb \"black\"\n");
		out.append("plot");
		for (int i = 0; i < series.size(); i++)
		{
			if (i > 0)
			{
				out.append(", ");
			}
			String s_name = series.get(i);
			out.append(" \"-\" using ").append(i + 2).append(":xtic(1) title \"").append(s_name).append("\" ").append(getFillColor(i));
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
	public ClusteredHistogram setLogscale(Axis axis)
	{
		// Does not make much sense for a histogram: do nothing
		return this;
	}

}