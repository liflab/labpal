package ca.uqac.lif.tui;

public abstract class MenuItem extends TuiElement
{
	private String m_shortcut;
	
	private String m_label;
	
	private boolean m_exit;
	
	public MenuItem(String shortcut, String label, boolean exit)
	{
		super();
		m_shortcut = shortcut;
		m_label = label;
		m_exit = exit;
	}
	
	public boolean fires(String input)
	{
		if (input == null || input.isEmpty())
		{
			return false;
		}
		return input.substring(0, 1).compareToIgnoreCase(m_shortcut) == 0;
	}
	
	public void normal(AnsiPrinter printer)
	{
		printer.setForegroundColor(AnsiPrinter.Color.BLACK);
		printer.setBackgroundColor(AnsiPrinter.Color.BROWN);
	}
	
	public void highlight(AnsiPrinter printer)
	{
		printer.setBackgroundColor(AnsiPrinter.Color.BLACK);
		printer.setForegroundColor(AnsiPrinter.Color.YELLOW);
	}

	@Override
	public void render(AnsiPrinter printer)
	{
		int pos_lc = m_label.indexOf(m_shortcut.toLowerCase());
		int pos_uc = m_label.indexOf(m_shortcut.toUpperCase());
		if (pos_lc == -1 && pos_uc == -1)
		{
			if (printer.colorsEnabled())
			{
				highlight(printer);
				printer.print(m_shortcut);
				normal(printer);
				printer.print(" " + m_label);
			}
			else
			{
				printer.print("(" + m_shortcut + ") " + m_label);
			}
			
			return;
		}
		int pos = 0;
		if (pos_lc != -1)
		{
			pos = pos_lc;
		}
		if (pos_uc != -1 && pos_uc < pos)
		{
			pos = pos_uc;
		}
		normal(printer);
		printer.print(m_label.substring(0, pos));
		if (printer.colorsEnabled())
		{
			highlight(printer);
			printer.print(m_label.substring(pos, pos + 1));
			
		}
		else
		{
			printer.print("(" + m_label.substring(pos, pos + 1) + ")");
		}
		normal(printer);
		printer.print(m_label.substring(pos + 1));
	}
	
	public boolean isExit()
	{
		return m_exit;
	}
	
	public String getShortcut()
	{
		return m_shortcut;
	}
	
	public abstract void doSomething(AnsiPrinter printer);

}
