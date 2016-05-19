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
import ca.uqac.lif.parkbench.CliParser.ArgumentMap;
import ca.uqac.lif.parkbench.plot.Scatterplot;
import ca.uqac.lif.parkbench.table.ExperimentTable;

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
		
		// Prepare a table
		ExperimentTable table = new ExperimentTable();
		table.useForX("size").useForY("time").groupBy("name");

		// Initialize experiments
		for (int length = 5000; length <= 40000; length += 5000)
		{
			add(new QuickSort(length), table);
			add(new ShellSort(length), table);
			add(new BubbleSort(length), table);
			add(new GnomeSort(length), table);
		}
		
		// Prepare a plot from the results of the table
		Scatterplot plot = new Scatterplot(table);
		plot.withLines()
			.labelX("List size")
			.labelY("Time (ms)")
			.setTitle("Comparison of sorting algorithms");
		plot.assignTo(this);
	}
}
