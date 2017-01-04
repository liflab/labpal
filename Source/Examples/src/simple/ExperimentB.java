package simple;

import ca.uqac.lif.labpal.Experiment;

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
	public void execute()
	{
		int a = readInt("a");
		for (int i = 0; i < 10; i++)
		{
			//Experiment.wait(1000);
			setProgression(((float) i) / 10f);
		}
		write("y", a * 3 + 1);
	}
	
	@Override
	public float getDurationEstimate(float factor)
	{
		// Since we wait 10 seconds no matter what
		return 10f;
	}
	
	@Override
	public String toString()
	{
		return "B a=" + readInt("a");
	}
}
