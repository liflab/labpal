package browser;

import ca.uqac.lif.labpal.Experiment;

public class BrowserExperiment extends Experiment
{
	BrowserExperiment()
	{
		super();
	}
	
	public BrowserExperiment(String browser, String name, float value)
	{
		super(Status.DONE);
		setInput("browser", browser);
		write("market", name);
		write("share", value);
	}

	@Override
	public void execute()
	{
		// Nothing to do
	}

}
