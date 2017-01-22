package ca.uqac.lif.labpal.table;

import ca.uqac.lif.labpal.provenance.ProvenanceNode;

public class TableCellProvenanceNode extends ProvenanceNode
{
	protected final int m_row;
	
	protected final int m_col;
	
	protected final int m_tableId;
	
	public TableCellProvenanceNode(Table t, int row, int col)
	{
		super("T" + t.getId() + ":" + row + ":" + col, t);
		m_row = row;
		m_col = col;
		m_tableId = t.getId();
	}
	
	@Override
	public String toString()
	{
		return "Cell (" + m_row + "," + m_col + ") of Table #" + m_tableId; 
	}
}
