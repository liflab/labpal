/*
    ParkBench, a versatile benchmark environment
    Copyright (C) 2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sorting;

import ca.uqac.lif.parkbench.Laboratory;
import ca.uqac.lif.parkbench.Scatterplot;
import ca.uqac.lif.parkbench.CliParser.ArgumentMap;

public class SortingLab extends Laboratory
{
	public static void main(String[] args)
	{
		initialize(args, SortingLab.class);
	}

	public void setupExperiments(ArgumentMap map)
	{
		// Give a name to the lab
		setTitle("Sorting Algorithms");
		
		// Prepare a plot
		Scatterplot plot = new Scatterplot();
		plot.withLines()
			.useForX("size", "List size")
			.useForY("time", "Time (ms)")
			.groupBy("name")
			.setTitle("Comparison of sorting algorithms");
		plot.assignTo(this);

		// Initialize experiments
		for (int length = 5000; length <= 40000; length += 5000)
		{
			add(new QuickSort(length), plot);
			add(new ShellSort(length), plot);
			add(new BubbleSort(length), plot);
			add(new GnomeSort(length), plot);
		}
	}
}
