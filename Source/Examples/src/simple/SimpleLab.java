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
package simple;

import ca.uqac.lif.spreadsheet.plot.Plot.Axis;
import ca.uqac.lif.spreadsheet.plots.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.LabPalGnuplot;
import ca.uqac.lif.labpal.table.ExperimentTable;

/**
 * A simple lab with one set of experiments whose values are drawn in a
 * scatterplot.
 */
public class SimpleLab extends Laboratory
{	
	@Override
	public void setup()
	{
		// Sets the title of this lab
		setTitle("A simple laboratory");
		setAuthor("Emmett Brown");
		
		// Setup a table to collate experiment results
		ExperimentTable table = new ExperimentTable("a", "y");
		table.setTitle("Value of y for Experiment A");
		add(table);
		
		// Create the experiments
		for (int i = 0; i < 5; i++)
		{
			ExperimentA e = new ExperimentA(i);
			add(e); // Add to lab
			table.add(e); // Add to table
		}
		
		// Prepare a scatterplot from the table
		LabPalGnuplot plot = new LabPalGnuplot(table, new GnuplotScatterplot()
				.setTitle("A scatterplot")
				.setCaption(Axis.X, "Value of a")
				.setCaption(Axis.Y, "Value of y")
				.withLines().withPoints());
		add(plot); // Add to lab
	}
	
	public static void main(String[] args)
	{
		// Nothing more do to here
		initialize(args, SimpleLab.class);
	}

}
