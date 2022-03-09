package ca.uqac.lif.labpal.server;

import static ca.uqac.lif.labpal.region.ConditionalRegion.filter;
import static ca.uqac.lif.labpal.region.DiscreteRange.range;
import static ca.uqac.lif.labpal.region.ExtensionDomain.extension;
import static ca.uqac.lif.labpal.region.ProductRegion.product;
import static ca.uqac.lif.labpal.table.ExperimentTable.table;
import static ca.uqac.lif.labpal.table.TransformedTable.transform;
import static ca.uqac.lif.labpal.util.PermutationIterator.permute;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.region.Region;

import ca.uqac.lif.labpal.util.Stopwatch;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.spreadsheet.functions.ExpandAsColumns;
import ca.uqac.lif.spreadsheet.functions.Sort;

public class FoobarDemo
{
	public static class MyLab extends Laboratory
	{
		public void setup()
		{
			ExperimentFactory<?> f = new ExperimentFactory<>(this, RunTool.class);

			Region pr = filter(product(range("abc", 0, 5), range("def", 0, 5),
					extension("tool", "Foobar", "Foobaz")),
					(p) -> {
						return p.getInt("abc") % 2 == 0 || p.getInt("abc") > p.getInt("def");});
			
			for (String[] x : permute("abc", "def"))
				for (Region r : pr.all(x[0]))
					add(new Plot(
							add(transform(table(x[1], "tool", "duration").add(f, r).setTitle(r), 
									new ExpandAsColumns("tool", "duration"), new Sort().by(0))),
							new GnuplotScatterplot()).setTitle("Impact of " + x[1] + " with "));
		}
		
		public static class RunTool extends Experiment
		{
			public void execute()
			{
				Stopwatch.start(this);
				if (readString("tool").matches("Foobar"))
				{
					// Run Foobar
					Stopwatch.sleep(10000);
				}
				else
				{
					// Run Foobaz
				}
				writeOutput("duration", Stopwatch.stop(this));
			}
		}
		
		public static void main(String[] args)
		{
			System.exit(initialize(args, MyLab.class));
		}
	}
}
