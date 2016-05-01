package ca.uqac.lif.tui;

public class NestedMenu extends MenuItem
{
	protected Menu m_menu;
	
	public NestedMenu(String shortcut, String label, Menu menu)
	{
		super(shortcut, label, false);
		m_menu = menu;
	} 

	@Override
	public void doSomething(AnsiPrinter printer)
	{
		m_menu.render(printer);
	}

}
