package ca.uqac.lif.labpal.table;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.provenance.NodeFunction;

public class TableCellNode implements NodeFunction 
{
	protected final Table m_table;
	
	protected final int m_row;
	
	protected final int m_col;
	
	public TableCellNode(Table t, int row, int col)
	{
		super();
		m_table = t;
		m_row = row;
		m_col = col;
	}
	
	@Override
	public String toString()
	{
		return "Cell (" + m_row + "," + m_col + ") in Table #" + m_table.getId();
	}
	
	@Override
	public String getDataPointId()
	{
		return "T" + m_table.getId() + s_separator + m_row + s_separator + m_col;
	}
	
	@Override
	public NodeFunction dependsOn()
	{
		return m_table.getDependency(m_row, m_col);
	}
	
	public static NodeFunction dependsOn(Table t, String datapoint_id)
	{
		// Parse the datapoint ID and call the table on the extracted values
		String[] parts = datapoint_id.split(NodeFunction.s_separator);
		if (parts.length != 3)
		{
			// Invalid datapoint
			return null;
		}
		int id = Integer.parseInt(parts[0].substring(1).trim());
		if (id != t.getId())
		{
			// Wrong table
			return null;
		}
		int row = Integer.parseInt(parts[1].trim());
		int col = Integer.parseInt(parts[2].trim());
		return t.dependsOn(row, col);
	}
	
	/**
	 * Gets the owner of a datapoint
	 * @param lab
	 * @param datapoint_id
	 * @return The owner, or {@code null} if this object could not
	 * find the owner
	 */
	public static Table getOwner(Laboratory lab, String datapoint_id)
	{
		if (!datapoint_id.startsWith("T"))
			return null;
		String[] parts = datapoint_id.split(NodeFunction.s_separator);
		int id = Integer.parseInt(parts[0].substring(1).trim());
		return lab.getTable(id);
	}
	
	public Table getOwner()
	{
		return m_table;
	}
	
	public int getRow()
	{
		return m_row;
	}
	
	public int getCol()
	{
		return m_col;
	}
	
	@Override
	public int hashCode()
	{
		return m_table.getId() + m_row + m_col;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof TableCellNode))
		{
			return false;
		}
		TableCellNode tcn = (TableCellNode) o;
		return tcn.m_table.getId() == m_table.getId() &&
				tcn.m_col == m_col && tcn.m_row == m_row;
	}
}
