package ca.uqac.lif.labpal.table;

import java.util.HashMap;

/**
 * An entry in a data table
 */
public class TableEntry extends HashMap<String,Object>
{
	/**
	 * Dummy UID
	 */
	private static final transient long serialVersionUID = 1L;
	
	@Override
	public int hashCode()
	{
		int x = 0;
		for (Object o : values())
		{
			if (o != null)
			{
				x += o.hashCode();
			}
		}
		return x;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof TableEntry))
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}
		TableEntry te = (TableEntry) o;
		if (size() != te.size())
		{
			return false;
		}
		for (Entry<String,Object> entry : entrySet())
		{
			if (!te.get(entry.getKey()).equals(entry.getValue()))
			{
				return false;
			}
		}
		return true;
	}
}
