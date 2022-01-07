package primes;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.labpal.Claim;
import ca.uqac.lif.labpal.CliParser;
import ca.uqac.lif.labpal.CliParser.Argument;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;
import ca.uqac.lif.labpal.ExperimentValue;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.LabPalGnuplot;
import ca.uqac.lif.labpal.provenance.TrackedValue;
import ca.uqac.lif.spreadsheet.functions.ExpandAsColumns;
import ca.uqac.lif.spreadsheet.plot.Plot.Axis;
import ca.uqac.lif.spreadsheet.plots.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.labpal.table.TransformedTable;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;

/**
 * Compares various methods for checking if a number is prime.
 */
public class PrimeLabFull extends Laboratory
{
	@Override
	public void setupCli(CliParser parser)
	{
		// Adds two command-line arguments to define the interval of numbers
		parser.addArgument(new Argument().withLongName("start").withArgument("n")
				.withDescription("Start at number n"));
		parser.addArgument(new Argument().withLongName("stop").withArgument("n")
				.withDescription("Stop at number n"));
		parser.addArgument(new Argument().withLongName("step").withArgument("n")
				.withDescription("Increment by n (default: 2000)"));
	}

	@Override
	public void setup()
	{
		// Set default bounds, read command-line arguments to override defaults
		ArgumentMap map = getCliArguments();
		long start_number = 1000, stop_number = 10000, step = 2000;
		if (map.hasOption("start"))
			start_number = Long.parseLong(map.getOptionValue("start"));
		if (map.hasOption("stop"))
			stop_number = Long.parseLong(map.getOptionValue("stop"));
		if (map.hasOption("step"))
			step = Long.parseLong(map.getOptionValue("step"));
		// Print status
		System.out.println("Ranging from " + start_number + " to " + stop_number 
				+ " by steps of " + step);

		// Lab metadata
		setTitle("Comparison of primality tests");
		setAuthor("Emmett Brown");
		setDescription("This lab compares various methods for checking if a number is prime.");

		// Create a table
		ExperimentTable table = new ExperimentTable("Method", "Number", "Duration");
		table.setTitle("Comparison of primality tests");
		add(table);

		// Create and add experiments
		TrialDivision exp_td = new TrialDivision(start_number, stop_number, step);
		add(exp_td);
		table.add(exp_td);
		WilsonTheorem exp_wt = new WilsonTheorem(start_number, stop_number, step);
		add(exp_wt);
		table.add(exp_wt);
		EratosthenesSieve exp_es = new EratosthenesSieve(start_number, stop_number, step);
		add(exp_es);
		table.add(exp_es);
		
		// Add claims for each algorithm
		add(new MonotonicIncreaseClaim(exp_td));
		add(new MonotonicIncreaseClaim(exp_wt));
		add(new MonotonicIncreaseClaim(exp_es));

		// Create a plot, performing a transformation of the table before
		TransformedTable t_table = new TransformedTable(new ExpandAsColumns("Method", "Duration"), table);
		LabPalGnuplot plot = new LabPalGnuplot(t_table, new GnuplotScatterplot()
				.setLogscale(Axis.X).setCaption(Axis.Y, "Duration (us)"));
		add(plot);
	}

	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, PrimeLabFull.class);
	}

	public static abstract class PrimeExperiment extends Experiment
	{
		// The bounds of the interval
		protected long m_startNumber;
		protected long m_stopNumber;
		protected long m_step;

		// Define constants instead of hard-coding parameter names
		public static final String METHOD = "Method";
		public static final String NUMBER = "Number";
		public static final String DURATION = "Duration";
		
		protected PrimeExperiment()
		{
			super();
		}

		public PrimeExperiment(long start_number, long stop_number, long step)
		{
			super();
			m_startNumber = start_number;
			m_stopNumber = stop_number;
			m_step = step;
			// Create two lists: one to hold each number, the other to hold
			// the time taken for each number
			JsonList list_numbers = new JsonList();
			JsonList list_times = new JsonList();
			for (long n = m_startNumber; n <= m_stopNumber; n += m_step)
			{
				list_numbers.add(n);
				list_times.add(JsonNull.instance);
			}
			write(NUMBER, list_numbers);
			write(DURATION, list_times);
			// Give a textual description of each parameter
			describe(NUMBER, "The number to check for primality");
			describe(DURATION, "The duration of the operation, in nanoseconds");
			describe(METHOD, "The algorithm used to check if the number is prime");
		}

		@Override
		public void execute() throws ExperimentException
		{
			int index = 0;
			for (long n = m_startNumber; n <= m_stopNumber; n += m_step)
			{
				setProgression((n - m_startNumber) / (m_stopNumber - m_startNumber));
				long time_start = System.nanoTime();
				checkForPrime(n);
				long time_end = System.nanoTime();
				JsonList durations = readList(DURATION);
				durations.set(index, new JsonNumber(time_end - time_start));
				index++;
			}
		}

		public abstract boolean checkForPrime(long number) throws ExperimentException;		
	}

	/**
	 * Checks if a number is prime using trial division.
	 */
	public static class TrialDivision extends PrimeExperiment
	{
		protected TrialDivision()
		{
			super();
		}
		
		public TrialDivision(long start_number, long stop_number, long step)
		{
			super(start_number, stop_number, step);
			setInput(METHOD, "Trial Division");
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
		protected WilsonTheorem()
		{
			super();
		}
		
		public WilsonTheorem(long start_number, long stop_number, long step)
		{
			super(start_number, stop_number, step);
			setInput(METHOD, "Wilson's theorem");
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
		protected EratosthenesSieve()
		{
			super();
		}
		
		public EratosthenesSieve(long start_number, long stop_number, long step)
		{
			super(start_number, stop_number, step);
			setInput(METHOD, "Sieve of Eratosthenes");
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

	/**
	 * Claims that the time required for primality check increases
	 * monotonically with the magnitude of the number
	 */
	public static class MonotonicIncreaseClaim extends Claim
	{
		protected PrimeExperiment m_experiment;

		public MonotonicIncreaseClaim(PrimeExperiment experiment)
		{
			super();
			setName("Monotonic time for " + experiment.readString(PrimeExperiment.METHOD));
			setDescription("Asserts that the time required to check the primality of a number using the " + experiment.readString(PrimeExperiment.METHOD) + " increases monotonically with the number's magnitude.");
			m_experiment = experiment;
		}

		@Override
		public Result verify(Laboratory lab)
		{
			Result r = Result.OK;
			JsonList list = (JsonList) m_experiment.read(PrimeExperiment.DURATION);
			long current = -1, last = -1;
			int pos = 0;
			for (JsonElement e : list)
			{
				if (e instanceof JsonNull)
				{
					current = -1;
					last = -1;
					continue;
				}
				long v = ((JsonNumber) e).numberValue().longValue();
				if (last == -1)
				{
					last = v;
				}
				else
				{
					last = current;
					current = v;
					if (current < last)
					{
						r = Result.WARNING;
						Explanation exp = new Explanation("The time to check primality of number at position " + pos + " is smaller than the time to check primality of number at position " + (pos-1));
						exp.add(new TrackedValue(null, ComposedPart.compose(new NthElement(pos - 1), new ExperimentValue(PrimeExperiment.DURATION)), m_experiment));
						exp.add(new TrackedValue(null, ComposedPart.compose(new NthElement(pos), new ExperimentValue(PrimeExperiment.DURATION)), m_experiment));
						addExplanation(exp);
					}
				}
				pos++;
			}
			return r;
		}
	}
}
