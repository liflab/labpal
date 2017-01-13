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
 * A "box and whiskers" diagram displaying statistics about one or more
 * data series. Given a table in the following format:
 * 
 * <table border="1">
 * <tr><th>x</th><th>Min</th><th>Q1</th><th>Q2</th><th>Q3</th><th>Max</th></tr>
 * <tr><td>A</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td></tr>
 * <tr><td>B</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td></tr>
 * <tr><td>C</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td><td>&hellip;</td></tr>
 * </table>
 * a boxplot will display it like this:
 * 
 * <pre>
 * ^
 * |  ---
 * |   |       ---      ---
 * |   |        |        |
 * |  +-+      +-+      +-+
 * |  | |      | |      | |
 * |  | |      +-+      | |
 * |  +-+       |       | |
 * |   |       ---      | |
 * |  ---               +-+
 * |                     |
 * +---+--------+--------+----------&gt;
 *     A        B        C
 * 
 * </pre>
 * 
 * The position of the lines and box boundaries in each data series correspond
 * the the min/max values and quartiles of that series. 
 * 
 * @see ca.uqac.lif.labpal.table.BoxTransformation
 * @author Sylvain Hallé
 *
 */
public interface BoxPlot extends TwoDimensionalPlot
{
	// Empty interface
}
