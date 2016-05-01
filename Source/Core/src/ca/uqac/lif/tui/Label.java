package ca.uqac.lif.tui;

public class Label extends TuiElement
{
	private int m_width = -1;
	
	private String m_value = "";
	
	public void setWidth(int w)
	{
		m_width = w;
	}
	
	public void setValue(String s)
	{
		m_value = s;
	}

	@Override
	public void render(AnsiPrinter printer)
	{
		if (m_width > 0)
		{
			printer.print(AnsiPrinter.padToLength(m_value, m_width));
		}
		else
		{
			printer.print(m_value);
		}
	}
	
	
}
