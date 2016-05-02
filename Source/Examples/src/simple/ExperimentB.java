package simple;

import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonPath;
import ca.uqac.lif.parkbench.Experiment;

public class ExperimentB extends Experiment
{
	public ExperimentB(int a)
	{
		super();
		setInput("name", "Experiment B");
		setInput("a", a);
	}

	@Override
	public Status execute(JsonMap input, JsonMap output)
	{
		int a = JsonPath.getNumber(input, "a").intValue();
		//Experiment.wait(1000);
		output.put("y", a * 3 + 1);
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
