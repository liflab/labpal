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

import ca.uqac.lif.parkbench.FileHelper;
import ca.uqac.lif.parkbench.table.Table;

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
	public TwoDeePlot(Table t)
	{
		super();
		m_table = t;
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
	 * Tells the plot the label for the "x" axis
	 * @param label The label for the x axis in the resulting plot
	 * @return This plot
	 */
	public TwoDeePlot labelX(String label)
	{
		m_xLabel = label;
		return this;
	}
	
	/**
	 * Tells the plot the label for the "y" axis
	 * @param label The label for the y axis in the resulting plot
	 * @return This plot
	 */
	public TwoDeePlot labelY(String label)
	{
		m_yLabel = label;
		return this;
	}
		
	@Override
	public StringBuilder getHeader(Terminal term, String lab_title)
	{
		StringBuilder out = super.getHeader(term, lab_title);
		out.append("set xlabel \"").append(m_xLabel).append("\"").append(FileHelper.CRLF);
		out.append("set ylabel \"").append(m_yLabel).append("\"").append(FileHelper.CRLF);
		if (m_logscaleX)
		{
			out.append("set logscale x").append(FileHelper.CRLF);
		}
		if (m_logscaleY)
		{
			out.append("set logscale y").append(FileHelper.CRLF);
		}
		return out;
	}

}
