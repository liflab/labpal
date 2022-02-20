/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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
package ca.uqac.lif.labpal.region;

import java.util.HashSet;
import java.util.Set;

/**
 * A region containing points in the union of other regions.
 * @author Sylvain Hallé
 *
 */
public class Union extends ExtensionRegion
{
	/**
	 * Creates a new intersection.
	 * @param regions The regions to intersect
	 */
	public Union(Region ... regions)
	{
		super(regions[0].getDimensions());
		Set<Point> points = new HashSet<Point>();
		if (regions.length > 0)
		{
			points.addAll(regions[0].allPoints());
		}
		for (int i = 1; i < regions.length; i++)
		{
			points.addAll(regions[i].allPoints());
		}
		addAll(points);
	}
}
