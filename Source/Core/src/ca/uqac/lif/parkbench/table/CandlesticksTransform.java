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
package ca.uqac.lif.parkbench.table;

import java.util.Collections;
import java.util.Vector;

import ca.uqac.lif.parkbench.NumberHelper;

public class CandlesticksTransform extends TableTransform 
{
	
	public CandlesticksTransform(Table t)
	{
		super(t);
	}

	@Override
	public ConcreteTable getConcreteTable()
	{
		ConcreteTable tab = m_inputTable.getConcreteTable();
		Vector<String> series_names = tab.m_columnHeaders;
		Vector<String> x_values = tab.m_lineHeaders;
		Vector<String> columns = new Vector<String>();
		columns.add("box-min");
		columns.add("q1");
		columns.add("q2");
		columns.add("q3");
		columns.add("box-high");
		columns.add("dummy");
		ConcreteTable out_tabu = new ConcreteTable(columns, series_names);
		for (int i = 0; i < series_names.size(); i++)
		{
			Vector<Float> values = new Vector<Float>();
			for (int j = 0; j < x_values.size(); j++)
			{
				String v_s = tab.m_values[j][i];
				if (!NumberHelper.isNumeric(v_s))
				{
					continue;
				}
				Float v_f = Float.parseFloat(v_s);
				values.add(v_f);
			}
			if (!values.isEmpty())
			{
				Collections.sort(values);
				float num_vals = values.size();
				out_tabu.put(series_names.get(i), "box-min", Float.toString(values.firstElement()));
				out_tabu.put(series_names.get(i), "q1", Float.toString(values.get((int)(num_vals * 0.25))));
				out_tabu.put(series_names.get(i), "q2", Float.toString(values.get((int)(num_vals * 0.5))));
				out_tabu.put(series_names.get(i), "q3", Float.toString(values.get((int)(num_vals * 0.75))));
				out_tabu.put(series_names.get(i), "box-high", Float.toString(values.lastElement()));
			}
			out_tabu.put(series_names.get(i), "dummy", Integer.toString(i + 1));
		}
		return out_tabu;
	}
}
