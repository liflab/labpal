package simple;

import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonPath;
import ca.uqac.lif.parkbench.Experiment;

public class ExperimentA extends Experiment
{
	public ExperimentA(int a)
	{
		super();
		JsonMap params = new JsonMap();
		params.put("name", "Experiment A");
		params.put("a", a);
		setInputParameters(params);
	}

	@Override
	public Status execute(JsonMap input, JsonMap output)
	{
		int a = JsonPath.getNumber(input, "a").intValue();
		if (a == 2)
		{
			// Just to test the "fail" case
			setErrorMessage("The experiment failed");
			return Status.FAILED;
		}
		output.put("y", a * 2);
		return Status.DONE;
	}
	
	@Override
	public String toString()
	{
		return "A a=" + JsonPath.getNumber(getInputParameters(), "a").intValue();
	}

}
