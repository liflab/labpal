package ca.uqac.lif.labpal.macro;

public class ConstantNumberMacro extends NumberMacro
{
	protected Number m_value;

	public ConstantNumberMacro(String name, String description, Number value) 
	{
		super(name, description);
		m_value = value;
	}
	
	@Override
	public final Number getNumber()
	{
		return m_value;
	}

}
