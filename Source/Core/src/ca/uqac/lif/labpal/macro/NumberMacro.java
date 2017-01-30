package ca.uqac.lif.labpal.macro;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNumber;

public class NumberMacro extends MacroScalar
{
	public NumberMacro(String name, String description) 
	{
		super(name, description);
	}

	@Override
	public final JsonElement getValue()
	{
		Number n = getNumber();
		return new JsonNumber(n);
	}
	
	/**
	 * Gets the numerical value associated to this macro
	 * @return The value
	 */
	public Number getNumber()
	{
		return 0;
	}
}
