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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.experiment.ConcreteExperimentSelector;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Selects the experiments necessary to evaluate a claim.
 * @author Sylvain Hallé
 *
 */
public class ClaimExperimentSelector extends ConcreteExperimentSelector 
{
	/**
	 * The claim relative to which the experiments are selected.
	 */
	/*@ non_null @*/ protected final Claim m_claim;
	
	/**
	 * Creates a new instance of the selector.
	 * @param lab The lab from which to select experiments
	 * @param c The claim relative to which the experiments are selected
	 */
	public ClaimExperimentSelector(/*@ non_null @*/ Laboratory lab, /*@ non_null @*/ Claim c)
	{
		super(lab);
		m_claim = c;
	}

	@Override
	/*@ non_null @*/ public Collection<Experiment> select()
	{
		Set<Experiment> deps = new HashSet<Experiment>();
		for (Stateful s : m_claim.dependsOn())
		{
			if (s instanceof Experiment)
			{
				deps.add((Experiment) s);
			}
		}
		return deps;
	}
}
