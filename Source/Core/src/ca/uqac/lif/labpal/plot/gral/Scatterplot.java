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

import ca.uqac.lif.labpal.plot.TwoDimensionalPlot;
import ca.uqac.lif.labpal.table.DataTable;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.TableTransformation;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;

/**
 * Scatterplot with default settings. Given a table, this class will draw
 * an x-y scatterplot with the first column as the values for the "x" axis,
 * and every remaining column as a distinct data series plotted on the "y"
 * axis.
 *   
 * @author Sylvain Hallé
 */
public class Scatterplot extends GralPlot implements ca.uqac.lif.labpal.plot.Scatterplot
{
	/**
	 * The caption for the "x" axis
	 */
	protected String m_captionX = "";
	
	/**
	 * The caption for the "y" axis
	 */
	protected String m_captionY = "";
	
	/**
	 * Whether to draw each data series with lines between each data point
	 */
	protected boolean m_withLines = false;
	
	/**
	 * Whether to draw each data series with marks for each data point
	 */
	protected boolean m_withPoints = true;

	/**
	 * Creates a new scatterplot with default settings
	 * @param t The table
	 */
	public Scatterplot(Table t)
	{
		super(t);
	}
	
	/**
	 * Creates a new scatterplot with default settings
	 * @param t The table
	 * @param transformation A table transformation
	 */
	public Scatterplot(Table t, TableTransformation transformation)
	{
		super(t, transformation);
	}
	
	@Override
	public Scatterplot withLines(boolean b)
	{
		m_withLines = b;
		return this;
	}
	
	@Override
	public Scatterplot withLines()
	{
		m_withLines = true;
		return this;
	}
	
	@Override
	public Scatterplot withPoints(boolean b)
	{
		m_withPoints = b;
		return this;
	}
	
	@Override
	public Scatterplot withPoints()
	{
		m_withPoints = true;
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
	public de.erichseifert.gral.plots.Plot getPlot(DataTable source)
	{
		int num_cols = source.getColumnCount();
		DataSeries[] series = new DataSeries[num_cols - 1];
		for (int col = 1; col < num_cols; col++)
		{
			series[col - 1] = new DataSeries(source.getColumnName(col), source, 0, col);
		}
		XYPlot plot = new XYPlot(series);
		for (int col = 1; col < num_cols; col++)
		{
			if (m_withPoints)
			{
				PointRenderer pr = new DefaultPointRenderer2D();
				plot.setPointRenderers(series[col - 1], pr);
				for (PointRenderer r : plot.getPointRenderers(series[col - 1]))
				{
					r.setColor(m_palette.getPaint(col - 1));
				}
			}
			if (m_withLines)
			{
				LineRenderer lr = new DefaultLineRenderer2D();
				plot.setLineRenderers(series[col - 1], lr);
				for (LineRenderer r : plot.getLineRenderers(series[col - 1]))
				{
					r.setColor(m_palette.getPaint(col - 1));
				}
			}
		}
		plot.setInsets(new Insets2D.Double(20d, 60d, 60d, 40d));
		plot.getTitle().setText(getTitle());
		if (series.length > 1)
		{
			// Put legend only if more than one data series
			plot.setLegendVisible(true);
		}
		plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(m_captionX);
		plot.getAxisRenderer(XYPlot.AXIS_Y).getLabel().setText(m_captionY);
		customize(plot);
		return plot;
	}

}
