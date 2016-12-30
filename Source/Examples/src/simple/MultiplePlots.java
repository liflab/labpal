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

import java.util.List;

import ca.uqac.lif.labpal.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.labpal.plot.gnuplot.ClusteredHistogram;
import ca.uqac.lif.labpal.plot.gral.Scatterplot;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.labpal.table.Join;
import ca.uqac.lif.labpal.table.RenameColumns;
import ca.uqac.lif.labpal.table.Table;

/**
 * Create data series from multiple experiments and plot them in the same
 * graph.
 */
public class MultiplePlots extends Laboratory
{	
	@Override
	public void setupExperiments(ArgumentMap map, List<WebCallback> callbacks)
	{
		// Sets the title of this lab
		setTitle("Two simple experiments");
		setAuthorName("Emmett Brown");
		
		// Put the results of the experiments in tables
		ExperimentTable table_exp_a = new ExperimentTable("a", "y");
		ExperimentTable table_exp_b = new ExperimentTable("a", "y");
		
		// Create the experiments
		for (int i = 0; i < 5; i++)
		{
			add(new ExperimentA(i), table_exp_a);
			add(new ExperimentB(i), table_exp_b);
		}
		
		// Prepare a plot from the "y" values of both types of experiments
		final Table table1 = new Join(new String[]{"a"},
				new RenameColumns(table_exp_a, "a", "Experiment A"),
				new RenameColumns(table_exp_b, "a", "Experiment B")
				);
		table1.setTitle("Comparison of Experiment A and Experiment B");
		add(table1);
		Scatterplot plot = new Scatterplot(table1);
		plot.setCaption(Axis.X, "Value of a").setCaption(Axis.Y, "Value of y");
		plot.withLines().withPoints();
		add(plot);
		
		// Same data, displayed as a histogram. This graph requires Gnuplot
		// to be shown
		ClusteredHistogram histogram = new ClusteredHistogram(table1);
		histogram.setCaption(Axis.X, "Value of a").setCaption(Axis.Y, "Value of y");
		add(histogram);
	}
	
	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, MultiplePlots.class);
	}

}
