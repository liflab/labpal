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
package ca.uqac.lif.labpal.experiment;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.labpal.region.PointFactory;

/**
 * Unit tests for {@link ExperimentFactory}.
 */
public class SingleClassExperimentFactoryTest 
{
	protected static PointFactory s_factory = new PointFactory("a", "b", "c");
	
	@Test
	public void test1()
	{
		Laboratory lab = new Laboratory();
		ExperimentFactory<PointExperiment> f = new SingleClassExperimentFactory<PointExperiment>(lab, PointExperiment.class);
		PointExperiment e = f.get(s_factory.get("foo", 3, true));
		assertTrue(lab.contains(e));
		assertEquals("foo", e.read("a"));
		assertEquals(3, e.read("b"));
		assertEquals(true, e.read("c"));
		PointExperiment e2 = f.get(s_factory.get("foo", 3, true));
		assertEquals(e, e2);
	}
	
	@Test
	public void test2()
	{
		Laboratory lab = new Laboratory();
		ExperimentFactory<Experiment> f = new SingleClassExperimentFactory<Experiment>(lab, Experiment.class);
		Experiment e = f.get(s_factory.get("foo", 3, true));
		assertNull(e); // Since Experiment does not have a constructor accepting a Point
	}
	
	public static class PointExperiment extends Experiment
	{
		public PointExperiment(Point p) throws ExperimentException
		{
			super(p);
		}
	}
}
