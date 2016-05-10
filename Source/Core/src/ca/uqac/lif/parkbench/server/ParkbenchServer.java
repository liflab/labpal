package ca.uqac.lif.parkbench.server;

import ca.uqac.lif.jerrydog.InnerFileServer;
import ca.uqac.lif.jerrydog.RequestCallback;
import ca.uqac.lif.parkbench.CliParser;
import ca.uqac.lif.parkbench.CliParser.ArgumentMap;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public class ParkbenchServer extends InnerFileServer
{
	public ParkbenchServer(ArgumentMap args, Laboratory lab, LabAssistant assistant)
	{
		super(ParkbenchServer.class);
		setUserAgent("ParkBench " + Laboratory.s_versionString);
		if (args.hasOption("port"))
		{
			setServerPort(Integer.parseInt(args.getOptionValue("port")));
		}
		else
		{
			setServerPort(21212);
		}
		registerCallback(0, new IndexPageCallback(lab, assistant));
		registerCallback(0, new ExperimentPageCallback(lab, assistant));
		registerCallback(0, new ExperimentsPageCallback(lab, assistant));
		registerCallback(0, new AssistantPageCallback(lab, assistant));
		registerCallback(0, new PlotsPageCallback(lab, assistant));
		registerCallback(0, new PlotImageCallback(lab, assistant));
		registerCallback(0, new DownloadCallback(lab, assistant));
		registerCallback(0, new UploadCallback(this, lab, assistant));
	}
	
	public void changeLab(Laboratory lab)
	{
		for (RequestCallback cb : m_callbacks)
		{
			if (cb instanceof ParkBenchCallback)
			{
				((ParkBenchCallback) cb).changeLab(lab);
			}
		}
	}
	
	public static void setupCli(CliParser parser)
	{
		
	}

}
