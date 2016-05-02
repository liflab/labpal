package browser;

import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.parkbench.Experiment;

public class BrowserExperiment extends Experiment
{
	float value;
	String name;
	
	public BrowserExperiment(String browser, String name, float value)
	{
		super();
		setInput("browser", browser);
		this.name = name;
		this.value = value;
	}

	@Override
	public Status execute(JsonMap input, JsonMap output)
	{
		write("market", name);
		write("share", value);
		return Status.DONE;
	}

}
