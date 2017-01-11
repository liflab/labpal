/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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
package ca.uqac.lif.labpal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;

import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;

/**
 * Provides various utility methods for formatting things into character
 * strings.
 * @author Sylvain Hallé
 */
public class Formatter
{
	/**
	 * A math context with three significant digits
	 */
	protected static final MathContext s_threeDigitsContext = new MathContext(3);
	
	/**
	 * Formats a number into a character string using a "printf" format
	 * string
	 * @param n The number
	 * @param format_string The format string
	 * @return The formatted number
	 */
	public static String format(Number n, String format_string)
	{
		float f = n.floatValue();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.printf(format_string, f);
		return new String(baos.toByteArray());
	}
	
	/**
	 * Computes the ratio n/d. The purpose of this
	 * method is to avoid writing lots of checks for these cases:
	 * <ul>
	 * <li>If either n or d are null, returns 0</li>
	 * <li>If d = 0, returns 0</li>
	 * </ul>
	 * @param n
	 * @param d
	 * @return The ratio n/d, cast as a float
	 */
	public static float divide(Number n, Number d)
	{
		if (n == null || d == null)
		{
			return 0f;
		}
		float f_n = n.floatValue();
		float d_n = d.floatValue();
		if (d_n == 0)
		{
			return 0f;
		}
		return f_n / d_n;
	}
	
	/**
	 * Rounds a value to a given number of significant digits.
	 * Please remind that significant digits is <em>not</em> the same thing
	 * as the number of decimal figures of a number.
	 * <p>
	 * The name of the method is made purposefully short, as it is expected
	 * to be used very often in some parts of someone's code.
	 * @param n The value to be rounded
	 * @param digits The number of significant digits. Must be a positive
	 *   integer.
	 * @return The rounded number
	 */
	public static float sigDig(Number n, int digits)
	{
		BigDecimal bd = new BigDecimal(n.floatValue());
		bd = bd.round(new MathContext(digits));
		return bd.floatValue();
	}
	
	/**
	 * Rounds a value to three significant digits. This method may be slightly
	 * more efficient than calling {@code sigDig(n, 3)}, as it reuses an
	 * internal object instead of creating one upon every new call.
	 * @see #sigDig(Number, int)
	 * @param n The value to be rounded
	 * @return The rounded number
	 */
	public static float sigDig(Number n)
	{
		BigDecimal bd = new BigDecimal(n.floatValue());
		bd = bd.round(s_threeDigitsContext);
		return bd.floatValue();
	}
	
	/**
	 * Converts an object into a string
	 * @param o The object
	 * @return The string
	 */
	public static String asString(Object o)
	{
		if (o == null || o instanceof JsonNull)
		{
			return "";
		}
		if (o instanceof String)
		{
			return (String) o;
		}
		if (o instanceof JsonString)
		{
			return ((JsonString) o).stringValue();
		}
		if (o instanceof Number)
		{
			return ((Number) o).toString();
		}
		if (o instanceof JsonNumber)
		{
			return ((JsonNumber) o).toString();
		}
		return o.toString();
	}
}
