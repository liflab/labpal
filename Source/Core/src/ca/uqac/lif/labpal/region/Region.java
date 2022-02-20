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

import java.util.Set;

/**
 * An object representing a <em>discrete</em> set of sets of key-value pairs,
 * which can be interrogated and iterated over in various ways.
 * <p>
 * A region is an <em>immutable</em> object. All operations that modify the
 * state of a region return a new region instance. The original region instance
 * remains intact.
 * 
 * @author Sylvain Hallé
 */
public interface Region 
{
	/**
	 * Enumerates sub-regions of the current region, by setting some
	 * of its dimensions successively to all combinations of values in their
	 * domains.
	 * @param names The names of the dimensions to iterate over
	 * @return An enumeration of all sub-regions
	 */
	/*@ non_null @*/ public Iterable<Region> all(String ... names);
	
	/**
	 * Produces a new region by restricting the domain of a dimension to a
	 * single value in the current region.
	 * @param name The name of the dimension
	 * @param value The value to give to this dimension
	 * @return The new "flattened" region
	 */
	/*@ non_null @*/ public Region set(/*@ non_null @*/ String name, /*@ null @*/ Object value);
	
	/**
	 * Determines if a dimension of the region is set. The dimension is set
	 * when all points in the region have the same value for this dimension.
	 * @param name The name of the dimension
	 * @return <tt>true</tt> if the dimension is set, <tt>false</tt> if it does
	 * not exist or has a multi-value domain
	 */
	public boolean isSet(/*@ non_null @*/ String name);
	
	/**
	 * Gets the set of all points contained within this region.
	 * @return The set of points
	 */
	/*@ non_null @*/ public Set<Point> allPoints();
	
	/**
	 * Gets the number of points contained within the region.
	 * @return The size of the region
	 */
	public int size();
	
	/**
	 * Gets the domain associated to a dimension. This domain contains
	 * all the values in this dimension that are taken by at least <em>one</em>
	 * point in the region.
	 * @param name The name of the dimension
	 * @return The domain (which may be empty)
	 */
	/*@ non_null @*/ public Set<Object> getDomain(/*@ non_null @*/ String name);

	/**
	 * Gets the names of all the dimensions defined in this region.
	 * @return The dimension names
	 */
	/*@ non_null @*/ public String[] getDimensions();
}
