package ca.uqac.lif.parkbench.table;

import java.util.Vector;

import ca.uqac.lif.parkbench.Laboratory;

public abstract class Table 
{
	/**
	 * The table's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing table IDs
	 */
	protected static int s_idCounter = 1;
	
	/**
	 * The laboratory this plot is assigned to
	 */
	protected Laboratory m_lab;
	
	public Table()
	{
		super();
		m_id = s_idCounter++;
	}
	
	/**
	 * Gets the table's ID
	 * @return The ID
	 */
	public Integer getId()
	{
		return m_id;
	}
	
	public abstract Vector<String> getXValues();
	
	public abstract Tabular getTabular();
}
