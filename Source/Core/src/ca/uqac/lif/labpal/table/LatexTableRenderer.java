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
package ca.uqac.lif.labpal.table;

import java.io.PrintStream;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.StructuredSpreadsheetPrinter;

public class LatexTableRenderer extends StructuredSpreadsheetPrinter
{
	protected Table m_table;
	
	public LatexTableRenderer(Table t)
	{
		super();
		m_table = t;
	}
	
	public LatexTableRenderer render(PrintStream ps)
	{
		print(m_table.getSpreadsheet(), ps);
		return this;
	}
	
	@Override
	protected void printTableStart(Spreadsheet s, PrintStream ps)
	{
		String table_type = m_mergeCells ? "longtable" : "table";
		ps.print("\\begin{" + table_type + "}{");
		for (int i = 0; i < s.getWidth(); i++)
		{
			if (i > 0)
			{
				ps.print("|");
			}
			ps.print("c");
		}
		ps.println("}");
	}

	@Override
	protected void printTableEnd(Spreadsheet s, PrintStream ps)
	{
		String table_type = m_mergeCells ? "longtable" : "table";
		ps.print("\\end{" + table_type + "}");
	}

	@Override
	protected void printRowStart(Spreadsheet s, PrintStream ps, int row)
	{
		// Nothing to do
	}

	@Override
	protected void printCellStart(Spreadsheet s, PrintStream ps, int col, int row, int colspan, int rowspan)
	{
		if (col > 0)
		{
			ps.print(" & ");
		}
		if (rowspan > 1)
		{
			ps.print("\\multirow{" + rowspan + "}{*}{");
		}
		else
		{
			ps.print("{");
		}
	}

	@Override
	protected void printValue(Spreadsheet s, PrintStream ps, int col, int row, Object o)
	{
		if (o != null)
		{
			if (row == 0)
			{
				ps.print("\\textbf{");
			}
			ps.print(escape(o.toString()));
			if (row == 0)
			{
				ps.print("}");
			}
		}
	}

	@Override
	protected void printCellEnd(Spreadsheet s, PrintStream ps, int col, int row, int colspan, int rowspan)
	{
		ps.print("}");
	}

	@Override
	protected void printRowEnd(Spreadsheet s, PrintStream ps, int row)
	{
		ps.println(" \\\\");
		if (row == 0)
		{
			if (m_mergeCells)
			{
				ps.println("\\endfirsthead");
			}
			ps.println("\\hline");
		}
	}
	
	/**
	 * Escapes a string for LaTeX
	 * @param input The input string
	 * @return The output string
	 */
	public static String escape(String input)
	{
		String output = input;
		output = output.replaceAll("\\\\", "\\\\\\\\");
		output = output.replaceAll("_", "\\\\_");
		output = output.replaceAll("~", "\\\\~");
		output = output.replaceAll("&", "\\\\&");
		output = output.replaceAll("#", "\\\\$");
		output = output.replaceAll("%", "\\\\%");
		output = output.replaceAll("\\{", "\\\\\\{");
		output = output.replaceAll("\\}", "\\\\\\}");
		return output;
	}

	/**
	 * Formats a table name to be a valid name in LaTeX
	 * @param name The name
	 * @return The formatted name
	 */
	public static String formatName(String name)
	{
		String output = name;
		// Keep only letters and numbers
		output = output.replaceAll("[^A-Za-z0-9]", "");
		// Since macro names cannot have numbers, replace them by letters
		output = output.replaceAll("0", "a");
		output = output.replaceAll("1", "b");
		output = output.replaceAll("2", "c");
		output = output.replaceAll("3", "d");
		output = output.replaceAll("4", "e");
		output = output.replaceAll("5", "f");
		output = output.replaceAll("6", "g");
		output = output.replaceAll("7", "h");
		output = output.replaceAll("8", "i");
		output = output.replaceAll("9", "j");
		return output;
	}

}
