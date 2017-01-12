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
import java.util.List;

import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;

/**
 * Computes box-and-whiskers statistics from each column of an
 * input table.
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
		DataTable new_table = new DataTable(m_captionX, m_captionMin, m_captionQ1, m_captionQ2, m_captionQ3, m_captionMax);
		for (String col_name : table.getColumnNames())
		{
			List<Float> values = new ArrayList<Float>();
			for (TableEntry te : table.getEntries())
			{
				Float f = Table.readFloat(te.get(col_name));
				if (f != null)
				{
					values.add(f);
				}
			}
			Collections.sort(values);
			float num_values = values.size();
			TableEntry te = new TableEntry();
			te.put(m_captionX, new JsonString(col_name));
			te.put(m_captionMin, new JsonNumber(values.get(0)));
			te.put(m_captionQ1, new JsonNumber(values.get((int)(num_values * 0.25) - 1)));
			te.put(m_captionQ2, new JsonNumber(values.get((int)(num_values * 0.5) - 1)));
			te.put(m_captionQ3, new JsonNumber(values.get((int)(num_values * 0.75) - 1)));
			te.put(m_captionMax, new JsonNumber(values.get((int) num_values - 1)));
			new_table.add(te);
		}
		return new_table;
	}

}
