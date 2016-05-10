package ca.uqac.lif.parkbench.server;

import ca.uqac.lif.jerrydog.RestCallback;
import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;

public abstract class ParkBenchCallback extends RestCallback
{
	protected Laboratory m_lab;
	
	protected LabAssistant m_assistant;
	
	public ParkBenchCallback(String path, Laboratory lab, LabAssistant assistant)
	{
		super(Method.GET, path);
		m_lab = lab;
		m_assistant = assistant;
	}
	
	/**
	 * Changes the laboratory associated with this callback
	 * @param lab The new laboratory
	 */
	public void changeLab(Laboratory lab)
	{
		m_lab = lab;
	}
}
