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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Experiment selector that retrieves all experiments that are dependencies of
 * a given object. More precisely, the selector calculates the
 * <em>transitive closure</em> of the dependency relation starting from a given
 * dependent object.
 * 
 * @since 3.0
 * @see Dependent
 * @author Sylvain Hallé
 */
public class DependencyExperimentSelector extends ConcreteExperimentSelector
{
	/**
	 * The experiment from which dependencies are to be retrieved.
	 */
	/*@ non_null @*/ protected final Dependent<?> m_experiment;
	
	/**
	 * Creates a new dependency experiment selector.
	 * @param lab The lab from which to select experiments
	 * @param e The experiment from which dependencies are to be retrieved
	 */
	public DependencyExperimentSelector(/*@ non_null @*/ Laboratory lab, /*@ non_null @*/ Dependent<?> e)
	{
		super(lab);
		m_experiment = e;
	}

	@Override
	public Set<Experiment> select() 
	{
		Set<Object> deps = new HashSet<Object>();
		Queue<Object> added = new ArrayDeque<Object>();
		added.add(m_experiment);
		while (!added.isEmpty())
		{
			Object e = added.remove();
			if (deps.contains(e))
			{
				continue;
			}
			deps.add(e);
			if (e instanceof Dependent)
			{
				for (Object o_dep : ((Dependent<?>) e).dependsOn())
				{
					if (!deps.contains(o_dep) && !added.contains(o_dep))
					{
						added.add(o_dep);
					}
				}				
			}
		}
		// Filter out to retain only objects that are experiments
		Set<Experiment> exp_dep = new HashSet<Experiment>();
		for (Object o : deps)
		{
			if (o instanceof Experiment)
			{
				exp_dep.add((Experiment) o);
			}
		}
		return exp_dep;
	}
	
	public static List<Experiment> getDependencies(Dependent<?> object)
	{
		DependencyExperimentSelector des = new DependencyExperimentSelector(null, object);
		Set<Experiment> s_exp_deps = des.select();
		List<Experiment> l_exp_deps = new ArrayList<Experiment>();
		l_exp_deps.addAll(s_exp_deps);
		Collections.sort(l_exp_deps);
		return l_exp_deps;
	}
}
