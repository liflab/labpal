package simple;

import ca.uqac.lif.labpal.Experiment;

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
	public Status execute()
	{
		int a = readInt("a");
		if (a == 2)
		{
			// Just to test the "fail" case
			setErrorMessage("The experiment failed");
			return Status.FAILED;
		}
		write("y", a * 2);
		return Status.DONE;
	}
	
	@Override
	public String toString()
	{
		return "A a=" + readInt("a");
	}

}
