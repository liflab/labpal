package ca.uqac.lif.labpal.provenance;

public class UndefinedDependency implements NodeFunction
{
	public static final UndefinedDependency instance = new UndefinedDependency();

	private UndefinedDependency() 
	{
		super();
	}

	@Override
	public String toString()
	{
		return "Undefined dependency";
	}

	@Override
	public String getDataPointId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeFunction dependsOn()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
