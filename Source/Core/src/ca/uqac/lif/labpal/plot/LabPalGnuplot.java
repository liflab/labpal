/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2019 Sylvain Hallé

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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.Plot;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.plots.gnuplot.Gnuplot;

/**
 * A plot that uses Gnuplot for its rendering.
 * @author Sylvain Hallé
 */
public class LabPalGnuplot extends LabPalPlot
{
	protected LabPalGnuplot(Table t, Plot p)
	{
		super(t, p);
	}
	
	protected LabPalGnuplot(int id, Table t, Plot p)
	{
		super(id, t, p);
	}

	public String toGnuplot(PlotFormat format, String plot_title, boolean with_title)
	{
		Spreadsheet s = m_table.getSpreadsheet();
		m_plot.setTitle(plot_title);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		((Gnuplot) m_plot).toGnuplot(new PrintStream(baos), s, format, with_title);
		return baos.toString();
	}

}
