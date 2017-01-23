package ca.uqac.lif.labpal.table;

import java.util.Collection;

import ca.uqac.lif.labpal.provenance.DataOwner;
import ca.uqac.lif.labpal.provenance.ProvenanceNode;

public class FunctionProvenanceNode extends ProvenanceNode 
{
	/**
	 * The description of the transformation
	 */
	protected final String m_description;
	
	public FunctionProvenanceNode(String datapoint_id, DataOwner owner, String description)
	{
		super(datapoint_id, owner);
		m_description = description;
	}
	
	public FunctionProvenanceNode(String datapoint_id, DataOwner owner, String description, Collection<ProvenanceNode> parents)
	{
		super(datapoint_id, owner);
		m_description = description;
		m_parents.addAll(parents);
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append(m_description).append(" ");
		/*boolean first = true;
		for (ProvenanceNode pn : getParents())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				out.append(", ");
			}
			out.append(pn);
		}*/
		return out.toString();
	}

}
