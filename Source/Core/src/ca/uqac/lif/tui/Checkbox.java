package ca.uqac.lif.tui;

public class Checkbox extends TuiElement
{
	private boolean m_checked = false;

	@Override
	public void render(AnsiPrinter printer)
	{
		if (printer.colorsEnabled())
		{
			if (m_checked)
			{
				printer.print("\u2611\u0020");
			}
			else
			{
				printer.setForegroundColor(AnsiPrinter.Color.DARK_GRAY);
				printer.print("\u2610\u0020");
				printer.resetColors();
			}
		}
		else
		{
			printer.print("[");
			if (m_checked)
			{
				printer.print("X");
			}
			else
			{
				printer.print(" ");
			}
			printer.print("]");
		}
	}

	public boolean isChecked()
	{
		return m_checked;
	}

	public void setChecked(boolean b)
	{
		m_checked = b;
	}

	public void toggle()
	{
		setChecked(!m_checked);
	}
}
