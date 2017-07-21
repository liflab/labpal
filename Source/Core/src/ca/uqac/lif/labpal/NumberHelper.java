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

/**
 * Utility methods for manipulating numbers
 */
public class NumberHelper
{
	/**
	 * Rounds number num to n significant figures.
	 * Found from <a href="http://stackoverflow.com/a/1581007">StackOverflow</a>
	 * @param num The number
	 * @param n The number of significant figures
	 * @return The resulting number
	 */
	public static double roundToSignificantFigures(double num, int n) 
	{
	    if(num == 0) 
	    {
	        return 0;
	    }
	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;
	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num*magnitude);
	    return shifted/magnitude;
	}
	
	/**
	 * Checks if a given string contains a number
	 * @param s The string
	 * @return true if it contains a number, false otherwise
	 */
	public static boolean isNumeric(String s)
	{
		if (s == null)
		{
			return false;
		}
		try
		{
			Float.parseFloat(s);
			return true;
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
	}
	
	/**
	 * Converts a string into its "closest" primitive type. If the
	 * string parses as an integer, the number returned will be an
	 * {@code int}. Otherwise, if it parses as a float, the number
	 * returned will be a {@code float}. In all other cases, the
	 * returned value is {@code null}.
	 * @param s The string to convert into a number
	 * @return The number
	 */
	public static Number toPrimitiveNumber(String s)
	{
		if (s == null)
		{
			return null;
		}
		s = s.trim();
		try
		{
			int i = Integer.parseInt(s);
			return i;
		}
		catch (NumberFormatException nfe)
		{
			// Do nothing
		}
		try
		{
			float f = Float.parseFloat(s);
			return f;
		}
		catch (NumberFormatException nfe)
		{
			// Do nothing
		}
		return null;
	}
}
