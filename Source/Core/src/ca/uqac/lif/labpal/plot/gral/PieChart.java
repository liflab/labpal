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

import ca.uqac.lif.labpal.table.DataTable;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.TableTransformation;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.PiePlot;

public class PieChart extends GralPlot implements ca.uqac.lif.labpal.plot.PieChart
{
	public PieChart(Table t)
	{
		super(t);
	}
	
	public PieChart(Table t, TableTransformation transformation)
	{
		super(t, transformation);
	}
	
	@Override
	public de.erichseifert.gral.plots.Plot getPlot(DataTable source)
	{
		PiePlot plot = new PiePlot(source);
		plot.setInsets(new Insets2D.Double(20d, 60d, 60d, 40d));
		plot.getTitle().setText(getTitle());
		plot.setLegendVisible(true);
		customize(plot);
		return plot;
	}

}
