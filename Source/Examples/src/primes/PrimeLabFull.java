package primes;

import java.util.List;

import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.labpal.plot.gral.Scatterplot;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.labpal.table.ExpandAsColumns;
import ca.uqac.lif.labpal.table.ExperimentTable;

/**
 * Compares various methods for checking if a number is prime.
 */
public class PrimeLabFull extends Laboratory
{
	@Override
	public void setupExperiments(ArgumentMap map, List<WebCallback> callbacks)
	{
		setTitle("Comparison of primality tests");
		ExperimentTable table = new ExperimentTable("Method", "Number", "Duration");
		table.setTitle("Comparison of primality tests");
		add(table);
		for (long i : new long[]{13l, 89l, 233l, 1597l, 28657l, 514229l, 2147483647l})
		{
			TrialDivision exp_td = new TrialDivision(i);
			add(exp_td);
			table.add(exp_td);
			/*WilsonTheorem exp_wt = new WilsonTheorem(i);
			add(exp_wt);
			table.add(exp_wt);*/
			EratosthenesSieve exp_es = new EratosthenesSieve(i);
			add(exp_es);
			table.add(exp_es);
		}
		Scatterplot plot = new Scatterplot(table, new ExpandAsColumns("Method", "Duration"));
		plot.setLogscale(Axis.X);
		add(plot);
	}
	
	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, PrimeLabFull.class);
	}
	
	public static abstract class PrimeExperiment extends Experiment
	{
		public PrimeExperiment(long number)
		{
			super();
			setInput("Number", number);
		}

		@Override
		public Status execute() throws ExperimentException
		{
			long time_start = System.nanoTime();
			checkForPrime(readLong("Number"));
			long time_end = System.nanoTime();
			write("Duration", time_end - time_start);
			return Status.DONE;
		}
		
		public abstract boolean checkForPrime(long number) throws ExperimentException;		
	}
	
	/**
	 * Checks if a number is prime using trial division.
	 */
	public static class TrialDivision extends PrimeExperiment
	{
		public TrialDivision(long number)
		{
			super(number);
			setInput("Method", "Trial Division");
			setDescription("Checks if a number is prime using trial division.");
		}

		@Override
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
	
	/**
	 * Checks if a number is prime using Wilson's theorem. The theorem states
	 * that <i>p</i> is prime if and only if
	 * (<i>p</i>-1)! % <i>p</i> = <i>p</i>-1.
	 */
	public static class WilsonTheorem extends PrimeExperiment
	{
		public WilsonTheorem(long number)
		{
			super(number);
			setInput("Method", "Wilson's theorem");
			setDescription("Checks if a number is prime using Wilson's theorem. "
					+ " The theorem states that <i>p</i> is prime if and only if "
					+ "(<i>p</i>-1)! % <i>p</i> = <i>p</i>-1.");
		}

		@Override
		public boolean checkForPrime(long n) throws ExperimentException
		{
			long fac = 1;
			for (long i = 2; i < n - 1; i++)
			{
				fac *= i;
				if (fac < 0 || fac == Long.MAX_VALUE)
				{
					// Overflow: fail
					throw new ExperimentException(
							"An integer overflow was obtained while computing the factorial of " + n);
				}
			}
			boolean is_prime = ((fac % n) == (n - 1));
			return is_prime;
		}
	}
	
	/**
	 * Checks if a number is prime using the Sieve of Erastothenes.
	 */
	public static class EratosthenesSieve extends PrimeExperiment
	{
		public EratosthenesSieve(long number)
		{
			super(number);
			setInput("Method", "Eratosthenes' sieve");
			setDescription("Checks if a number is prime using the Sieve of Erastothenes.");
		}

		@Override
		public boolean checkForPrime(long n)
		{
			 if (n <= 1)
				 return false;
			 else if (n <= 3)
				 return true;
			 else if (n % 2 == 0 || n % 3 == 0)
				 return false;
			 long i = 5;
			 while (i*i < n)
			 {
				 if (n % i == 0 || n % (i + 2) == 0)
					 return false;
				 i += 6;
			 }
			 return true;		
		}
	}
}
