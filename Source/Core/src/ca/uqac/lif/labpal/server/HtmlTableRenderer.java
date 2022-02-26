/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.labpal.server;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.HtmlSpreadsheetPrinter;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Renders a table to be displayed in the web interface. Each cell of the table
 * is decorated with a hyperlink to a page that displays the explanation of
 * that cell.
 * @author Sylvain Hallé
 */
public class HtmlTableRenderer extends HtmlSpreadsheetPrinter
{
	/**
	 * A set of cells to be highlighted in the table.
	 */
	protected Set<Cell> m_toHighlight;
	
	/**
	 * The table to render.
	 */
	/*@ non_null @*/ protected Table m_table;
	
	/**
	 * The prefix of the URL for the explanation of a value
	 */
	protected String m_explainUrlPrefix;
	
	public HtmlTableRenderer(Table t, String prefix)
	{
		super();
		m_table = t;
		m_explainUrlPrefix = prefix;
		m_toHighlight = new HashSet<Cell>();
	}
	
	public HtmlTableRenderer(Table t)
	{
		this(t, "explain");
	}
	
	public HtmlTableRenderer highlight(Cell c)
	{
		m_toHighlight.add(c);
		return this;
	}
	
	public HtmlTableRenderer highlight(Collection<Cell> cells)
	{
		m_toHighlight.addAll(cells);
		return this;
	}
	
	public HtmlTableRenderer highlight(Cell ... cells)
	{
		for (Cell c :cells)
		{
			m_toHighlight.add(c);
		}
		return this;
	}
	
	/**
	 * Sets the prefix of the URL for the explanation of a value. Each cell
	 * in the HTML table is surrounded by a link leading to an explanation
	 * page. 
	 * @param prefix The URL prefix
	 */
	public HtmlTableRenderer setExplainUrlPrefix(String prefix)
	{
		m_explainUrlPrefix = prefix;
		return this;
	}
	
	public void render(PrintStream ps)
	{
		print(m_table.getSpreadsheet(), ps);
	}
	
	@Override
	protected void printCellStart(Spreadsheet s, PrintStream ps, int col, int row, int colspan, int rowspan)
	{
		String celltype = (row == 0 ? "th" : "td");
		Cell c = Cell.get(col, row);
		String highlight_code = m_toHighlight.contains(c) ? " class=\"highlighted\"" : "";
		ps.print("<");
		ps.print(celltype);
		if (colspan > 1)
		{
			ps.print(" colspan=\"");
			ps.print(colspan);
			ps.print("\"");
		}
		if (rowspan > 1)
		{
			ps.print(" rowspan=\"");
			ps.print(rowspan);
			ps.print("\"");
		}
		ps.print(highlight_code);
		ps.println(">");
	}
	
	@Override
	protected void printValue(Spreadsheet s, PrintStream ps, int col, int row, Object o)
	{
		if (o == null)
		{
			ps.print("&nbsp;");
			return;
		}
		String dp_id = "T" + m_table.getId() + ":" + row + ":" + col;
		ps.print("<a class=\"explanation\" title=\"Click to see where this value comes from\" href=\"");
		ps.print(m_explainUrlPrefix);
		ps.print("?id=" + dp_id + "\">");
		ps.print(escape(o.toString()));
		ps.print("</a>");
	}
}
