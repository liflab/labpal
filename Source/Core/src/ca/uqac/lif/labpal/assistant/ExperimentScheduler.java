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
import java.util.List;
import java.util.Queue;

import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Agent that is responsible for processing a list of experiments to be run by
 * an assistant and, optionally modifying its contents. The scheduler does so
 * through a method called {@link #schedule(Collection) schedule()}. When given
 * a collection of experiments, the output of this method is a new
 * <em>ordered</em> list resulting from the action of the scheduler. There is
 * no guarantee on the output: some schedulers may reorder the experiments, but
 * may also add new experiments or delete some experiments from the input.
 * <p>
 * A scheduler is applied on the current queue of experiments in an assistant
 * with {@link Assistant#apply(ExperimentScheduler)}.
 * 
 * @since 3.0
 * @author Sylvain Hallé
 *
 */
public interface ExperimentScheduler 
{
	/**
	 * Organizes a collection of experiments.
	 * @param experiments The experiments to organize
	 * @return The new list of experiments
	 */
	/*@ non_null @*/ public List<Experiment> schedule(/*@ non_null @*/ Collection<Experiment> experiments);
	
	/**
	 * Organizes a queue of experiments.
	 * @param experiments The experiments to organize
	 * @return The new list of experiments
	 */
	/*@ non_null @*/ public List<Experiment> schedule(/*@ non_null @*/ Queue<Experiment> experiments);
	
	/**
	 * Resets the internal state of the scheduler to be ready to schedule a new
	 * collection of experiments.
	 */
	public void restart();
}
