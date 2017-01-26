package ca.uqac.lif.labpal.provenance;

import java.util.Collection;

public class DirectValue extends AggregateFunction 
{
	public DirectValue(Collection<NodeFunction> nodes)
	{
		super(getCaption(nodes.size()), nodes);
	}
	
	public DirectValue(NodeFunction ... nodes)
	{
		super(getCaption(nodes.length), nodes);
	}
	
	public void add(NodeFunction node)
	{
		m_nodes.add(node);
	}
	
	protected static String getCaption(int n)
	{
		if (n > 1)
		{
			return "The values of";
		}
		return "The value of";
	}	
}
