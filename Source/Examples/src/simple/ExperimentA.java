package simple;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;

public class ExperimentA extends Experiment
{
	ExperimentA()
	{
		super();
	}
	
	public ExperimentA(int a)
	{
		this();
		setInput("name", "Experiment A");
		setInput("a", a);
		setDescription("Simply returns as its output the value of its input parameter 'a'");
	}

	@Override
	public void execute() throws ExperimentException
	{
		int a = readInt("a");
		if (a == 2)
		{
			// Just to test the "fail" case
			throw new ExperimentException("The experiment failed");
		}
		write("y", a * 2);
	}
	
	@Override
	public String toString()
	{
		return "A a=" + readInt("a");
	}

}
