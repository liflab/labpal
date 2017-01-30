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

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.macro.ConstantNumberMacro;
import ca.uqac.lif.labpal.macro.Macro;
import ca.uqac.lif.labpal.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.labpal.plot.gnuplot.Scatterplot;
import ca.uqac.lif.labpal.table.ExpandAsColumns;
import ca.uqac.lif.labpal.table.ExperimentTable;

public class SortingLab extends Laboratory
{
	public void setup()
	{
		// A few constants
		final int min_length = 5000;
		final int max_length = 30000;
		final int increment = 5000;
		
		// Give a name to the lab
		setTitle("Sorting Algorithms");
		setAuthor("Fred Flintstone");
		setDescription("This lab compares the performance of a few common sorting algorithms.");
		
		// Prepare a table
		ExperimentTable table = new ExperimentTable("size", "time", "name");
		table.setTitle("Comparison of sorting algorithms");
		table.setNickname("sorttime");
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
		Scatterplot plot = new Scatterplot(table, new ExpandAsColumns("name", "time"));
		plot.setCaption(Axis.X, "List size").setCaption(Axis.Y, "Time (ms)");
		plot.withLines().setNickname("sortplot");
		add(plot);
		
		// Create a few macros showing summary information
		add(new ConstantNumberMacro("maxSize", "The maximum size of the arrays sorted in the experiments", max_length));
		add(new ConstantNumberMacro("numAlgos", "The number of algorithms compared in this lab", 4));
		add(new SlowestMacro());
	}
	
	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, SortingLab.class);
	}
	
	/**
	 * This macro finds the name of the sorting algorithm with the slowest
	 * sorting time
	 */
	protected class SlowestMacro extends Macro
	{
		public SlowestMacro()
		{
			super("slowestAlgo", "The name of the slowest sorting algorithm");
		}
		
		@Override
		public JsonElement getValue()
		{
			float longest_time = 0f;
			String algo_name = "None";
			for (Experiment e : getExperiments())
			{
				float time = e.readFloat("time");
				if (time > longest_time)
				{
					longest_time = time;
					algo_name = e.readString("name");
				}
			}
			return new JsonString(algo_name);
		}
	}
}
