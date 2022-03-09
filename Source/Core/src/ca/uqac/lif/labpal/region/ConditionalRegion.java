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
public class ConditionalRegion extends ExtensionRegion
{
	/**
	 * Creates a new instance of conditional region.
	 * @param r The region whose points will be filtered according to a
	 * condition
	 * @param condition The condition
	 * @return The new conditional region
	 */
	public static ConditionalRegion filter(/*@ non_null @*/ Region r, /*@ non_null @*/ MembershipCondition condition)
	{
		return new ConditionalRegion(r, condition);
	}
	
	/**
	 * Determines if a point belongs to this region.
	 * @param p The point
	 * @return <tt>true</tt> if the point should be in the region,
	 * <tt>false</tt> otherwise
	 */
	/*@ non_null @*/ protected MembershipCondition m_condition;
	
	public static interface MembershipCondition
	{
		public boolean belongsTo(Point p);
	}
	
	/**
	 * Creates a new conditional region.
	 * @param r The region whose points will be filtered according to a
	 * condition
	 * @param condition The condition
	 */
	public ConditionalRegion(/*@ non_null @*/ Region r, /*@ non_null @*/ MembershipCondition condition)
	{
		super(r.getDimensions());
		m_condition = condition;
		for (Point p : r.allPoints())
		{
			if (m_condition.belongsTo(p))
			{
				add(p);
			}
		}
	}
}
