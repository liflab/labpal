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
package ca.uqac.lif.parkbench;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;

public class Table
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

	
	public Table()
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
	public Table addExperiment(Experiment e)
	{
		m_experiments.add(e);
		return this;
	}
	
	/**
	 * Tells the plot to group experiment results into data series, according
	 * to an input parameter present in the experiments
	 * @param param The input parameters in an experiment used to determine
	 * to which data series it belongs
	 * @return This table
	 */
	public Table groupBy(String ... param)
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
	public Table useForX(String param)
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
	public Table useForY(String param)
	{
		m_yName = param;
		return this;
	}
	
	/**
	 * Returns the contents of the table as a CSV string.
	 * @param series The data series in the table 
	 * @return A CSV string
	 */
	public String toCsv(Vector<String> series)
	{
		Vector<Float> x_values = getXValues();
		Map<Float,Map<String,Float>> values = getValues(series, x_values);
		StringBuilder out = new StringBuilder();
		for (float x : x_values)
		{
			out.append(x);
			Map<String,Float> m = values.get(x);
			for (String s : series)
			{
				out.append(",");
				if (m.get(s) != null)
				{
					out.append(m.get(s));
				}
				else
				{
					out.append("?");
				}
			}
			out.append("\n");
		}
		return out.toString();
	}

	/**
	 * Returns the contents of the table as a CSV string.
	 * @return A CSV string
	 */
	public String toCsv()
	{
		Vector<String> series = getSeriesNames();
		return toCsv(series);
	}
	
	/**
	 * Gets the sorted list of all distinct series names found in the set of
	 * experiments associated to this plot
	 * @return The list of all series names
	 */
	protected Vector<String> getSeriesNames()
	{
		Vector<String> series = new Vector<String>();
		for (Experiment e: m_experiments)
		{
			if (e == null)
				continue;
			String s_name = createSeriesName(e);
			if (!series.contains(s_name))
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
		for (String series_param : m_seriesNames)
		{
			if (!s_name.isEmpty())
			{
				s_name += ",";
			}
			JsonElement j_name = e.getInputParameters().get(series_param);
			String ser_name = "";
			if (j_name instanceof JsonString)
			{
				ser_name = ((JsonString) j_name).stringValue();
			}
			else
			{
				ser_name = j_name.toString();
			}
			s_name += series_param + "=" + ser_name;
		}
		return s_name;
	}
	
	/**
	 * Gets the sorted list of all x values occurring in at least one experiment
	 * @return The list of x values
	 */
	protected Vector<Float> getXValues()
	{
		Vector<Float> values = new Vector<Float>();
		for (Experiment e : m_experiments)
		{
			if (e == null)
				continue;
			JsonElement j_val = e.getInputParameters().get(m_xName);
			if (!(j_val instanceof JsonNumber))
				continue;
			float f_val = ((JsonNumber) j_val).numberValue().floatValue();
			if (!values.contains(f_val))
			{
				values.add(f_val);
			}
		}
		Collections.sort(values);
		return values;
	}

	/**
	 * Creates a map
	 * @param series The data series in the table
	 * @param x_values A <em>sorted</em> list of all the x-values
	 *   occurring in the table
	 * @return The map
	 */
	protected Map<Float,Map<String,Float>> getValues(Vector<String> series, Vector<Float> x_values)
	{
		// Build the table from the values
		Map<Float,Map<String,Float>> values = createMap(series, x_values);
		for (Experiment e : m_experiments)
		{
			if (e == null || e.getStatus() != Experiment.Status.DONE)
				continue;
			String ser = createSeriesName(e);
			JsonNumber n_x = (JsonNumber) e.getInputParameters().get(m_xName);
			if (n_x == null)
				continue;
			float x = n_x.numberValue().floatValue();
			JsonNumber n_y = (JsonNumber) e.getOutputParameters().get(m_yName);
			if (n_y == null)
				continue;
			float y = n_y.numberValue().floatValue();
			Map<String,Float> m = values.get(x);
			m.put(ser, y);
		}
		return values;
	}

	/**
	 * Creates a map
	 * @param series The data series in the table
	 * @param x_values A <em>sorted</em> list of all the x-values
	 *   occurring in the table
	 * @return The map
	 */
	protected static Map<Float,Map<String,Float>> createMap(Vector<String> series, Vector<Float> x_values)
	{
		Map<Float,Map<String,Float>> out_values = new HashMap<Float,Map<String,Float>>();
		for (float v : x_values)
		{
			Map<String,Float> values = new HashMap<String ,Float>();
			for (String s : series)
			{
				values.put(s, null);
			}
			out_values.put(v, values);
		}
		return out_values;
	}

}
