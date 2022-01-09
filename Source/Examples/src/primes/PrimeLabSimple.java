package primes;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.spreadsheet.chart.Chart.Axis;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.labpal.table.ExperimentTable;

/**
 * Compares various methods for checking if a number is prime.
 */
public class PrimeLabSimple extends Laboratory
{
	@Override
	public void setup()
	{
		ExperimentTable table = new ExperimentTable("Number", "Duration");
		add(table);
		for (long i : new long[]{13l, 89l, 233l, 1597l, 28657l, 514229l, 2147483647l})
		{
			TrialDivision exp_td = new TrialDivision(i);
			add(exp_td);
			table.add(exp_td);
		}
		Plot plot = new Plot(table, new GnuplotScatterplot().setLogscale(Axis.X));
		add(plot);
	}
	
	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, PrimeLabSimple.class);
	}
		
	/**
	 * Checks if a number is prime using trial division.
	 */
	public static class TrialDivision extends Experiment
	{
		public TrialDivision(long number)
		{
			setInput("Number", number);
		}
		
		@Override
		public void execute()
		{
			long time_start = System.nanoTime();
			checkForPrime(readLong("Number"));
			long time_end = System.nanoTime();
			write("Duration", time_end - time_start);
		}

		public boolean checkForPrime(long n)
		{
			for (long d = 2; d <= Math.sqrt(n); d++)
			{
				if ((n / d) % 1 == 0)
				{
					return true;
				}
			}
			return false;
		}
	}	
}
