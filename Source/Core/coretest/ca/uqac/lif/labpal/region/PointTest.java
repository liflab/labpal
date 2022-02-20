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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for {@link Point}.
 */
public class PointTest 
{
	@Test
	public void test1()
	{
		Point p1 = new Point();
		p1.set("a", "foo");
		p1.set("b", 3);
		p1.set("c", true);
		Point p2 = new Point();
		p2.set("a", "foo");
		p2.set("b", 3);
		p2.set("c", true);
		assertEquals(p1.hashCode(), p2.hashCode());
		assertTrue(p1.equals(p2));
		assertTrue(p2.equals(p1));
	}
}
