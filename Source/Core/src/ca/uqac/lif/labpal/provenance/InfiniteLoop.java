package ca.uqac.lif.labpal.provenance;

public class InfiniteLoop extends ProvenanceNode
{
	public static final InfiniteLoop instance = new InfiniteLoop();
	
	private InfiniteLoop() 
	{
		super(null);
	}
	
	@Override
	public String toString()
	{
		return "Infinite loop detected";
	}

}
