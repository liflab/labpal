package ca.uqac.lif.labpal.table;

import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.spreadsheet.Spreadsheet;

public class TransformedTable extends Table
{
	/**
	 * The function to apply to the input spreadsheet
	 */
	protected Function m_transformation;
	
	protected Table[] m_inputTables;
	
	public TransformedTable(Function f, Table ... tables)
	{
		super();
		m_transformation = f;
		m_inputTables = tables;
	}
	
	@Override
	public Spreadsheet getSpreadsheet()
	{
		Object[] ins = new Object[m_inputTables.length];
		for (int i = 0; i < m_inputTables.length; i++)
		{
			ins[i] = m_inputTables[i].getSpreadsheet();
		}
		Object[] out = m_transformation.evaluate(ins);
		if (!(out[0] instanceof Spreadsheet))
		{
			return null;
		}
		return (Spreadsheet) out[0];
	}

	@Override
	public Table dependsOn(int col, int row)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
