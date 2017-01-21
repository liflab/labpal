package ca.uqac.lif.labpal.provenance;


public class TableProvenanceNode extends ProvenanceNode
{
	public TableProvenanceNode(int id, String datapoint_id, DataOwner owner)
	{
		super(id, datapoint_id, owner);
	}

	@Override
	public String toString()
	{
		String dpid = getDataPointId();
		String[] parts = dpid.split(":");
		return "Value of cell (" + parts[1] + "," + parts[2] + ") in table #" + parts[0].substring(1);
	}
}
