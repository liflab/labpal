/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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
package examples.sorting;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.region.DiscreteRange;
import ca.uqac.lif.labpal.region.ExtensionDomain;
import ca.uqac.lif.labpal.region.ProductRegion;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.TransformedTable;
import ca.uqac.lif.labpal.util.CliParser;
import ca.uqac.lif.labpal.util.CliParser.Argument;
import ca.uqac.lif.labpal.util.CliParser.ArgumentMap;
import ca.uqac.lif.spreadsheet.chart.Chart.Axis;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotHistogram;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.spreadsheet.functions.ColumnSum;
import ca.uqac.lif.spreadsheet.functions.ExpandAsColumns;

import static examples.sorting.SortExperiment.ALGORITHM;
import static examples.sorting.SortExperiment.DURATION;
import static examples.sorting.SortExperiment.SIZE;

/**
 * This is an example of a lab that creates experiments to compare
 * sorting algorithms.
 * 
 * @author Sylvain Hallé
 */
public class SortingLab extends Laboratory
{
	@Override
	public void setup()
	{
		/* Set basic metadata about the lab */
		setName("Sorting Algorithms");
		setAuthor("Fred Flintstone");
		setDoi("99.99999.9999"); // Fake DOI
		
		/* Define a default range of list lengths. */ 
		int min_length = 5000;
		int max_length = 30000;
		int increment = 5000;
		
		/* Override this range by user-supplied CLI arguments if any. */
		ArgumentMap args = getCliArguments();
		if (args.hasOption("min"))
			min_length = Integer.parseInt(args.getOptionValue("min"));
		if (args.hasOption("max"))
			max_length = Integer.parseInt(args.getOptionValue("max"));

		/* Create an experiment factory. */
		SortExperimentFactory f = new SortExperimentFactory(this);

		/* Define the global parameter space of the lab in a region. There are two
		 * input parameters: the sorting algorithm, and the list size. */
		Region global_r = new ProductRegion(
				new ExtensionDomain<String>(ALGORITHM, ShellSort.NAME,
						GnomeSort.NAME, BubbleSort.NAME, QuickSort.NAME, BadSort.NAME),
				new DiscreteRange(SIZE, min_length, max_length, increment));
		
		/* Associate experiments to a table */
		Table table = new ExperimentTable(SIZE, DURATION, ALGORITHM).add(f, global_r).setTitle("Comparison of sorting algorithms").setNickname("sorttime");

		/* Prepare a plot from the results of the table. To do so, we must first
		 * turn the pairs of cells algorithm/duration into one column for each
		 * algorithm. This is done with the ExpandAsColumns table transformation. */
		Table t_table = add(new TransformedTable(
				new ExpandAsColumns(ALGORITHM, DURATION), table))
				.setTitle("Sorting time per algorithm").setNickname("sorttimealg");
		add(new Plot(t_table, new GnuplotScatterplot()
				.setCaption(Axis.X, "List size").setCaption(Axis.Y, "Time (ms)")
				.withLines()).setNickname("sortplot"));
		
		/* From the same data, display results as a histogram instead of a
		 * scatterplot. */
		add(new Plot(t_table, new GnuplotHistogram()
				.setTitle("Sorting time for each array")
				.setCaption(Axis.X, "List size").setCaption(Axis.Y, "Time (ms)"))
				.setNickname("sorthisto"));

		/* Just for fun, create another histogram with the sum of all sorting times
		 * for each algorithm. We can do this with another table transformation
		 * called ColumnSum. */
		Table p_table = add(new TransformedTable(new ColumnSum(), t_table))
				.setTitle("Cumulative sorting time").setNickname("sumtime");
		add(new Plot(p_table, new GnuplotHistogram()
				.setCaption(Axis.X, "Total size").setCaption(Axis.Y, "Time (ms)")));

		// Create a few macros showing summary information
		/*add(new ConstantNumberMacro(this, "maxSize", "The maximum size of the arrays sorted in the experiments", max_length));
		add(new ConstantNumberMacro(this, "numAlgos", "The number of algorithms compared in this lab", 4));
		add(new SlowestMacro(this));*/
		
		/* Adds a claim */
		add(new IncreasingTime(f, global_r));
		add(new SlowerThan(GnomeSort.NAME, BubbleSort.NAME, f, global_r));
		add(new SlowerThan(QuickSort.NAME, ShellSort.NAME, f, global_r));
	}
	
	@Override
	public void setupCli(CliParser parser)
	{
		/* Add two custom CLI arguments to the lab, to set the minimum and maximum
		 * length of the lists to sort. */
		parser.addArgument(new Argument().withLongName("min")
				.withArgument("n").withDescription("Sets minimum list length to n"));
		parser.addArgument(new Argument().withLongName("max")
				.withArgument("n").withDescription("Sets maximum list length to n"));
	}

	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, SortingLab.class);
	}
}
