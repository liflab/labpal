package ca.uqac.lif.tui;

import java.util.ArrayList;

public class Menu extends TuiElement
{
	private ArrayList<MenuItem> m_items;
	
	public static String s_promptCharacterBlack = ">";
	
	public static String s_promptCharacterColor = "\u25ba";
	
	public Menu()
	{
		super();
		m_items = new ArrayList<MenuItem>();
	}
	
	public void addItem(MenuItem i)
	{
		m_items.add(i);
	}

	@Override
	public void render(AnsiPrinter printer)
	{
		while (true)
		{
			renderBefore(printer);
			for (MenuItem mi : m_items)
			{
				mi.render(printer);
				printer.print("  ");
			}
			if (printer.colorsEnabled())
				printer.print(s_promptCharacterColor);
			else
				printer.print(s_promptCharacterBlack);
			printer.resetColors();
			printer.print(" ");
			String choice = printer.readLine();
			for (MenuItem mi : m_items)
			{
				if (mi.fires(choice))
				{
					if (mi.isExit())
					{
						return;
					}
					mi.doSomething(printer);
					break;
				}
			}
		}
	}
	
	public void renderBefore(AnsiPrinter printer)
	{
		
	}
}
