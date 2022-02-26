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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.labpal.assistant.ExperimentSelector;

/**
 * Selects a set of experiments from a lab.
 *  
 * @author Sylvain Hallé
 * @since 3.0
 */
public class IntersectionExperimentSelector implements ExperimentSelector
{
	/**
	 * The selectors to take the intersection of.
	 */
	/*@ non_null @*/ protected final ExperimentSelector[] m_selectors;
	
	/**
	 * Creates a new instance of the selector.
	 * @param selectors The selectors to take the intersection of
	 */
	public IntersectionExperimentSelector(ExperimentSelector ... selectors)
	{
		super();
		m_selectors = selectors;
	}

	@Override
	/*@ non_null @*/ public Collection<Experiment> select()
	{
		Set<Experiment> set = new HashSet<Experiment>();
		if (m_selectors.length > 0)
		{
			set.addAll(m_selectors[0].select());
		}
		for (int i = 1; i < m_selectors.length; i++)
		{
			set.retainAll(m_selectors[i].select());
		}
		return set;
	}
}
