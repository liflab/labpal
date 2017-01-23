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
package ca.uqac.lif.labpal.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.labpal.Formatter;
import ca.uqac.lif.labpal.provenance.ProvenanceNode;

/**
 * Computes box-and-whiskers statistics from each column of an
 * input table. For example, given the following table:
 * <table border="1">
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * <tr><td>0</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>3</td><td>4</td></tr>
 * <tr><td>2</td><td>5</td><td>2</td></tr>
 * <tr><td>3</td><td>7</td><td>8</td></tr>
 * </table>
 * the box transformation will produce the following result:
 * <table border="1">
 * <tr><th>x</th><th>Min</th><th>Q1</th><th>Q2</th><th>Q3</th><th>Max</th></tr>
 * <tr><td>A</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td></tr>
 * <tr><td>B</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td></tr>
 * <tr><td>C</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td></tr>
 * </table> 
 * The columns represent respectively:
 * <ol>
 * <li>The name of the column header in the original table</li>
 * <li>The minimum value of that column (Min)</li>
 * <li>The value of the first quartile (Q1)</li>
 * <li>The value of the second quartile (Q2)</li>
 * <li>The value of the third quartile (Q3)</li>
 * <li>The maximum value of that column (Max)</li> 
 * </ol>
 * 
 * This transformation is called a "box transform", because it produces a
 * table in a form that can be used by a
 * {@link ca.uqac.lif.labpal.plot.BoxPlot BoxPlot}.
 *  
 * @author Sylvain Hallé
 */
public class BoxTransformation implements TableTransformation 
{
	protected String m_captionX = "x";
	protected String m_captionMin = "Min";
	protected String m_captionQ1 = "Q1";
	protected String m_captionQ2 = "Q2";
	protected String m_captionQ3 = "Q3";
	protected String m_captionMax = "Max";
	
	public BoxTransformation()
	{
		super();
	}
	
	public BoxTransformation(String x, String prefix)
	{
		super();
		m_captionX = x;
		m_captionMin = prefix + "_Min";
		m_captionQ1 = prefix + "_Q1";
		m_captionQ2 = prefix + "_Q2";
		m_captionQ3 = prefix + "_Q3";
		m_captionMax = prefix + "_Max";
	}
	
	public BoxTransformation(String x, String min, String q1, String q2, String q3, String max)
	{
		super();
		m_captionX = x;
		m_captionMin = min;
		m_captionQ1 = q1;
		m_captionQ2 = q2;
		m_captionQ3 = q3;
		m_captionMax = max;
	}

	@Override
	public DataTable transform(DataTable... tables) 
	{
		DataTable table = tables[0];
		DataTable new_table = new TemporaryDataTable(m_captionX, m_captionMin, m_captionQ1, m_captionQ2, m_captionQ3, m_captionMax);
		for (String col_name : table.getColumnNames())
		{
			Set<ProvenanceNode> deps = new HashSet<ProvenanceNode>();
			List<Float> values = new ArrayList<Float>();
			for (TableEntry te : table.getEntries())
			{
				Float f = Table.readFloat(te.get(col_name));
				if (f != null)
				{
					values.add(f);
				}
				deps.addAll(te.getDatapointIds(col_name));
			}
			Collections.sort(values);
			if (values.isEmpty())
			{
				// Nothing to do
				return new_table;
			}
			float num_values = values.size();
			TableEntry te = new TableEntry();
			te.put(m_captionX, Formatter.jsonCast(col_name));
			te.put(m_captionMin, new JsonNumber(values.get(0)));
			te.put(m_captionQ1, new JsonNumber(values.get((int)(num_values * 0.25) - 1)));
			te.put(m_captionQ2, new JsonNumber(values.get((int)(num_values * 0.5) - 1)));
			te.put(m_captionQ3, new JsonNumber(values.get((int)(num_values * 0.75) - 1)));
			te.put(m_captionMax, new JsonNumber(values.get((int) num_values - 1)));
			te.addDependency(m_captionMin, new FunctionProvenanceNode("T", table, "Minimum value of column " + col_name, deps));
			te.addDependency(m_captionQ1, new FunctionProvenanceNode("T", table, "First quartile " + col_name, deps));
			te.addDependency(m_captionQ2, new FunctionProvenanceNode("T", table, "Second quartile of column " + col_name, deps));
			te.addDependency(m_captionQ3, new FunctionProvenanceNode("T", table, "Third quartile of column " + col_name, deps));
			te.addDependency(m_captionMax, new FunctionProvenanceNode("T", table, "Maximum value of column " + col_name, deps));
			new_table.add(te);
		}
		return new_table;
	}

}
