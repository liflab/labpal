package ca.uqac.lif.parkbench.table;

import java.util.Vector;

public class CandlesticksTransform extends TabularTransform 
{
	
	public CandlesticksTransform(Table t)
	{
		super(t);
	}

	@Override
	public Tabular getTabular()
	{
		Tabular tab = m_inputTable.getTabular();
		Vector<String> series_names = tab.m_columnHeaders;
		Vector<String> x_values = tab.m_lineHeaders;
		Vector<String> columns = new Vector<String>();
		columns.add("box-min");
		columns.add("q1");
		columns.add("q2");
		columns.add("q3");
		columns.add("box-high");
		columns.add("dummy");
		Tabular out_tabu = new Tabular(columns, series_names);
		for (int i = 0; i < series_names.size(); i++)
		{
			float max = -1000000;
			float min = 1000000;
			float sum = 0;
			float nb_val = 0;
			boolean seen_value = false;
			for (int j = 0; j < x_values.size(); j++)
			{
				String v_s = tab.m_values[j][i];
				if (!ValueTable.isNumeric(v_s))
				{
					continue;
				}
				seen_value = true;
				Float v_f = Float.parseFloat(v_s);
				nb_val++;
				sum += v_f;
				max = Math.max(max, v_f);
				min = Math.min(min, v_f);
			}
			float avg = sum / nb_val;
			if (seen_value)
			{
				out_tabu.put(series_names.get(i), "box-min", Float.toString(min));
				out_tabu.put(series_names.get(i), "q1", Float.toString(min));
				out_tabu.put(series_names.get(i), "q2", Float.toString(avg));
				out_tabu.put(series_names.get(i), "q3", Float.toString(max));
				out_tabu.put(series_names.get(i), "box-high", Float.toString(max));
			}
			out_tabu.put(series_names.get(i), "dummy", Integer.toString(i + 1));
		}
		return out_tabu;
	}

}
