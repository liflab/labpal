package ca.uqac.lif.parkbench.table;

import java.util.Vector;

import ca.uqac.lif.parkbench.FileHelper;
import ca.uqac.lif.parkbench.plot.Plot;

public class Tabular
{
	Vector<String> m_columnHeaders = new Vector<String>();
	Vector<String> m_lineHeaders = new Vector<String>();
	String[][] m_values;

	public Tabular(Vector<String> column_headers, Vector<String> line_headers)
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

	public Tabular put(String line, String column, String value)
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

	public Tabular transpose()
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
	 * Replaces the content of each entry by its fraction of the
	 * sum of all values for the column
	 */
	public void normalizeColumns()
	{
		if (m_values.length == 0)
		{
			return;
		}
		for (int j = 0; j < m_values[0].length; j++)
		{
			float total = 0;
			for (int i = 0; i < m_values.length; i++)
			{
				total += Float.parseFloat(m_values[i][j]);
			}
			for (int i = 0; i < m_values.length; i++)
			{
				m_values[i][j] = Float.toString(Float.parseFloat(m_values[i][j]) / total);
			}
		}
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
			if (ValueTable.isNumeric(x))
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
}