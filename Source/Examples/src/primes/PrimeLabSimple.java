package primes;

import java.util.List;

import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.labpal.plot.gral.Scatterplot;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.labpal.table.ExperimentTable;

/**
 * Compares various methods for checking if a number is prime.
 */
public class PrimeLabSimple extends Laboratory
{
	@Override
	public void setupExperiments(ArgumentMap map, List<WebCallback> callbacks)
	{
		ExperimentTable table = new ExperimentTable("Number", "Duration");
		add(table);
		for (long i : new long[]{13l, 89l, 233l, 1597l, 28657l, 514229l, 2147483647l})
		{
			TrialDivision exp_td = new TrialDivision(i);
			add(exp_td);
			table.add(exp_td);
		}
		Scatterplot plot = new Scatterplot(table);
		plot.setLogscale(Axis.X);
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
		public Status execute()
		{
			long time_start = System.nanoTime();
			checkForPrime(readLong("Number"));
			long time_end = System.nanoTime();
			write("Duration", time_end - time_start);
			return Status.DONE;
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
