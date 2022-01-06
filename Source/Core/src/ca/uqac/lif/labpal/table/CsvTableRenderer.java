package ca.uqac.lif.labpal.table;

import java.io.PrintStream;

import ca.uqac.lif.spreadsheet.AnsiSpreadsheetPrinter;

public class CsvTableRenderer extends AnsiSpreadsheetPrinter
{
	protected Table m_table;

	public CsvTableRenderer(Table t)
	{
		super();
		setColumnSeparator(",");
		setGroupCells(false);
		setMaxWidth(-1);
		m_table = t;
	}

	public CsvTableRenderer render(PrintStream ps)
	{
		print(m_table.getSpreadsheet(), ps);
		return this;
	}
}
