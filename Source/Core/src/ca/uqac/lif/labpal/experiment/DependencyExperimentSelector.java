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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import ca.uqac.lif.labpal.Laboratory;

/**
 * Experiment selector that retrieves all experiments that are dependencies of
 * a given experiment.
 * 
 * @since 3.0
 * 
 * @author Sylvain Hallé
 */
public class DependencyExperimentSelector extends ConcreteExperimentSelector
{
	/**
	 * The experiment from which dependencies are to be retrieved.
	 */
	/*@ non_null @*/ protected final Experiment m_experiment;
	
	/**
	 * Creates a new dependency experiment selector.
	 * @param lab The lab from which to select experiments
	 * @param e The experiment from which dependencies are to be retrieved
	 */
	public DependencyExperimentSelector(/*@ non_null @*/ Laboratory lab, /*@ non_null @*/ Experiment e)
	{
		super(lab);
		m_experiment = e;
	}

	@Override
	public Set<Experiment> select() 
	{
		Set<Experiment> deps = new HashSet<Experiment>();
		Queue<Experiment> added = new ArrayDeque<Experiment>();
		added.add(m_experiment);
		while (!added.isEmpty())
		{
			Experiment e = added.remove();
			if (deps.contains(e))
			{
				continue;
			}
			deps.add(e);
			for (int dep : e.dependsOn())
			{
				Experiment e_dep = m_laboratory.getExperiment(dep);
				if (e_dep != null && !deps.contains(e_dep) && !added.contains(e_dep))
				{
					added.add(e_dep);
				}
			}
		}
		return deps;
	}
}
