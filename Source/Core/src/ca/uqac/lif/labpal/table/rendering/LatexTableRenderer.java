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
package ca.uqac.lif.labpal.table.rendering;

import java.util.List;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonString;

/**
 * Renders a result tree as a LaTeX table. The resulting table
 * uses the <tt>multirow</tt> package to merge cells vertically.
 * 
 * @author Sylvain Hallé
 */
public class LatexTableRenderer extends TableNodeRenderer 
{
	/**
	 * A counter for the columns in the table
	 */
	protected transient int m_numColumns = 0;
	
	/**
	 * A buffer to hold the column headers until we know how many
	 * columns there are. This is due to the fact that a <code>tabular</code>
	 * environment in LaTeX must specify the alignment of each column
	 * <em>before</em> the keys are listed, and so in our case before
	 * we know how many keys there are.
	 */
	protected final transient StringBuilder m_keyBuffer = new StringBuilder();
	
	/**
	 * Whether the table's column headers are printed in bold
	 */
	protected boolean m_boldKeys = true;
	
	/**
	 * The number of repeated cells the last time a table line was started
	 */
	protected transient int m_repeatedCells = 0;
	
	/**
	 * The name of the LaTeX environment that displays the table
	 */
	public static enum EnvironmentName {TABULAR, LONGTABLE};
	
	/**
	 * The name of the LaTeX environment that displays the table
	 */
	protected EnvironmentName m_environmentName = EnvironmentName.TABULAR;
	
	/**
	 * Sets the name of the LaTeX environment that displays the table
	 * @param name The name
	 */
	public void setEnvironmentName(EnvironmentName name)
	{
		m_environmentName = name;
	}
	
	/**
	 * Sets whether the table's column headers are printed in bold
	 * @param b Set to <code>true</code> to put the headers in bold,
	 *   (the default), <code>false</code> otherwise 
	 */
	public void setBoldKeys(boolean b)
	{
		m_boldKeys = b;
	}
	
	/**
	 * Gets the LaTeX name associated to each value of EnvironmentName
	 * @param name The name
	 * @return The LaTeX string
	 */
	protected static String getLatexEnvironmentName(EnvironmentName name)
	{
		if (name == EnvironmentName.LONGTABLE)
		{
			return "longtable";
		}
		return "tabular";
	}

	@Override
	public void startStructure(StringBuilder out) 
	{
		out.append("\\begin{").append(getLatexEnvironmentName(m_environmentName)).append("}");
	}

	@Override
	public void startKeys(StringBuilder out)
	{
		// Do nothing
	}

	@Override
	public void printKey(StringBuilder out, String key)
	{
		if (m_keyBuffer.length() > 0)
		{
			m_keyBuffer.append(" & ");
		}
		if (m_boldKeys)
		{
			m_keyBuffer.append("\\textbf{");
		}
		else
		{
			m_keyBuffer.append("{");
		}
		m_keyBuffer.append(key).append("}");
		m_numColumns++;
	}

	@Override
	public void endKeys(StringBuilder out)
	{
		out.append("{|");
		for (int i = 0; i < m_numColumns; i++)
		{
			out.append("c|");
		}
		out.append("}\n");
		out.append(m_keyBuffer).append("\\\\\n");
		if (m_environmentName == EnvironmentName.LONGTABLE)
		{
			out.append("\\endfirsthead\n");
		}
		else
		{
			out.append("\\hline");
		}
	}

	@Override
	public void startBody(StringBuilder out) 
	{
		// Do nothing
	}

	@Override
	public void startRow(StringBuilder out, int max_depth) 
	{
		m_keyBuffer.setLength(0); // Clear
		m_repeatedCells = 0;
	}

	@Override
	public void printCell(StringBuilder out, List<JsonElement> values, int nb_children, int max_depth) 
	{
		if (nb_children > 2)
		{
			m_keyBuffer.append(" \\multirow{").append(nb_children).append("}{*}{");
		}
		else
		{
			m_keyBuffer.append("{");
		}
		JsonElement last = values.get(values.size() - 1);
		if (last instanceof JsonString)
		{
			m_keyBuffer.append(((JsonString) last).stringValue());
		}
		else if (last instanceof JsonNull)
		{
			m_keyBuffer.append("");
		}
		else
		{
			m_keyBuffer.append(last);
		}
		m_keyBuffer.append("}");
		if (values.size() < max_depth)
		{
			m_keyBuffer.append(" & ");
		}
		
	}

	@Override
	public void printRepeatedCell(StringBuilder out, List<JsonElement> values, int index, int max_depth)
	{
		if (values.size() > 1)
		{
			m_keyBuffer.append(" & ");
		}
		m_repeatedCells++;
	}

	@Override
	public void endRow(StringBuilder out, int max_depth)
	{
		if (m_repeatedCells > 0)
		{
			out.append("\\cline{").append(m_repeatedCells + 1).append("-").append(max_depth).append("}\n");
		}
		else
		{
			out.append("\\hline\n");
		}
		out.append(m_keyBuffer);
		out.append("\\\\\n");		
	}

	@Override
	public void endBody(StringBuilder out) 
	{
		// Do nothing
	}

	@Override
	public void endStructure(StringBuilder out)
	{
		out.append("\\end{").append(getLatexEnvironmentName(m_environmentName)).append("}");
	}

}
