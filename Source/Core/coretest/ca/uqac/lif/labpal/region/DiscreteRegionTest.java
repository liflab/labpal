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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link ExtensionRegion}.
 */
public class DiscreteRegionTest 
{
	@Test
	public void test1()
	{
		PointFactory factory = new PointFactory("a", "b");
		ExtensionRegion r = new ExtensionRegion(factory.get(0, "x"), factory.get(1, "x"), factory.get(0, "y"), factory.get(1, "y"));
		assertFalse(r.isSet("a"));
		assertFalse(r.isSet("b"));
		assertEquals(4, r.size());
		assertEquals("a", r.getDimensions()[0]);
		assertEquals("b", r.getDimensions()[1]);
		Region new_r = r.set("a", 0);
		assertNotEquals(r, new_r);
		assertTrue(new_r.isSet("a"));
		assertFalse(new_r.isSet("b"));
	}
	
	@Test
	public void test2()
	{
		PointFactory factory = new PointFactory("a", "b");
		ExtensionRegion r = new ExtensionRegion(factory.get(0, "x"), factory.get(1, "x"), factory.get(0, "y"));
		assertFalse(r.isSet("a"));
		assertFalse(r.isSet("b"));
		Region new_r = r.set("a", 1);
		assertNotEquals(r, new_r);
		assertTrue(new_r.isSet("a"));
		assertTrue(new_r.isSet("b"));
	}
	
	@Test
	public void testEnumeration1()
	{
		PointFactory factory = new PointFactory("a", "b");
		ExtensionRegion dr = new ExtensionRegion("a", "b");
		for (int a : new int[] {1, 2})
		{
			for (String b : new String[] {"foo", "bar"})
			{
				dr.add(factory.get(a, b));
			}
		}
		List<Region> output = new ArrayList<Region>();
		for (Region r : dr.all("a", "b"))
		{
			output.add(r);
		}
		assertEquals(4, output.size());
	}
	
	@Test
	public void testEnumeration2()
	{
		PointFactory factory = new PointFactory("a", "b");
		ExtensionRegion dr = new ExtensionRegion("a", "b");
		for (int a : new int[] {1, 2})
		{
			for (String b : new String[] {"foo", "bar"})
			{
				dr.add(factory.get(a, b));
			}
		}
		List<Region> output = new ArrayList<Region>();
		for (Region r : dr.all())
		{
			output.add(r);
		}
		assertEquals(4, output.size());
	}
	
	@Test
	public void testEnumeration3()
	{
		PointFactory factory = new PointFactory("a", "b");
		ExtensionRegion dr = new ExtensionRegion("a", "b");
		for (int a : new int[] {1, 2})
		{
			for (String b : new String[] {"foo", "bar"})
			{
				dr.add(factory.get(a, b));
			}
		}
		List<Region> output = new ArrayList<Region>();
		for (Region r : dr.all("a"))
		{
			assertTrue(r.isSet("a"));
			assertEquals(2, r.getDomain("b").size());
			output.add(r);
		}
		assertEquals(2, output.size());
	}
}
