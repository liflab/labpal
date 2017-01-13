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

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.plot.BoxPlot;
import ca.uqac.lif.labpal.plot.TwoDimensionalPlot;
import ca.uqac.lif.labpal.table.DataTable;
import ca.uqac.lif.labpal.table.Table;

/**
 * Gnuplot implementation of a boxplot.
 * @author Sylvain Hallé
 */
public class GnuBoxPlot extends GnuPlot implements BoxPlot
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
	 * Optional names given to the data series
	 */
	protected String[] m_seriesNames;
	
	public GnuBoxPlot(Table table) 
	{
		super(table);
		m_seriesNames = null;
	}
	
	public GnuBoxPlot(Table table, String ... series_names) 
	{
		super(table);
		if (series_names.length == 0)
			m_seriesNames = null;
		else
			m_seriesNames = series_names;
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
	public String toGnuplot(ImageType term, String lab_title, boolean with_caption)
	{
		DataTable tab = processTable(m_table.getDataTable());
		String csv_values = tab.toCsv(s_datafileSeparator, s_datafileMissing);
		StringBuilder out = new StringBuilder();
		out.append(getHeader(term, lab_title, with_caption));
		out.append("set boxwidth 0.2 absolute").append(FileHelper.CRLF);
		out.append("set offset 0.5,0.5,0,0").append(FileHelper.CRLF);
		out.append("set ytics nomirror").append(FileHelper.CRLF);
		out.append("set xlabel \"").append(m_captionX).append("\"").append(FileHelper.CRLF);
		out.append("set ylabel \"").append(m_captionY).append("\"").append(FileHelper.CRLF);
		int num_series = (tab.getColumnCount() - 1) / 5;
		float offset = 0, offset_step = 0.3f;
		if (num_series % 2 == 0)
		{
			offset = -((((float) num_series) / 2f) - 1f) * offset_step - 0.15f;
		}
		else
		{
			offset = - ((float) num_series - 1f) / 2f * offset_step;
		}
		out.append("plot ");
		for (int s_count = 0; s_count < num_series; s_count++)
		{
			String signum = "+";
			if (offset < 0)
				signum = "-";
			out.append("'-' using ($1").append(signum).append(Math.abs(offset)).append("):").append(5 * s_count + 3).append(":").append(5 * s_count + 2).append(":").append(5 * s_count + 6).append(":").append(5 * s_count + 5).append(" with candlesticks title \"").append(getSeriesName(s_count)).append("\" whiskerbars, ");
			out.append("'' using ($1").append(signum).append(Math.abs(offset)).append("):").append(5 * s_count + 4).append(":").append(5 * s_count + 4).append(":").append(5 * s_count + 4).append(":").append(5 * s_count + 4).append(" with candlesticks linetype -1 linewidth 2 notitle, ");
			offset += offset_step;
		}
		out.append(FileHelper.CRLF);
		// In Gnuplot, if we use the special "-" filename, we must repeat
		// the data as many times as we use it in the plot command; it does not remember it
		for (int i = 0; i < num_series + 1; i++)
		{
			out.append(csv_values).append("end").append(FileHelper.CRLF);;
		}
		return out.toString();
	}
	
	protected String getSeriesName(int index)
	{
		if (m_seriesNames == null || index >= m_seriesNames.length)
		{
			return "Series " + (index + 1);
		}
		return m_seriesNames[index];
	}

	@Override
	public TwoDimensionalPlot setLogscale(Axis axis)
	{
		// Nothing to do
		return this;
	}

}
