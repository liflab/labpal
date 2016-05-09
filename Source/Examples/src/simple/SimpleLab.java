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

		// Prepare a scatterplot
		Scatterplot my_plot = new Scatterplot();
		my_plot.useForX("a", "Value of a")
			.useForY("y", "Return value").groupBy("name")
			.setTitle("My plot").assignTo(this);
		BarPlot b_plot = new BarPlot();
		b_plot.useForX("name", "Name")
			.useForY("y", "Return value").groupBy("x")
			.setTitle("My bar plot").assignTo(this);

		// Create the experiments
		for (int i = 0; i < 5; i++)
		{
			add(new ExperimentA(i), my_plot, b_plot);
			add(new ExperimentB(i), my_plot, b_plot);
		}
	}
}
