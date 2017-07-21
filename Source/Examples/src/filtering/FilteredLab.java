package filtering;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;
import ca.uqac.lif.labpal.ExperimentFilter;
import ca.uqac.lif.labpal.Laboratory;

public class FilteredLab extends Laboratory
{

	@Override
	public void setup()
	{
		for (int a = 0; a < 10; a++)
		{
			for (int b = 0; b < 10; b++)
			{
				add(new DummyExperiment(a, b));
			}
		}
	}
	
	/**
	 * A dummy experiment that does nothing, but has two input
	 * parameters, {@code a} and {@code b}
	 */
	public static class DummyExperiment extends Experiment
	{
		public DummyExperiment(int a, int b)
		{
			super();
			setInput("a", a);
			setInput("b", b);
		}

		@Override
		public void execute() throws ExperimentException
		{
			// Do nothing
		}
	}
	
	@Override
	public ExperimentFilter createFilter(String arguments)
	{
		return new MyFilter(arguments);
	}
	
	public static void main(String[] args)
	{
		// Nothing more to do here
		initialize(args, FilteredLab.class);
	}
}
