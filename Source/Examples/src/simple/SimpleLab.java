/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hall�

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
package simple;

import ca.uqac.lif.parkbench.CliParser.ArgumentMap;
import ca.uqac.lif.parkbench.plot.BarPlot;
import ca.uqac.lif.parkbench.plot.Scatterplot;
import ca.uqac.lif.parkbench.table.ExperimentTable;
import ca.uqac.lif.parkbench.Laboratory;

public class SimpleLab extends Laboratory
{
	public static void main(String[] args)
	{
		initialize(args, SimpleLab.class);
	}
	
	@Override
	public void setupExperiments(ArgumentMap map)
	{
		// Sets the title of this lab
		setTitle("Two simple experiments");
		// Put the results of the experiments in tables
		ExperimentTable table1 = new ExperimentTable();
		table1.useForX("a").useForY("y").groupBy("name");
		ExperimentTable table2 = new ExperimentTable();
		table2.useForX("name").useForY("y").groupBy("a");
		// Create the experiments
		for (int i = 0; i < 5; i++)
		{
			add(new ExperimentA(i), table1, table2);
			add(new ExperimentB(i), table1, table2);
		}
		// Prepare a scatterplot
		Scatterplot my_plot = new Scatterplot(table1);
		my_plot.labelX("Value of a").labelY("Return value").setTitle("My plot").assignTo(this);
		BarPlot b_plot = new BarPlot(table2);
		b_plot.labelX("Value of a").labelY("Return value").setTitle("My plot").assignTo(this);
	}
}
