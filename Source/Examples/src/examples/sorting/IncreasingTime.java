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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.labpal.claim.TrooleanQuantifier.AllObjects;
import ca.uqac.lif.labpal.claim.ExperimentPair;
import ca.uqac.lif.labpal.claim.ExperimentPair.First;
import ca.uqac.lif.labpal.claim.ExperimentPair.Second;
import ca.uqac.lif.labpal.claim.PairClaim;
import ca.uqac.lif.labpal.claim.TrooleanCondition;
import ca.uqac.lif.labpal.claim.ValueOf;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.Fork;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.units.functions.CompareQuantities.QuantityIsLessThan;

import static examples.sorting.SortExperiment.ALGORITHM;
import static examples.sorting.SortExperiment.DURATION;

public class IncreasingTime extends DurationComparison
{
	public IncreasingTime(ExperimentFactory<?> factory, Region r)
	{
		super(factory, r);
		setStatement("A longer list always takes a longer time to sort.");
		setDescription("For every algorithm, the sorting time increases "
				+ "monotonically with list size, i.e. a list with more elements takes "
				+ "more time to sort than a list with fewer elements. The claim"
				+ "verifies this by comparing the duration of each pair of "
				+ "experiments of successive length. Note that this comparison "
				+ "is done for each sorting algorithm separately.");
		setNickname("monotonic");
	}

	@Override
	protected void addPairs(List<ExperimentPair> list)
	{
		for (Region alg_r : m_region.all(ALGORITHM))
		{
			List<Point> points = new ArrayList<Point>(alg_r.allPoints());
			Collections.sort(points);
			for (int i = 0; i < points.size() - 1; i++)
			{
				list.add(new ExperimentPair(m_factory.get(points.get(i)), m_factory.get(points.get(i + 1))));
			}
		}
	}
}
