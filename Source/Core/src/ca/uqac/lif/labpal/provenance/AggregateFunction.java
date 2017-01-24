package ca.uqac.lif.labpal.provenance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uqac.lif.labpal.Laboratory;

public class AggregateFunction implements NodeFunction
{
	/**
	 * The list of nodes the function is applied on
	 */
	protected final List<NodeFunction> m_nodes;
	
	/**
	 * The name of the aggregate function
	 */
	protected final String m_name;
	
	public AggregateFunction(String name, Collection<NodeFunction> nodes)
	{
		super();
		m_name = name;
		m_nodes =  new ArrayList<NodeFunction>(nodes.size());
		m_nodes.addAll(nodes);
	}
	
	public AggregateFunction(String name, NodeFunction ... nodes)
	{
		super();
		m_name = name;
		m_nodes =  new ArrayList<NodeFunction>(nodes.length);
		for (NodeFunction nf : nodes)
		{
			m_nodes.add(nf);
		}
	}
	
	@Override
	public String toString()
	{
		return m_name;
	}

	@Override
	public String getDataPointId()
	{
		return "";
	}

	public Object getOwner(Laboratory lab, String datapoint_id)
	{
		return null;
	}

	@Override
	public NodeFunction dependsOn() 
	{
		return this;
	}
}
