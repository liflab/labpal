package ca.uqac.lif.parkbench.table;

import java.util.Vector;

public abstract class TabularTransform extends Table
{
	protected Table m_inputTable;
	
	public TabularTransform(Table t)
	{
		super();
		m_inputTable = t;
	}
	
	@Override
	public Vector<String> getXValues()
	{
		return getTabular().getXValues();
	}
}

