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
import java.util.List;
import java.util.Random;

import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * A scheduler that randomly removes a subset of a list of experiments. The
 * scheduler does so by iterating through the list and flipping a coin on
 * each to decide if it should be kept. As a result, the output list preserves
 * the relative ordering of experiments, and is expected to have a size of
 * about half that of the original.
 * 
 * @since 3.0
 * @author Sylvain Hallé
 *
 */
public class RandomDecimate extends RandomExperimentScheduler
{
	/**
	 * Creates a new instance of the scheduler, using a new instance of Java's
	 * {@link Random} object as its source of randomness.
	 */
	public RandomDecimate()
	{
		super();
	}
	
	/**
	 * Creates a new instance of the scheduler, using a specific instance of
	 * Java's {@link Random} object as its source of randomness.
	 * @param r The Random object
	 */
	public RandomDecimate(Random r)
	{
		super(r);
	}
	
	@Override
	protected List<Experiment> scheduleFromList(List<Experiment> experiments)
	{
		List<Experiment> out_list = new ArrayList<Experiment>();
		for (int i = 0; i < experiments.size(); i ++)
		{
			if (m_random.nextBoolean())
			{
				out_list.add(experiments.get(i));
			}
		}
		return out_list;
	}
}
