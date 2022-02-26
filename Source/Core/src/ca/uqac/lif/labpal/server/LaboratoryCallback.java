package ca.uqac.lif.labpal.server;

import ca.uqac.lif.jerrydog.RestCallback;

public abstract class LaboratoryCallback extends RestCallback
{
	protected LabPalServer m_server;
	
	public LaboratoryCallback(LabPalServer s, Method m, String path)
	{
		super(m, path);
		m_server = s;
	}
}
