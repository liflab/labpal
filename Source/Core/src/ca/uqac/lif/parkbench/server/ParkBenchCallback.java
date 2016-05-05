package ca.uqac.lif.parkbench.server;

import ca.uqac.lif.jerrydog.RequestCallback;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public abstract class ParkBenchCallback extends RequestCallback
{
	protected Laboratory m_lab;
	
	protected LabAssistant m_assistant;
	
	public ParkBenchCallback(Laboratory lab, LabAssistant assistant)
	{
		super();
		m_lab = lab;
		m_assistant = assistant;
	}
}
