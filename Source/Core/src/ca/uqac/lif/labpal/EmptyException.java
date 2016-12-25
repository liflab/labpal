package ca.uqac.lif.labpal;

public class EmptyException extends Exception
{
	/**
	 * Dummy UID
	 */
	private static final long serialVersionUID = 1L;
	
	private final String m_message;
	
	public EmptyException()
	{
		super();
		m_message = "";
	}
	
	public EmptyException(String message)
	{
		super();
		m_message = message;
	}
	
	@Override
	public String getMessage()
	{
		return m_message;
	}

}
