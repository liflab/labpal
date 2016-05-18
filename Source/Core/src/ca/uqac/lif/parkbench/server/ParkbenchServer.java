/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.parkbench.server;

import ca.uqac.lif.jerrydog.InnerFileServer;
import ca.uqac.lif.jerrydog.RequestCallback;
import ca.uqac.lif.parkbench.CliParser;
import ca.uqac.lif.parkbench.CliParser.ArgumentMap;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

/**
 * Server supporting ParkBench's web GUI
 *  
 * @author Sylvain Hallé
 *
 */
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
		registerCallback(0, new StatusPageCallback(lab, assistant));
		registerCallback(0, new HomePageCallback(lab, assistant));
		registerCallback(0, new ExperimentPageCallback(lab, assistant));
		registerCallback(0, new ExperimentsPageCallback(lab, assistant));
		registerCallback(0, new AssistantPageCallback(lab, assistant));
		registerCallback(0, new PlotsPageCallback(lab, assistant));
		registerCallback(0, new PlotImageCallback(lab, assistant));
		registerCallback(0, new DownloadCallback(lab, assistant));
		registerCallback(0, new UploadCallback(this, lab, assistant));
		registerCallback(0, new HelpPageCallback(lab, assistant));
		registerCallback(0, new AllPlotsCallback(lab, assistant));
		registerCallback(0, new TablesPageCallback(lab, assistant));
		registerCallback(0, new TablePageCallback(lab, assistant));
	}
	
	/**
	 * Changes the laboratory associated with each registered callback.
	 * This occurs when the user loads a new lab from a file.
	 * @param lab The new laboratory
	 */
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
