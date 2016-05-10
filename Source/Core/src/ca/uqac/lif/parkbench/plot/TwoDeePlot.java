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
package ca.uqac.lif.parkbench.plot;

import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.Laboratory;

public abstract class TwoDeePlot extends Plot
{
	/**
	 * The underlying table of values for this plot
	 */
	protected Table m_table;
	
	/**
	 * The label used for the x-axis
	 */
	protected String m_xLabel;
	
	/**
	 * The label used for the y-axis
	 */
	protected String m_yLabel;
	
	/**
	 * Whether the x-axis has a logarithmic scale
	 */
	protected boolean m_logscaleX = false;
	
	/**
	 * Whether the y-axis has a logarithmic scale
	 */
	protected boolean m_logscaleY = false;
	
	/**
	 * Creates an empty 2D plot
	 */
	public TwoDeePlot()
	{
		super();
		m_table = new Table();
	}
	
	@Override
	public TwoDeePlot add(Experiment e)
	{
		m_table.addExperiment(e);
		return this;
	}
	
	@Override
	public Plot assignTo(Laboratory a)
	{
		Table tab = new Table();
		tab.m_xName = m_table.m_xName;
		tab.m_yName = m_table.m_yName;
		tab.m_seriesNames = m_table.m_seriesNames;
		for (Experiment e : m_table.m_experiments)
		{
			int exp_id = e.getId();
			Experiment new_e = a.getExperiment(exp_id);
			tab.addExperiment(new_e);
		}
		m_table = tab;
		super.assignTo(a);
		return this;
	}
	
	/**
	 * Tells the plot to draw the x axis with a logarithmic scale
	 * @return This plot
	 */
	public TwoDeePlot setLogscaleX()
	{
		m_logscaleX = true;
		return this;
	}
	
	/**
	 * Tells the plot to draw the y axis with a logarithmic scale
	 * @return This plot
	 */
	public TwoDeePlot setLogscaleY()
	{
		m_logscaleY = true;
		return this;
	}
	
	/**
	 * Tells the plot to group experiment results into data series, according
	 * to a parameter present in the experiments
	 * @param param The input parameters in an experiment used to determine
	 * to which data series it belongs
	 * @return This plot
	 */
	public TwoDeePlot groupBy(String ... param)
	{
		m_table.groupBy(param);
		return this;
	}
	
	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "x" value 
	 * @param param The output parameter to use for the "x" value
	 * @param label The label for the x axis in the resulting plot
	 * @return This plot
	 */
	public TwoDeePlot useForX(String param, String label)
	{
		m_table.useForX(param);
		m_xLabel = label;
		return this;
	}
	
	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "x" value 
	 * @param param The output parameter to use for the "x" value
	 * @return This plot
	 */
	public TwoDeePlot useForX(String param)
	{
		return useForX(param, "");
	}
	
	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "x" value 
	 * @param param The output parameter to use for the "y" value
	 * @param label The label for the y axis in the resulting plot
	 * @return This plot
	 */
	public TwoDeePlot useForY(String param, String label)
	{
		m_table.useForY(param);
		m_yLabel = label;
		return this;
	}
	
	/**
	 * Tells the plot what input parameter of the experiments to use as the
	 * "x" value 
	 * @param param The output parameter to use for the "y" value
	 * @return This plot
	 */
	public TwoDeePlot useForY(String param)
	{
		return useForY(param, "");
	}
	
	@Override
	public StringBuilder getHeader(Terminal term, String lab_title)
	{
		StringBuilder out = super.getHeader(term, lab_title);
		out.append("set xlabel \"").append(m_xLabel).append("\"\n");
		out.append("set ylabel \"").append(m_yLabel).append("\"\n");
		if (m_logscaleX)
		{
			out.append("set logscale x\n");
		}
		if (m_logscaleY)
		{
			out.append("set logscale y\n");
		}
		return out;
	}

}
