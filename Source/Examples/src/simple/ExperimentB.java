package simple;

import ca.uqac.lif.parkbench.Experiment;

public class ExperimentB extends Experiment
{
	public ExperimentB()
	{
		super();
	}
	
	public ExperimentB(int a)
	{
		this();
		setInput("name", "Experiment B");
		setInput("a", a);
		setDescription("Simply returns as its output a linear function of its input parameter 'a'");
	}

	@Override
	public Status execute()
	{
		int a = readInt("a");
		for (int i = 0; i < 10; i++)
		{
			Experiment.wait(1000);
			setProgression(((float) i) / 10f);
		}
		write("y", a * 3 + 1);
		return Status.DONE;
	}
	
	@Override
	public float getDurationEstimate(float factor)
	{
		return 1f;
	}
	
	@Override
	public String toString()
	{
		return "B a=" + readInt("a");
	}
}
