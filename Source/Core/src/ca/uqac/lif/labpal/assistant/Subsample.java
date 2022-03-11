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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Picks a subset of the experiments in a list such that each object that
 * depends on these experiments has a minimum fraction <i>t</i> of its
 * dependencies. For example, if <i>t</i> = 0.5, then the resulting
 * list will contain experiments such that each dependent object has
 * at least half of the experiments it depends on. This can be seen as a form
 * of "sub-sampling": instead of running all the experiments that populate
 * a set of plots, this scheduler will pick a subset that is sufficient to
 * populate each plot in part.
 * <p>
 * The selection proceeds according to a greedy algorithm, which works as
 * follows. First, for each dependent object <i>o</i>, a triplet
 * [<i>o</i>, &ell;, <i>n</i>] is initially created, with &ell; being the subset of
 * the experiments of the scheduler's queue which <i>o</i> depends on, and
 * <i>n</i>=|&ell;|</li>. Then, the following steps proceed until the set of triplets
 * is empty:
 * <ol>
 * <li>The triplet [<i>o</i>, &ell;, <i>n</i>] with the lowest value of
 * 1 &minus; |&ell;| / <i>n</i> is selected (anyone if multiple triplets have
 * the lowest value).</li>
 * <li>An experiment <i>e</i> in &ell; is picked and added to the output
 * queue.</li>
 * <li>All the triplets [<i>o</i>, &ell;, n] are then updated such that
 * <i>e</i> is removed if <i>e</i> &in; &ell;. If &ell; = &emptyset; or
 * 1 &minus; |&ell;| / <i>n</i> &geq; <i>t</i>, object <i>o</i> is considered
 * sufficiently covered and the triplet is removed.</li>
 * </ol>
 * <p>
 * The algorithm picks experiments from the list, and at every step, prefers
 * an experiment for an object that has least been covered. The process stops
 * when every object has sufficient coverage. Overall, this creates a list
 * where experiments cover each object more or less evenly.
 * <p>
 * Since the scheduler applies a greedy algorithm, it may result in a selection
 * of experiments that is larger than necessary. To this end, the algorithm can
 * be repeated by passing the selected set of experiments of a previous
 * iteration, which may result in some experiments being further removed. by
 * default, the scheduler repeats the process three times.
 * @since 3.0
 * @author Sylvain Hallé
 */
public class Subsample extends DependencyScheduler
{
	/**
	 * The fraction of each dependent object that should be covered.
	 */
	protected float m_threshold;
	
	/**
	 * Whether to shuffle the list of experiments before selecting.
	 */
	protected boolean m_shuffleFirst = true;
	
	/**
	 * The number of iterations of the process.
	 */
	protected int m_iterations = 3;

	/**
	 * Creates a new instance of the scheduler.
	 * @param threshold A fraction between 0 and 1 specifying the fraction of
	 * each dependent object that must minimally be covered
	 * @param objects A list of collections of dependent objects that need to be
	 * taken into account in the scheduling process
	 */
	@SafeVarargs
	public Subsample(float threshold, Collection<? extends Dependent<?>> ... objects)
	{
		super(objects);
		m_threshold = threshold;
	}
	
	/**
	 * Sets whether the experiment dependencies for each object should be
	 * randomly shuffled before the scheduling process starts. Shuffling reduces
	 * the possibility that experiments selected for each object are clustered
	 * close to each other, and increases the likelihood that selected
	 * experiments are more evenly "spread out".
	 * @param b Set to <tt>true</tt> to shuffle experiments (default),
	 * <tt>false</tt> otherwise
	 * @return This scheduler
	 */
	/*@ non_null @*/ public Subsample shuffleFirst(boolean b)
	{
		m_shuffleFirst = b;
		return this;
	}

	@SuppressWarnings("unchecked")
	protected List<Experiment> scheduleFromList(List<Experiment> experiments, Collection<? extends Dependent<?>> ... objects)
	{
		List<Experiment> start_list = new ArrayList<Experiment>(experiments);
		for (int iteration = 0; iteration < m_iterations; iteration++)
		{
			List<Experiment> out_list = new ArrayList<Experiment>(start_list.size());
			// Gets triplets
			List<ObjectTriplet> ot_pairs = getDependencies(objects);
			List<SubsampleTriplet> pairs = new ArrayList<SubsampleTriplet>(ot_pairs.size());
			for (ObjectTriplet ot : ot_pairs)
			{
				SubsampleTriplet st = (SubsampleTriplet) ot;
				st.m_experiments.retainAll(start_list);
				if (!st.getExperiments().isEmpty() && (iteration > 0 || st.getMaxFraction() >= m_threshold))
				{
					// If an object already has a coverage below the threshold at the
					// start of the process, we do not consider it in the process
					pairs.add(st);
				}
			}
			Collections.sort(pairs);
			while (!pairs.isEmpty())
			{
				// Pick triplet with lowest coverage
				SubsampleTriplet op = pairs.get(0);
				List<Experiment> exps = op.getExperiments();
				if (exps.isEmpty())
				{
					// All experiments the object depends on are already in list:
					// done with this object
					pairs.remove(0);
					continue;
				}
				Experiment e = exps.get(0);
				out_list.add(e);
				Iterator<SubsampleTriplet> it = pairs.iterator();
				while (it.hasNext())
				{
					SubsampleTriplet p = it.next();
					p.pick(e);
					if (p.getFraction() >= m_threshold)
					{
						it.remove(); // Object sufficiently covered
					}
				}
				Collections.sort(pairs);
			}
			if (start_list.size() == out_list.size())
			{
				// This iteration did not remove any experiment; stop
				break;
			}
			start_list = out_list;
		}
		return start_list;
	}

	@Override
	protected SubsampleTriplet getObjectTriplet(Object o, List<Experiment> dependencies)
	{
		if (m_shuffleFirst)
		{
			Collections.shuffle(dependencies);
		}
		return new SubsampleTriplet(o, dependencies);
	}

	/**
	 * Association between an object, a list of experiments and an integer.
	 */
	protected static class SubsampleTriplet extends ObjectTriplet implements Comparable<SubsampleTriplet>
	{
		/**
		 * The total number of experiments the object depends on.
		 */
		protected final int m_total;
		
		/**
		 * The number of dependencies for this object that have been picked
		 * so far.
		 */
		protected int m_picked;

		/**
		 * Creates a new object triplet.
		 * @param o The object in the object triplet
		 * @param exps The experiments this object depends on
		 * @param d The integer to which this object is associated
		 */
		public SubsampleTriplet(/*@ non_null @*/ Object o, /*@ non_null @*/ List<Experiment> exps)
		{
			super(o, exps);
			m_total = exps.size();
			m_picked = 0;
		}

		/**
		 * Informs the object triplet that an experiment has been picked. If this
		 * experiment is present in the triplet's list, it is removed. Otherwise
		 * the triplet remains unchanged.
		 * @param e The experiment
		 */
		public void pick(Experiment e)
		{
			int index = m_experiments.indexOf(e);
			if (index >= 0)
			{
				m_experiments.remove(index);
				m_picked++;
			}
		}

		/**
		 * Gets the fraction of all experiments associated to this tuple that have
		 * already been picked.
		 * @return The fraction, or 1 if no experiments were originally put into
		 * this triplet
		 */
		public float getFraction()
		{
			if (m_total == 0)
			{
				return 1;
			}
			return (float) m_picked / (float) m_total;
		}
		
		/**
		 * Gets the maximum coverage that this object would get if all experiments
		 * were picked.
		 * @return The fraction, or 1 if no experiments were originally put into
		 * this triplet
		 */
		public float getMaxFraction()
		{
			if (m_total == 0)
			{
				return 1;
			}
			return (float) m_experiments.size() / (float) m_total;
		}

		@Override
		public String toString()
		{
			return m_object + m_experiments.toString() + "\u21a6" + getFraction();
		}

		@Override
		public int compareTo(SubsampleTriplet p)
		{
			float diff = getFraction() - p.getFraction();
			if (diff == 0)
			{
				return 0;
			}
			if (diff > 0)
			{
				return 1;
			}
			return -1;
		}
	}
}