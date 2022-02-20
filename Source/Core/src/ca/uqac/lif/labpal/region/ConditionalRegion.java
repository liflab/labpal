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

/**
 * A region containing points from another region if they respect a condition.
 * @author Sylvain Hallé
 */
public abstract class ConditionalRegion extends ExtensionRegion
{
	/**
	 * Creates a new conditional region.
	 * @param r The region whose points will be filtered according to a
	 * condition
	 */
	public ConditionalRegion(Region r)
	{
		super(r.getDimensions());
		for (Point p : r.allPoints())
		{
			if (belongsTo(p))
			{
				add(p);
			}
		}
	}
	
	/**
	 * Determines if a point belongs to this region.
	 * @param p The point
	 * @return <tt>true</tt> if the point should be in the region,
	 * <tt>false</tt> otherwise
	 */
	public abstract boolean belongsTo(Point p);
}
