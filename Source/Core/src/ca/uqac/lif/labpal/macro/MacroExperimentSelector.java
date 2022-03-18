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
package ca.uqac.lif.labpal.macro;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.experiment.ConcreteExperimentSelector;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Selects the experiments necessary to evaluate a macro, if this macro depends
 * on experiments.
 * @author Sylvain Hallé
 *
 */
public class MacroExperimentSelector extends ConcreteExperimentSelector 
{
	/**
	 * The macro relative to which the experiments are selected.
	 */
	/*@ non_null @*/ protected final Macro m_macro;

	/**
	 * Creates a new instance of the selector.
	 * @param lab The lab from which to select experiments
	 * @param m The macro relative to which the experiments are selected
	 */
	public MacroExperimentSelector(/*@ non_null @*/ Laboratory lab, /*@ non_null @*/ Macro m)
	{
		super(lab);
		m_macro = m;
	}

	@Override
	/*@ non_null @*/ public Collection<Experiment> select()
	{
		Set<Experiment> deps = new HashSet<Experiment>();
		if (m_macro instanceof Dependent)
		{
			for (Object o : ((Dependent<?>) m_macro).dependsOn())
			{
				if (o instanceof Experiment)
				{
					deps.add((Experiment) o);
				}
			}
		}
		return deps;
	}
}
