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
 * Unit tests for {@link Subsample}.
 */
public class SubsampleTest
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
		float fraction = 0.5f;
		List<Experiment> exps = getList(1, 2, 3, 4, 5, 6);
		Subsample bo = new Subsample(fraction, deps);
		bo.shuffleFirst(false); // So that test is repeatable
		List<Experiment> scheduled = bo.schedule(exps);
		assertTrue(exps.size() > scheduled.size());
		for (DummyDependent d : deps)
		{
			if (!d.dependsOn().isEmpty())
			{
				float coverage = getCoverage(d, scheduled);
				assertTrue(coverage >= fraction);
				assertTrue(coverage < 1);
			}
		}
	}
	
	protected static float getCoverage(Dependent<Experiment> d, List<Experiment> scheduled)
	{
		Set<Experiment> deps = new HashSet<Experiment>(d.dependsOn());
		float total = deps.size();
		if (total == 0)
		{
			return 1;
		}
		deps.retainAll(scheduled);
		return ((float) deps.size()) / total;
	}

	protected static List<Experiment> getList(int ... exp_ids)
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
