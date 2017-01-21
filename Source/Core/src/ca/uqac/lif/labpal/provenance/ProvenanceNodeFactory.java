package ca.uqac.lif.labpal.provenance;

public class ProvenanceNodeFactory
{
	protected int m_idCounter = 0;

	public ProvenanceNodeFactory()
	{
		super();
	}

	public ProvenanceNode newNode(String id, DataOwner owner)
	{
		int node_id = m_idCounter++;
		if (id.startsWith("T"))
		{
			return new TableProvenanceNode(node_id, id, owner);
		}
		if (id.startsWith("E"))
		{
			return new ExperimentProvenanceNode(node_id, id, owner);
		}
		return new ProvenanceNode(node_id, id, owner);
	}
}
