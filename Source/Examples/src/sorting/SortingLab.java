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

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.macro.ConstantNumberMacro;
import ca.uqac.lif.mtnp.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.mtnp.plot.gnuplot.ClusteredHistogram;
import ca.uqac.lif.mtnp.plot.gnuplot.Scatterplot;
import ca.uqac.lif.mtnp.table.ColumnSum;
import ca.uqac.lif.mtnp.table.ExpandAsColumns;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.mtnp.table.TransformedTable;

/**
 * This is an example of a lab that creates experiments to compare
 * sorting algorithms. It is intended as a showcase of the various
 * features that are available in LabPal.
 * 
 * @author Sylvain Hallé
 */
public class SortingLab extends Laboratory
{
	public void setup()
	{
		// A few constants
		final int min_length = 5000;
		final int max_length = 10000;
		final int increment = 5000;
		
		// Give a name to the lab
		setTitle("Sorting Algorithms");
		setAuthor("Fred Flintstone");
		
		// Prepare a table
		ExperimentTable table = new ExperimentTable("size", "time", "name");
		table.setTitle("Comparison of sorting algorithms").setNickname("sorttime");
		add(table);

		// Initialize experiments
		for (int length = min_length; length <= max_length; length += increment)
		{
			add(new QuickSort(length), table);
			add(new ShellSort(length), table);
			add(new BubbleSort(length), table);
			add(new GnomeSort(length), table);
		}
		
		// Prepare a plot from the results of the table
		TransformedTable t_table = new TransformedTable(ExpandAsColumns.get("name", "time"), table);
		t_table.setTitle("Sorting time per algorithm").setNickname("sorttimealg");
		add(t_table);
		Scatterplot plot = new Scatterplot(t_table);
		plot.setCaption(Axis.X, "List size").setCaption(Axis.Y, "Time (ms)");
		plot.withLines().setNickname("sortplot");
		add(plot);
		ClusteredHistogram c_plot = new ClusteredHistogram(t_table);
		c_plot.setTitle("Sorting time for each array").setNickname("sorthisto");
		c_plot.setCaption(Axis.X, "List size").setCaption(Axis.Y, "Time (ms)");
		add(c_plot);
		
		// Just for fun, create another plot with the sum of all sorting
		// times for each algorithm
		TransformedTable p_table = new TransformedTable(ColumnSum.get(), t_table);
		p_table.setTitle("Cumulative sorting time").setNickname("sumtime");
		add(p_table);
		ClusteredHistogram p_plot = new ClusteredHistogram(p_table);
		c_plot.setCaption(Axis.X, "Total size").setCaption(Axis.Y, "Time (ms)");
		add(p_plot);
		
		// Create a few macros showing summary information
		add(new ConstantNumberMacro(this, "maxSize", "The maximum size of the arrays sorted in the experiments", max_length));
		add(new ConstantNumberMacro(this, "numAlgos", "The number of algorithms compared in this lab", 4));
		add(new SlowestMacro(this));
	}
	
	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, SortingLab.class);
	}
}
