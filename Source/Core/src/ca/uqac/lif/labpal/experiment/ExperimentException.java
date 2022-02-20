package ca.uqac.lif.labpal.experiment;

public class ExperimentException extends Exception
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 2L;

	public ExperimentException(Throwable t)
	{
		super(t);
	}
	
	public ExperimentException(String message)
	{
		super(message);
	}
}
