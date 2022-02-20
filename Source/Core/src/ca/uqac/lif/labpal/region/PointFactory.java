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
 * A utility class to facilitate the instantiation of points with common
 * dimensions.
 * 
 * @since 3.0
 * 
 * @author Sylvain Hallé
 */
public class PointFactory
{
	private final String[] m_dimensions;
	
	public PointFactory(String ... dimensions)
	{
		super();
		m_dimensions = dimensions;
	}
	
	public Point get(Object ... values)
	{
		Point p = new Point();
		for (int i = 0; i < Math.min(m_dimensions.length, values.length); i++)
		{
			p.set(m_dimensions[i], values[i]);
		}
		return p;
	}
}