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

import ca.uqac.lif.units.si.Meter;

/**
 * Unit tests for {@link DimensionRange}.
 */
public class DimensionRangeTest
{
	@Test//(timeout = 1000)
	public void test1()
	{
		DimensionRange dr = new DimensionRange("foo", new Meter(0), new Meter(5), new Meter(1));
		assertEquals(6, dr.size());
	}
}
