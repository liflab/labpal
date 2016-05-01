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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;

/**
 * A two-dimensional plot showing (x,y) values for multiple data series.
 */
public class Scatterplot extends Plot
{
	/**
	 * Whether to draw each data series with lines between each data point
	 */
	protected boolean m_withLines = false;
	
	/**
	 * Whether to draw each data series with marks for each data point
	 */
	protected boolean m_withPoints = true;
	
	/**
	 * The input parameter in an experiment used to determine to which data
	 * series it belongs
	 */
	protected ArrayList<String> m_seriesNames;
	
	/**
	 * The input parameter in an experiment to use as the x-value of the plot
	 */
	protected String m_xName;
	
	/**
	 * The output parameter in an experiment to use as the y-value of the plot
	 */
	protected String m_yName;
	
	/**
	 * The label used for the x-axis
	 */
	protected String m_xLabel;
	
	/**
	 * The label used for the y-axis
	 */
	protected String m_yLabel;
	
	/**
	 * Creates an empty scatterplot
	 */
	public Scatterplot()
	{
		super();
		m_seriesNames = new ArrayList<String>();
	}
	
	/**
	 * Tells the plot to draw each data series with lines between each
	 * data point
	 * @return This plot
	 */
	public Scatterplot withLines()
	{
		return withLines(true);
	}
	
	/**
	 * Tells the plot to draw each data series with a mark for each
	 * data point
	 * @return This plot
	 */
	public Scatterplot withPoints()
	{
		return withPoints(true);
	}
	
	/**
	 * Tells the plot to group experiment results into data series, according
	 * to an input parameter present in the experiments
	 * @param param The input parameters in an experiment used to determine
	 * to which data series it belongs
	 * @return This plot
	 */
	public Scatterplot groupBy(String ... param)
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
	 * @param label The label for the x axis in the resulting plot
	 * @return This plot
	 */
	public Scatterplot useForX(String param, String label)
	{
		m_xName = param;
		m_xLabel = label;
		return this;
	}
	
	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "x" value 
	 * @param param The output parameter to use for the "y" value
	 * @param label The label for the y axis in the resulting plot
	 * @return This plot
	 */
	public Scatterplot useForY(String param, String label)
	{
		m_yName = param;
		m_yLabel = label;
		return this;
	}
	
	/**
	 * Tells whether to draw each data series with lines between each
	 * data point
	 * @param b True to draw lines, false otherwise
	 * @return This plot
	 */
	public Scatterplot withLines(boolean b)
	{
		m_withLines = b;
		return this;
	}
	
	/**
	 * Tells whether to draw each data series with a mark for each
	 * data point
	 * @param b True to draw points, false otherwise
	 * @return This plot
	 */
	public Scatterplot withPoints(boolean b)
	{
		m_withLines = b;
		return this;
	}
	
	/**
	 * Gets the sorted list of all distinct series names found in the set of
	 * experiments associated to this plot
	 * @return The list of all series names
	 */
	protected Vector<String> getSeriesNames()
	{
		Vector<String> series = new Vector<String>();
		for (int id : m_experimentIds)
		{
			Experiment e = m_lab.getExperiment(id);
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
		for (int id : m_experimentIds)
		{
			Experiment e = m_lab.getExperiment(id);
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

	@Override
	public String toGnuplot(Terminal term)
	{
		String point_string = " with points";
		if (m_withLines)
		{
			if (m_withPoints)
			{
				point_string = " with linespoints";
			}
			else
			{
				point_string = " with lines";
			}
		}
		// Build the table from the values
		Vector<String> series = getSeriesNames();
		Vector<Float> x_values = getXValues();
		Table tab = new Table(series, x_values);
		for (int id : m_experimentIds)
		{
			Experiment e = m_lab.getExperiment(id);
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
			tab.put(ser, x, y);
		}
		// Build GP string from table
		StringBuilder out = new StringBuilder();
		out.append(Plot.getHeader());
		out.append("set terminal ").append(getTerminalName(term)).append("\n");
		out.append("set title \"").append(m_title).append("\"\n");
		out.append("set datafile separator ','\n");
		out.append("set datafile missing \"?\"\n");
		out.append("set xlabel \"").append(m_xLabel).append("\"\n");
		out.append("set ylabel \"").append(m_yLabel).append("\"\n");
		out.append("plot");
		for (int i = 0; i < series.size(); i++)
		{
			if (i > 0)
			{
				out.append(", ");
			}
			String s_name = series.get(i);
			out.append(" '-' using 0:").append(i + 1).append(" title '").append(s_name).append("'").append(point_string);
		}
		out.append("\n");
		// In Gnuplot, if we use the special "-" filename, we must repeat
		// the data as many times as we use it in the plot command; it does not remember it
		for (int i = 0; i < series.size(); i++)
		{
			String data_csv = tab.toCsv(series, x_values);
			out.append(data_csv).append("end\n");
		}
		return out.toString();
	}
}
