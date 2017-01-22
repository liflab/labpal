package ca.uqac.lif.labpal.table.rendering;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.labpal.table.DataTable;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.Table.CellCoordinate;
import ca.uqac.lif.labpal.table.TableEntry;

public class PlainTableRenderer 
{
	/**
	 * The table to render
	 */
	protected Table m_table;
	
	/**
	 * A collection of cell coordinates that should be "highlighted" when
	 * displaying the table
	 */
	protected Set<CellCoordinate> m_cellsToHighlight;
	
	public PlainTableRenderer(Table t)
	{
		super();
		m_table = t;
		m_cellsToHighlight = new HashSet<CellCoordinate>();
	}
	
	public PlainTableRenderer(Table t, Collection<CellCoordinate> to_highlight)
	{
		super();
		m_table = t;
		m_cellsToHighlight = new HashSet<CellCoordinate>();
		m_cellsToHighlight.addAll(to_highlight);
	}
	
	public String render()
	{
		StringBuilder out = new StringBuilder();
		out.append("<table border=\"1\">\n");
		DataTable dt = m_table.getDataTable();
		String col_names[] = dt.getColumnNames();
		out.append("<thead>\n<tr>\n");
		for (String col_name : col_names)
		{
			out.append("<th>").append(col_name).append("</th>");
		}
		out.append("</tr></thead>\n<tbody>\n");
		int row = 0;
		for (TableEntry te : dt.getEntries())
		{
			out.append("<tr>");
			for (int col = 0; col < col_names.length; col++)
			{
				Object value = te.get(col_names[col]);
				String css_class = "";
				if (isHighlighted(row, col))
				{
					css_class += " class=\"highlighted\"";
				}
				out.append("<td").append(css_class).append("><a class=\"explanation\" href=\"explain?id=").append(m_table.getDatapointId(row, col)).append("\">").append(value).append("</a></td>");
			}
			out.append("</tr>\n");
			row++;
		}
		out.append("</tbody>\n</table>\n");
		return out.toString();
	}
	
	/**
	 * Determines if an x-y cell should be highlighted
	 * @param row The row
	 * @param col The column
	 * @return true if the cell should be highlighted
	 */
	public boolean isHighlighted(int row, int col)
	{
		for (CellCoordinate cc : m_cellsToHighlight)
		{
			if (cc.row == row && cc.col == col)
				return true;
		}
		return false;
	}
}
