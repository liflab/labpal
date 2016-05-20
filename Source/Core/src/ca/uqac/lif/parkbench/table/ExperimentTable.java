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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.Laboratory;

/**
 * Table whose values are taken from the results of one or more experiments.
 */
public class ExperimentTable extends Table
{
	/**
	 * The set of experiments this table is supposed to handle
	 */
	protected Set<Experiment> m_experiments;

	/**
	 * The set of experiments this table is supposed to handle
	 */
	protected Vector<String> m_seriesNames;

	/**
	 * The input parameter in an experiment to use as the x-value of the plot
	 */
	protected String m_xName;

	/**
	 * The input parameter in an experiment to use as the y-value of the plot
	 */
	protected String m_yName;

	public ExperimentTable()
	{
		super();
		m_experiments = new HashSet<Experiment>();
		m_seriesNames = new Vector<String>();
	}

	/**
	 * Adds an experiment to the table
	 * @param e The experiment
	 * @return This table
	 */
	public ExperimentTable add(Experiment e)
	{
		m_experiments.add(e);
		return this;
	}

	/**
	 * Tells the plot to group experiment results into data series, according
	 * to a parameter present in the experiments
	 * @param param The input parameters in an experiment used to determine
	 * to which data series it belongs
	 * @return This table
	 */
	public ExperimentTable groupBy(String ... param)
	{
		for (String p : param)
		{
			m_seriesNames.add(p);
		}
		return this;
	}

	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "x" value 
	 * @param param The output parameter to use for the "x" value
	 * @return This table
	 */
	public ExperimentTable useForX(String param)
	{
		m_xName = param;
		return this;
	}

	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "y" value 
	 * @param param The output parameter to use for the "y" value
	 * @return This table
	 */
	public ExperimentTable useForY(String param)
	{
		m_yName = param;
		return this;
	}

	/**
	 * Returns the contents of the table as a CSV string.
	 * @return A CSV string
	 */
	public String toCsv()
	{
		Vector<String> series = getSeriesNames();
		return toCsv(series, false);
	}

	/**
	 * Returns the contents of the table as a CSV string.
	 * @param series The list of the column headers
	 * @return A CSV string
	 */
	public String toCsv(Vector<String> series, boolean transposed)
	{
		Vector<String> x_values = getXValues();
		ConcreteTable t = getValues(series, x_values);
		if (transposed)
		{
			t.transpose();
		}
		return t.toCsv();
	}
	
	/**
	 * Returns the contents of the table as a CSV string.
	 * @param series The list of the column headers
	 * @return A CSV string
	 */
	public String toCsv(boolean transposed)
	{
		Vector<String> series = new Vector<String>();
		series.add("");
		Vector<String> x_values = getXValues();
		ConcreteTable t = getValues(series, x_values);
		if (transposed)
		{
			t.transpose();
		}
		return t.toCsv();
	}
	
	public ConcreteTable getConcreteTable()
	{
		Vector<String> series = getSeriesNames();
		Vector<String> x_values = getXValues();
		ConcreteTable t = getValues(series, x_values);
		return t;
	}

	/**
	 * Gets the sorted list of all distinct series names found in the set of
	 * experiments associated to this plot
	 * @return The list of all series names
	 */
	public Vector<String> getSeriesNames()
	{
		Vector<String> series = new Vector<String>();
		for (Experiment e: m_experiments)
		{
			if (e == null)
				continue;
			String s_name = createSeriesName(e);
			if (s_name != null && !series.contains(s_name))
			{
				series.add(s_name);
			}
		}
		Collections.sort(series);
		return series;
	}

	/**
	 * Creates the name of the series an experiment belongs to, based on
	 * the filtering criteria
	 * @param e The experiment
	 * @return The name of the series
	 */
	protected String createSeriesName(Experiment e)
	{
		String s_name = "";
		if (m_seriesNames.size() == 1)
		{
			// If only one parameter, don't put its name
			return e.readString(m_seriesNames.get(0));
		}
		for (String series_param : m_seriesNames)
		{
			if (!s_name.isEmpty())
			{
				s_name += ",";
			}
			String ser_name = e.readString(series_param);
			if (ser_name == null)
			{
				continue;
			}
			s_name += series_param + "=" + ser_name;
		}
		return s_name;
	}


	/**
	 * Creates a map
	 * @param series The data series in the table
	 * @param x_values A <em>sorted</em> list of all the x-values
	 *   occurring in the table
	 * @return The map
	 */
	protected ConcreteTable getValues(Vector<String> series, Vector<String> x_values)
	{
		// Build the table from the values
		ConcreteTable values = new ConcreteTable(series, x_values);
		for (Experiment e : m_experiments)
		{
			if (e == null || e.getStatus() != Experiment.Status.DONE)
				continue;
			String ser = createSeriesName(e);
			JsonElement je_x = e.read(m_xName);
			if (je_x instanceof JsonList)
			{
				JsonList jl_x = (JsonList) je_x;
				JsonElement je_y = e.read(m_yName);
				if (!(je_y instanceof JsonList))
				{
					// If the x element is an array, the y element should
					// be an array too
					assert false;
				}
				JsonList jl_y = (JsonList) je_y;
				for (int i = 0; i < jl_x.size(); i++)
				{
					String n_x = elementToString(jl_x.get(i));
					String n_y = elementToString(jl_y.get(i));
					if (n_x == null || n_y == null)
						continue;
					values.put(n_x, ser, n_y);
				}
			}
			else
			{
				String n_x = e.readString(m_xName);
				if (n_x == null)
					continue;
				String n_y = e.readString(m_yName);
				if (n_y == null)
					continue;
				values.put(n_x, ser, n_y);
			}
		}
		return values;
	}
	
	protected static String elementToString(JsonElement je)
	{
		if (je == null)
		{
			return null;
		}
		if (je instanceof JsonString)
		{
			return ((JsonString) je).stringValue();
		}
		return je.toString();
	}
	
	@Override
	public ExperimentTable assignTo(Laboratory a)
	{
		m_lab = a;
		a.add(this);
		HashSet<Experiment> exps = new HashSet<Experiment>();
		for (Experiment e : m_experiments)
		{
			int exp_id = e.getId();
			Experiment new_e = a.getExperiment(exp_id);
			exps.add(new_e);
		}
		m_experiments = exps;
		return this;
	}
	
	/**
	 * Gets the sorted list of all x values occurring in at least one experiment
	 * @return The list of x values
	 */
	public Vector<String> getXValues()
	{
		Vector<String> values = new Vector<String>();
		for (Experiment e : m_experiments)
		{
			if (e == null)
				continue;
			// Check if value is a list
			JsonElement je = e.read(m_xName);
			if (je instanceof JsonList)
			{
				// Yes: add all values of that list
				JsonList jl = (JsonList) je;
				for (JsonElement jel : jl)
				{
					String f_val;
					if (jel instanceof JsonString)
					{
						f_val = ((JsonString) jel).stringValue();
					}
					else
					{
						f_val = jel.toString();
					}
					if (!values.contains(f_val))
					{
						values.add(f_val);
					}
				}
			}
			else
			{
				// No: add that single value
				String f_val = e.readString(m_xName);
				if (f_val == null)
					continue;
				if (!values.contains(f_val))
				{
					values.add(f_val);
				}
			}
		}
		Collections.sort(values, new XComparator());
		return values;
	}

		
	protected class XComparator implements Comparator<String>
	{
		@Override
		public int compare(String arg0, String arg1)
		{
			try
			{
				Float.parseFloat(arg0);
				// We are comparing numbers
				return Float.valueOf(arg0).compareTo(Float.valueOf(arg1));
			}
			catch (NumberFormatException nfe)
			{
				// We are comparing strings
				return arg0.compareTo(arg1);
			}
		}
	}
}
