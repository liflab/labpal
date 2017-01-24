package ca.uqac.lif.labpal.provenance;

public class BrokenChain extends ProvenanceNode
{
	public BrokenChain() 
	{
		super(null);
	}
	
	@Override
	public String toString()
	{
		return "Broken chain";
	}

}
