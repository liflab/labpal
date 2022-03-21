/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

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
package examples.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.labpal.region.Point;

/**
 * "Bad sort" algorithm. It is not really a sorting algorithm, and is used in
 * this demo to show examples of what can go wrong in an experiment.
 */
public class BadSort extends SortExperiment
{
	/**
	 * The name of this algorithm.
	 */
	public static final transient String NAME = "Bad Sort";
	
	public BadSort(Point p)
	{
		super(NAME, p.getInt(SIZE));
	}

	@Override
	public void sort(int[] array) throws ExperimentException
	{
		/* We deliberately throw an exception for the smallest list. */
		if (readInt(SIZE) == 5000)
		{
			throw new ExperimentException("Bad Sort refuses to sort this list");
		}
		/* Otherwise, we first correctly sort the array using Java's own sorting
		 * method. */
		List<Integer> list = new ArrayList<Integer>(array.length);
		for (int i : array)
		{
			list.add(i);
		}
		Collections.sort(list);
		/* We then deliberately flip two elements, but only do so if the list
		 * size is a multiple of 20000 (to make the error more subtle). */
		int pos1 = 0, pos2 = 0;
		if (readInt(SIZE) % 20000 == 0)
		{
			pos1 = (int) (Math.random() * 20000);
			pos2 = (int) (Math.random() * 20000);
		}
		/* Finally, we copy the list back into the array. */
		for (int i = 0; i < array.length; i++)
		{
			if (i == pos1)
			{
				// Flips pos1 with pos2
				array[i] = list.get(pos2);
			}
			else if (i == pos2)
			{
			// Flips pos2 with pos1
				array[i] = list.get(pos1);
			}
			else
			{
				array[i] = list.get(i);
			}
		}
	}
}
