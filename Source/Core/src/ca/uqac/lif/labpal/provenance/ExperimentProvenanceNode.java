package ca.uqac.lif.labpal.provenance;

public class ExperimentProvenanceNode extends ProvenanceNode
{
	public ExperimentProvenanceNode(int id, String datapoint_id, DataOwner owner)
	{
		super(id, datapoint_id, owner);
	}

	@Override
	public String toString()
	{
		String dpid = getDataPointId();
		String[] parts = dpid.split(":");
		return "Value of " + parts[1] + " in experiment #" + parts[0].substring(1);
	}
}
