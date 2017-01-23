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
package multipoint;

import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.gral.Scatterplot;
import ca.uqac.lif.labpal.table.BoxTransformation;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.labpal.table.TransformedTable;

/**
 * Create an experiment that generates multiple values for some of
 * its parameters.
 * <p>
 * In this example, the {@link MultipointExperiment}, when run, fills
 * its parameters {@code a} and {@code b} with two <em>lists</em> of
 * numbers. When given such an experiment, a table expands these two
 * lists into as many entries as there are pairs of values. Hence, an
 * experiment whose parametrs are as follows:
 * <pre>
 * a : [0, 1, 2, 3]
 * b : [4, 5, 6, 7]
 * c : 8
 * </pre>
 * will result in the following table:
 * <table border="1">
 * <tr><th>a</th><th>b</th><th>c</th></tr>
 * <tr><td>0</td><td>4</td><td>8</td></tr>
 * <tr><td>1</td><td>5</td><td>8</td></tr>
 * <tr><td>2</td><td>6</td><td>8</td></tr>
 * <tr><td>3</td><td>7</td><td>8</td></tr>
 * </table>
 */
public class MultipointLab extends Laboratory 
{
	@Override
	public void setup() 
	{
		ExperimentTable table = new ExperimentTable("a", "b");
		add(table);
		Scatterplot plot = new Scatterplot(table);
		add(plot);
		add(new MultipointExperiment(), table);
		TransformedTable tt = new TransformedTable(new BoxTransformation(), table);
		add(tt);
	}

	public static class MultipointExperiment extends Experiment 
	{
		public MultipointExperiment()
		{
			super();
		}

		@Override
		public void execute()
		{
			JsonList list_x = new JsonList();
			JsonList list_y = new JsonList();
			for (int i = 0; i < 10; i++)
			{
				list_x.add(i);
				list_y.add(2*i);
			}
			write("a", list_x);
			write("b", list_y);
			write("c", 8); // Dummy value
		}
	}
	
	public static void main(String[] args)
	{
		initialize(args, MultipointLab.class);
	}
}
