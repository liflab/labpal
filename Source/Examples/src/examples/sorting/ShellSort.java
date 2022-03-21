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
package examples.sorting;

import ca.uqac.lif.labpal.region.Point;

/**
 * Shell sort algorithm, as found here:
 * http://stackoverflow.com/a/17543917
 */
public class ShellSort extends SortExperiment
{
	/**
	 * The name of this algorithm.
	 */
	public static final transient String NAME = "Shell Sort";
	
	public ShellSort(Point p)
	{
		super(NAME, p.getInt(SIZE));
	}

	@Override
	public void sort(int[] array)
	{
		int j, comp = 0;
		for( int gap = array.length / 2; gap > 0; gap /= 2 )
		{
			for( int i = gap; i < array.length; i++ )
			{
				int tmp = array[ i ];
				for( j = i; j >= gap && tmp < array[ j - gap ]; j -= gap )
				{
					comp++;
					array[ j ] = array[ j - gap ];
				}
				array[ j ] = tmp;
			}
		}
		writeOutput(COMPARISONS, comp);
	}
}
