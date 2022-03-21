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
package examples.sorting;

import java.util.List;

import ca.uqac.lif.labpal.claim.ExperimentPair;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.labpal.region.Region;

import static examples.sorting.SortExperiment.ALGORITHM;
import static examples.sorting.SortExperiment.SIZE;

public class SlowerThan extends DurationComparison
{
	protected String m_algorithm1;
	
	protected String m_algorithm2;
	
	public SlowerThan(String alg1, String alg2, ExperimentFactory<?> factory, Region r)
	{
		super(factory, r);
		m_algorithm1 = alg1;
		m_algorithm2 = alg2;
		setStatement(alg1 + " is slower than " + alg2 + ".");
		setDescription("Sorting time for " + alg1 + " is longer than for "
				+ alg2 + " for any list length.");
		setNickname("slower");
	}

	@Override
	protected void addPairs(List<ExperimentPair> list)
	{
		for (Region alg_r : m_region.all(SIZE))
		{
			Point p1 = alg_r.set(ALGORITHM, m_algorithm1).asPoint();
			Point p2 = alg_r.set(ALGORITHM, m_algorithm2).asPoint();
			list.add(new ExperimentPair(m_factory.get(p1), m_factory.get(p2)));
		}
	}
}
