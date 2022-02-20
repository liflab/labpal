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
 * Unit tests for {@link ProductRegion}. Since all methods in this class are
 * directly inherited from its parent {@link ExtensionRegion}, only a few tests
 * on the object's construction are conducted here.  
 */
public class ProductRegionTest 
{
	@Test
	public void test1()
	{
		ProductRegion r = new ProductRegion(
				new ExtensionDomain<String>("a", "foo", "bar"), 
				new ExtensionDomain<Integer>("b", 0, 1, 2));
		assertEquals(6, r.size());
	}
}
