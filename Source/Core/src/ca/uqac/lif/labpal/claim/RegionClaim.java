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
package ca.uqac.lif.labpal.claim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.petitpoucet.function.Function;

/**
 * A claim expressed as a function over a set of experiments obtained from a
 * region passed to a factory.
 * @author Sylvain Hallé
 * @since 3.0
 */
public class RegionClaim extends FunctionClaim
{
	/*@ non_null @*/ protected final ExperimentFactory<?> m_factory;

	/*@ non_null @*/ protected final Region m_region;

	public RegionClaim(/*@ non_null @*/ Function condition, /*@ non_null @*/ ExperimentFactory<?> factory, /*@ non_null @*/ Region r)
	{
		super(condition);
		m_factory = factory;
		m_region = r;
		m_input = null;
	}

	@Override
	public Collection<Stateful> dependsOn()
	{
		Set<Stateful> set = new HashSet<Stateful>();
		set.addAll(m_factory.get(m_region));
		return set;
	}

	@Override
	protected Object[] getInput()
	{
		List<Experiment> exps = new ArrayList<Experiment>();
		exps.addAll(m_factory.get(m_region));
		return new Object[] {exps};
	}
}
