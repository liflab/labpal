package ca.uqac.lif.tui;

import java.util.ArrayList;
import java.util.Collection;

public class TuiList extends TuiElement
{
	private int m_numColumns = 1;
	
	private String m_header = "";
	
	private ArrayList<TuiElement> m_items;
	
	public TuiList()
	{
		super();
		m_items = new ArrayList<TuiElement>();
	}
	
	public TuiList setHeader(String s)
	{
		m_header = s;
		return this;
	}
	
	public TuiList setColumns(int c)
	{
		m_numColumns = c;
		return this;
	}
	
	public TuiList addAll(Collection<TuiElement> c)
	{
		m_items.addAll(c);
		return this;
	}
	
	public TuiList add(TuiElement e)
	{
		m_items.add(e);
		return this;
	}
	
	@Override
	public void render(AnsiPrinter printer)
	{
		int num_col = 0;
		if (!m_header.isEmpty())
		{
			for (int i = 0; i < m_numColumns; i++)
			{
				printer.print(m_header + " ");
			}
			printer.print("\n");
		}
		for (TuiElement e : m_items)
		{
			e.render(printer);
			if (num_col == m_numColumns - 1)
			{
				printer.print("\n");
				num_col = 0;
			}
			else
			{
				printer.print(" ");
				num_col++;
			}
		}
		printer.print("\n");
	}

}
