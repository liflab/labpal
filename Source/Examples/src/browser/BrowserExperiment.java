package browser;

import ca.uqac.lif.parkbench.Experiment;

public class BrowserExperiment extends Experiment
{
	float value;
	String name;
	
	BrowserExperiment()
	{
		super();
	}
	
	public BrowserExperiment(String browser, String name, float value)
	{
		super();
		setInput("browser", browser);
		this.name = name;
		this.value = value;
	}

	@Override
	public Status execute()
	{
		write("market", name);
		write("share", value);
		return Status.DONE;
	}

}
