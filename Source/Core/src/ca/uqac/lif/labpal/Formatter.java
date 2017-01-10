package ca.uqac.lif.labpal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Formatter
{

	public static String format(Number n, String format_string)
	{
		float f = n.floatValue();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.printf(format_string, f);
		return new String(baos.toByteArray());
	}
}
