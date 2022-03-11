package examples;

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

/**
 * A minimal example illustrating how to create experiments, add them to a lab,
 * and create tables and plots out of their results. The lab deliberately uses
 * all available notational shortcuts, does not contain any comment and does
 * not override any default value to produce a non-trivial working lab with the
 * fewest possible lines of code.
 * <p>
 * A commented and extended version of this lab, illustrating more features of
 * the library, can be found at {@link FoobarExtended}.
 * 
 * @see FoobarExtended
 */
public class FoobarDemo
{
	public static class MyLab extends Laboratory
	{
		public void setup()	{
			ExperimentFactory<?> f = new ExperimentFactory<>(this, RunTool.class);

			Region pr = filter(product(range("abc", 0, 10), range("def", 0, 10),
					extension("tool", "Foobar", "Foobaz")),
					(p) -> {
						return p.getInt("abc") % 2 == 0 || p.getInt("abc") >= p.getInt("def");});

			for (String[] x : permute("abc", "def"))
				for (Region r : pr.all(x[0]))
					add(new Plot(
							add(transform(table(x[1], "tool", "duration").add(f, r)
									.setTitle("Impact of " + x[1] + " with ", r), 
									new ExpandAsColumns("tool", "duration"), new Sort().by(0))),
							new GnuplotScatterplot()));
		}

		public static class RunTool extends Experiment {
			public void execute() {
				Stopwatch.start(this);
				// Simulate the execution of "Foobar" and "Foobaz" by waiting 
				if (readString("tool").matches("Foobar"))
					Stopwatch.sleep((long) (Math.random() * 100 * readFloat("abc")));
				else
					Stopwatch.sleep((long) (Math.random() * 150 * readFloat("def")));
				writeOutput("duration", Stopwatch.stop(this));
			}
		}

		public static void main(String[] args) {
			System.exit(initialize(args, MyLab.class));
		}
	}
}