package ca.uqac.lif.labpal;

public class ExperimentException extends Exception
{
	/**
	 * Dummy UID
	 */
	private static final long serialVersionUID = 1L;
	
	public ExperimentException()
	{
		super();
	}
	
	public ExperimentException(String message)
	{
		super(message);
	}
	
	public ExperimentException(Throwable t)
	{
		super(t);
	}


}
