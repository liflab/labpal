package ca.uqac.lif.labpal;

public class EmptyException extends Exception
{
	/**
	 * Dummy UID
	 */
	private static final long serialVersionUID = 1L;
	
	public EmptyException()
	{
		super();
	}
	
	public EmptyException(String message)
	{
		super(message);
	}
	
	public EmptyException(Throwable t)
	{
		super(t);
	}
}