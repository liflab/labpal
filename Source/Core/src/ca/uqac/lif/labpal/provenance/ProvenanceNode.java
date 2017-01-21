package ca.uqac.lif.labpal.provenance;

import java.util.HashSet;
import java.util.Set;

public class ProvenanceNode
{
	/**
	 * The owner of the data point
	 */
	protected final DataOwner m_owner;
	
	/**
	 * The ID of the data point
	 */
	protected final String m_dataPointId;
	
	/**
	 * The ID of the node
	 */
	protected final int m_id;
	
	/**
	 * A set of points this data point depends on
	 */
	protected final Set<ProvenanceNode> m_parents;
	
	/**
	 * Creates a new provenance node
	 * @param id The node's ID
	 * @param datapoint_id The ID of the data point
	 * @param owner The owner of the data point
	 */
	public ProvenanceNode(int id, String datapoint_id, DataOwner owner)
	{
		super();
		m_id = id;
		m_owner = owner;
		m_dataPointId = datapoint_id;
		m_parents = new HashSet<ProvenanceNode>();
	}
	
	/**
	 * Adds a parent to this node
	 * @param p The parent
	 */
	public void addParent(ProvenanceNode p)
	{
		m_parents.add(p);
	}
	
	public String getDataPointId()
	{
		return m_dataPointId;
	}
	
	public Set<ProvenanceNode> getParents()
	{
		return m_parents;
	}
	
	@Override
	public String toString()
	{
		return m_dataPointId;
	}
}
