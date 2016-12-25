/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

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
package ca.uqac.lif.labpal.table;

import java.util.Vector;

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.NumberHelper;
import ca.uqac.lif.labpal.plot.Plot;

/**
 * A table containing concrete values that can be queried.
 */
public class ConcreteTable extends Table
{
	/**
	 * The number of significant digits that will be used when displaying the
	 * table in the web interface
	 */
	public static transient final int s_significantDigits = 3;
	
	Vector<String> m_columnHeaders = new Vector<String>();
	Vector<String> m_lineHeaders = new Vector<String>();
	String[][] m_values;

	public ConcreteTable(Vector<String> column_headers, Vector<String> line_headers)
	{
		super();
		m_columnHeaders.addAll(column_headers);
		m_lineHeaders.addAll(line_headers);
		m_values = new String[m_lineHeaders.size()][m_columnHeaders.size()];
	}
	
	public Vector<String> getSeriesNames()
	{
		return m_columnHeaders;
	}
	
	public int getWidth()
	{
		return m_columnHeaders.size();
	}

	public ConcreteTable put(String line, String column, String value)
	{
		int p_l = m_lineHeaders.indexOf(line);
		int p_c = m_columnHeaders.indexOf(column);
		m_values[p_l][p_c] = value;
		return this;
	}

	public String get(String line, String column)
	{
		int p_l = m_lineHeaders.indexOf(line);
		int p_c = m_columnHeaders.indexOf(column);
		return m_values[p_l][p_c];
	}

	public ConcreteTable transpose()
	{
		// Swap lines and columns
		Vector<String> temp = m_lineHeaders;
		m_lineHeaders = m_columnHeaders;
		m_columnHeaders = temp;
		// Transpose the array of values
		String[][] transposed = new String[m_values[0].length][m_values.length];
		for (int i = 0; i < m_values.length; i++)
		{
			for (int j = 0; j < m_values[0].length; j++)
			{
				transposed[j][i] = m_values[i][j];
			}
		}
		m_values = transposed;
		return this;
	}
	
	public Vector<String> getXValues()
	{
		return m_lineHeaders;
	}
	
	/**
	 * Returns the contents of the table as a CSV string.
	 * @param series The data series in the table 
	 * @return A CSV string
	 */
	public String toCsv()
	{
		StringBuilder out = new StringBuilder();
		for (String x : m_lineHeaders)
		{
			if (NumberHelper.isNumeric(x))
				out.append(x);
			else
			{
				out.append("\"" + x + "\"");
			}
			for (String s : m_columnHeaders)
			{
				out.append(Plot.s_datafileSeparator);
				String val = get(x,s);
				if (val != null)
				{
					out.append(val);
				}
				else
				{
					out.append(Plot.s_datafileMissing);
				}
			}
			out.append(FileHelper.CRLF);
		}
		return out.toString();
	}
	
	/**
	 * Returns the contents of the table as a CSV string.
	 * @param series The data series in the table 
	 * @return A CSV string
	 */
	public String toHtml()
	{
		StringBuilder out = new StringBuilder();
		out.append("<table border=\"1\">\n<thead><tr><th></th>");
		for (String s : m_columnHeaders)
		{
			out.append("<th>").append(s).append("</th>");
		}
		out.append("</tr></thead><tbody>\n");
		for (String x : m_lineHeaders)
		{
			out.append("<tr><td>").append(x).append("</td>");
			for (String s : m_columnHeaders)
			{
				out.append("<td>");
				String val = get(x,s);
				if (val != null)
				{
					if (NumberHelper.isNumeric(val))
					{
						out.append(NumberHelper.roundToSignificantFigures(Double.parseDouble(val), s_significantDigits));
					}
					else
					{
						out.append(val);
					}
				}
				else
				{
					out.append(Plot.s_datafileMissing);
				}
				out.append("</td>");
			}
			out.append("</tr>\n");
		}
		out.append("</tbody></table>");
		return out.toString();
	}

	@Override
	public ConcreteTable getConcreteTable() 
	{
		return this;
	}
}