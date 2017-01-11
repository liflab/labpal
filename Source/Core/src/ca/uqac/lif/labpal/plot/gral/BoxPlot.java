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
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.XYPlot;

public class BoxPlot extends GralPlot implements TwoDimensionalPlot
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
	 * Creates a new box plot with default settings
	 * @param t The table
	 * @param transformation A table transformation
	 */
	public BoxPlot(Table t, TableTransformation transformation)
	{
		super(t, transformation);
	}
	
	public BoxPlot(Table t)
	{
		super(t);
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
		DataSource box_source = de.erichseifert.gral.plots.BoxPlot.createBoxData(source);
		de.erichseifert.gral.plots.BoxPlot plot = new de.erichseifert.gral.plots.BoxPlot(box_source);
		plot.setInsets(new Insets2D.Double(20d, 60d, 60d, 40d));
		plot.getTitle().setText(getTitle());
		if (!m_captionX.isEmpty())
		{
			plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(m_captionX);
		}
		else
		{
			plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(source.getColumnName(0));
		}
		plot.getAxisRenderer(XYPlot.AXIS_Y).getLabel().setText(m_captionY);
		customize(plot);
		return plot;
	}

	@Override
	public TwoDimensionalPlot setLogscale(Axis axis) 
	{
		// Makes no sense in a box plot
		return this;
	}
}
