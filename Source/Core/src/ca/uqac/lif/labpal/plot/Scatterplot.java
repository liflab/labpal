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
package ca.uqac.lif.labpal.plot;

/**
 * A two-dimensional plot with (x,y) data points that can be joined by
 * line segments.
 * @author Sylvain Hallé
 *
 */
public interface Scatterplot extends TwoDimensionalPlot
{
	/**
	 * Tells whether to draw each data series with a mark for each
	 * data point
	 * @param b True to draw points, false otherwise
	 * @return This plot
	 */
	public Scatterplot withPoints(boolean b);
	
	/**
	 * Tells the plot to draw each data series with a mark for each
	 * data point
	 * @return This plot
	 */
	public Scatterplot withPoints();
	
	/**
	 * Tells whether to draw each data series with lines between each
	 * data point
	 * @param b True to draw lines, false otherwise
	 * @return This plot
	 */
	public Scatterplot withLines(boolean b);
	
	/**
	 * Tells the plot to draw each data series with lines between each
	 * data point
	 * @return This plot
	 */
	public Scatterplot withLines();
}
