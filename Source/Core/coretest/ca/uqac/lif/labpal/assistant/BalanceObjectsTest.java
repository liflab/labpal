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
package ca.uqac.lif.labpal.assistant;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.Identifiable;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Unit tests for {@link BalanceObjects}.
 */
public class BalanceObjectsTest
{
	protected static Experiment[] s_experiments = new Experiment[15];

	static
	{
		for (int i = 0; i < 14; i++)
		{
			s_experiments[i+1] = new Experiment();
		}
	}

	@Test
	public void testSchedule1()
	{
		Set<DummyDependent> deps = new HashSet<DummyDependent>();
		DummyDependent d1 = new DummyDependent(1, 3, 5).setId(1);
		DummyDependent d2 = new DummyDependent(2, 4, 6).setId(2);
		deps.add(d1);
		deps.add(d2);
		List<Experiment> exps = getList(1, 2, 3, 4, 5, 6);
		BalanceObjects bo = new BalanceObjects(deps);
		List<Experiment> scheduled = bo.schedule(exps);
		assertEquals(exps.size(), scheduled.size());
		for (int i = 0; i < scheduled.size() - 1; i++)
		{
			if (d1.dependsOn().contains(scheduled.get(i)))
			{
				assertFalse(d1.dependsOn().contains(scheduled.get(i + 1)));
			}
			if (d2.dependsOn().contains(scheduled.get(i)))
			{
				assertFalse(d2.dependsOn().contains(scheduled.get(i + 1)));
			}
		}
	}

	@Test
	public void testSchedule2()
	{
		Set<DummyDependent> deps = new HashSet<DummyDependent>();
		DummyDependent d1 = new DummyDependent(1, 3, 5).setId(1);
		DummyDependent d2 = new DummyDependent(2, 4, 6).setId(2);
		deps.add(d1);
		deps.add(d2);
		List<Experiment> exps = getList(1, 2, 3, 4, 5, 6, 7, 8);
		BalanceObjects bo = new BalanceObjects(deps);
		List<Experiment> scheduled = bo.schedule(exps);
		assertEquals(exps.size(), scheduled.size());
		for (int i = 0; i < scheduled.size() - 3; i++)
		{
			if (d1.dependsOn().contains(scheduled.get(i)))
			{
				assertFalse(d1.dependsOn().contains(scheduled.get(i + 1)));
			}
			if (d2.dependsOn().contains(scheduled.get(i)))
			{
				assertFalse(d2.dependsOn().contains(scheduled.get(i + 1)));
			}
		}
		// Experiments 7 and 8 are at the end
		for (int i = 6; i < 8; i++)
		{
			Experiment e = scheduled.get(i);
			assertTrue(e.getId() > 6);
		}
	}

	protected List<Experiment> getList(int ... exp_ids)
	{
		List<Experiment> list = new ArrayList<Experiment>(exp_ids.length);
		for (int id : exp_ids)
		{
			list.add(s_experiments[id]);
		}
		return list;
	}

	protected class DummyDependent implements Dependent<Experiment>, Identifiable
	{
		protected int m_id;

		protected List<Experiment> m_dependencies;

		public DummyDependent(int ... exp_ids)
		{
			super();
			m_dependencies = new ArrayList<Experiment>(exp_ids.length);
			for (int id : exp_ids)
			{
				m_dependencies.add(s_experiments[id]);
			}
		}

		public DummyDependent setId(int id)
		{
			m_id = id;
			return this;
		}

		@Override
		public int getId()
		{
			return m_id;
		}

		public DummyDependent(Experiment ... experiments)
		{
			super();
			m_dependencies = Arrays.asList(experiments);
		}

		@Override
		public Collection<Experiment> dependsOn()
		{
			return m_dependencies;
		}

		@Override
		public String toString()
		{
			return "D" + m_id;
		}

	}
}
