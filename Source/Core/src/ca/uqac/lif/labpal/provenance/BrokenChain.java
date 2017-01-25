package ca.uqac.lif.labpal.provenance;

public class BrokenChain extends ProvenanceNode
{
	public static final BrokenChain instance = new BrokenChain();
	
	private BrokenChain() 
	{
		super(null);
	}
	
	@Override
	public String toString()
	{
		return "Broken chain";
	}

}
