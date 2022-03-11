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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Common ancestor to experiment schedulers that base their decision on a
 * random process.
 * @since 3.0
 * @author Sylvain Hallé
 *
 */
public abstract class RandomExperimentScheduler implements ExperimentScheduler
{
	/**
	 * The random object used to get random Boolean values.
	 */
	/*@ non_null @*/ protected transient Random m_random;
	
	/**
	 * Creates a new instance of the scheduler, using a new instance of Java's
	 * {@link Random} object as its source of randomness.
	 */
	public RandomExperimentScheduler()
	{
		super();
		m_random = new Random();
	}
	
	/**
	 * Creates a new instance of the scheduler, using a specific instance of
	 * Java's {@link Random} object as its source of randomness.
	 * @param r The Random object
	 */
	public RandomExperimentScheduler(Random r)
	{
		super();
		m_random = r;
	}
	
	@Override
	public List<Experiment> schedule(Collection<Experiment> experiments)
	{
		List<Experiment> list = new ArrayList<Experiment>(experiments.size());
		list.addAll(experiments);
		return scheduleFromList(list);
	}

	@Override
	public List<Experiment> schedule(Queue<Experiment> experiments)
	{
		List<Experiment> list = new ArrayList<Experiment>(experiments.size());
		list.addAll(experiments);
		return scheduleFromList(list);
	}
	
	/**
	 * Selects the experiments from a list.
	 * @param experiments The list of experiments
	 * @return The new list of experiments
	 */
	protected abstract List<Experiment> scheduleFromList(List<Experiment> experiments);
	
	@Override
	public void restart()
	{
		// Nothing to do
	}
}
