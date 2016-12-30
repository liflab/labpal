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
package sorting;

import java.util.List;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.labpal.plot.gral.Scatterplot;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.labpal.table.ExpandAsColumns;
import ca.uqac.lif.labpal.table.ExperimentTable;

public class SortingLab extends Laboratory
{
	public void setupExperiments(ArgumentMap map, List<WebCallback> callbacks)
	{
		// Give a name to the lab
		setTitle("Sorting Algorithms");
		setDescription("This lab compares the performance of a few common sorting algorithms.");
		
		// Prepare a table
		ExperimentTable table = new ExperimentTable("size", "time", "name");
		table.setTitle("Comparison of sorting algorithms");
		add(table);

		// Initialize experiments
		for (int length = 5000; length <= 30000; length += 5000)
		{
			add(new QuickSort(length), table);
			add(new ShellSort(length), table);
			add(new BubbleSort(length), table);
			add(new GnomeSort(length), table);
		}
		
		// Prepare a plot from the results of the table
		Scatterplot plot = new Scatterplot(table, new ExpandAsColumns("name", "time"));
		plot.setCaption(Axis.X, "List size").setCaption(Axis.Y, "Time (ms)");
		plot.withLines();
		add(plot);
	}
	
	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, SortingLab.class);
	}
}
