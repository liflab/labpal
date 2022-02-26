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

import java.util.Collection;

import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Selects a set of experiments from a lab according to a condition.
 *  
 * @author Sylvain Hallé
 * @since 3.0
 */
public interface ExperimentSelector 
{
	/**
	 * Selects a set of experiments.
	 * @return The set of experiments
	 */
	/*@ non_null @*/ public Collection<Experiment> select();
}
