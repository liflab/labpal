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
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.experiment.DependencyExperimentSelector;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Shuffles the experiments in a list in such a way that the objects which
 * depend on them are approximately evenly distributed.
 * <p>
 * The shuffling proceeds according to a greedy algorithm, which works as
 * follows. First, for each dependent object <i>o</i>, a triplet
 * [<i>o</i>, &ell;, n] is initially created, with &ell; being the subset of
 * the experiments of the scheduler's queue which <i>o</i> depends on, and
 * <i>n</i>=0</li>. Then, the following steps proceed until the set of triplets
 * is empty:
 * <ol>
 * <li>The triplet [<i>o</i>, &ell;, n] with the largest value of <i>n</i> is
 * selected (anyone if multiple triplets have the largest <i>n</i>). If
 * &ell; = &emptyset;, the triplet is removed and the iteration restarts</li>
 * <li>If &ell; &ne; &emptyset;, an experiment <i>e</i> in &ell; is picked
 * and added to the output queue.</li>
 * <li>All the triplets [<i>o</i>, &ell;, n] are then updated as follows:
 * <ul>
 * <li>if <i>e</i> &in; &ell;, <i>e</i> is removed and <i>n</i> is set to
 * 0</li>
 * <li>if <i>e</i> &notin; &ell;, <i>n</i> is incremented by 1</li>
 * </ul>
 * </li>
 * </ol>
 * <p>
 * The algorithm picks experiments from the list, and at every step, prefers
 * an experiment for an object that has been "covered" the longest time ago.
 * Overall, this creates a list where experiments cover each object more or
 * less evenly.
 *  
 * @author Sylvain Hallé
 */
public class BalanceObjects implements ExperimentScheduler
{
	/**
	 * An array of collections of dependent objects
	 */
	protected transient Collection<? extends Dependent<?>>[] m_objects;
	
	/**
	 * Creates a new instance of the scheduler.
	 * @param objects A list of collections of dependent objects that need to be
	 * taken into account in the scheduling process
	 */
	@SafeVarargs
	public BalanceObjects(Collection<? extends Dependent<?>> ... objects)
	{
		super();
		m_objects = objects;
	}
	
	@Override
	public List<Experiment> schedule(Collection<Experiment> experiments)
	{
		List<Experiment> list = new ArrayList<Experiment>(experiments.size());
		list.addAll(experiments);
		return scheduleFromList(list, m_objects);
	}

	@Override
	public List<Experiment> schedule(Queue<Experiment> experiments)
	{
		List<Experiment> list = new ArrayList<Experiment>(experiments.size());
		list.addAll(experiments);
		return scheduleFromList(list, m_objects);
	}
	
	/**
	 * Orders a list of experiments.
	 * @param experiments The original list of experiments
	 * @param objects A list of collections of dependent objects
	 * @return The reordered list
	 */
	@SafeVarargs
	protected static List<Experiment> scheduleFromList(List<Experiment> experiments, Collection<? extends Dependent<?>> ... objects)
	{
		List<Experiment> out_list = new ArrayList<Experiment>(experiments.size());
		// Gets triplets
		List<ObjectTriplet> pairs = getDependencies(experiments, objects);
		Collections.sort(pairs);
		Set<Experiment> remaining = new HashSet<Experiment>(experiments.size());
		remaining.addAll(experiments);
		while (!pairs.isEmpty())
		{
			// Pick triplet with largest integer
			ObjectTriplet op = pairs.get(0);
			List<Experiment> exps = op.getExperiments();
			if (exps.isEmpty())
			{
				// All experiments the object depends on are already in list: done with this object
				pairs.remove(0);
				continue;
			}
			Experiment e = exps.get(0);
			out_list.add(e);
			remaining.remove(e);
			for (ObjectTriplet p : pairs)
			{
				p.pick(e);
			}
			Collections.sort(pairs);
		}
		// Adds all remaining experiments on which no object depends
		out_list.addAll(remaining);
		return out_list;
	}

	/**
	 * Creates a list associating lab objects with the subset of a given list of
	 * experiments on which these objects depend. Each element of the list
	 * contains an object, the sublist of experiments it depends on, and an
	 * integer (set to 0).
	 * @param experiments The list of experiments
	 * @param objects A list of collections of dependent objects
	 * @return The list of triplets
	 */
	@SafeVarargs
	protected static List<ObjectTriplet> getDependencies(List<Experiment> experiments, Collection<? extends Dependent<?>> ... objects)
	{
		List<ObjectTriplet> pairs = new ArrayList<ObjectTriplet>();
		for (Collection<? extends Dependent<?>> o_set : objects)
		{
			for (Dependent<?> o : o_set)
			{
				List<Experiment> deps = new ArrayList<Experiment>(experiments);
				deps.retainAll(DependencyExperimentSelector.getDependencyList(o));
				if (!deps.isEmpty())
				{
					pairs.add(new ObjectTriplet(o, deps, 0));
				}
			}
		}
		return pairs;
	}
	
	/**
	 * Association between an object, a list of experiments and an integer.
	 */
	protected static class ObjectTriplet implements Comparable<ObjectTriplet>
	{
		/**
		 * The object in the object triplet.
		 */
		/*@ non_null @*/ protected Object m_object;
		
		/**
		 * The experiments this object depends on.
		 */
		/*@ non_null @*/ protected List<Experiment> m_experiments;
		
		/**
		 * The integer to which this object is associated.
		 */
		protected int m_d;
		
		/**
		 * Creates a new object triplet.
		 * @param o The object in the object triplet
		 * @param exps The experiments this object depends on
		 * @param d The integer to which this object is associated
		 */
		public ObjectTriplet(/*@ non_null @*/ Object o, /*@ non_null @*/ List<Experiment> exps, int d)
		{
			super();
			m_object = o;
			m_experiments = exps;
			this.m_d = d;
		}
		
		/**
		 * Gets the object of this object triplet.
		 * @return The object
		 */
		/*@ pure non_null @*/ public Object getObject()
		{
			return m_object;
		}
		
		/**
		 * Gets the experiments this object depends on.
		 * @return The experiments
		 */
		/*@ pure non_null @*/ public List<Experiment> getExperiments()
		{
			return m_experiments;
		}
		
		/**
		 * Gets the integer of this object triplet.
		 * @return The integer
		 */
		/*@ pure @*/ public int getValue()
		{
			return m_d;
		}
		
		/**
		 * Informs the object triplet that an experiment has been picked. If this
		 * experiment is present in the triplet's list, it is removed and its
		 * integer is reset to 0. If the experiment is not present, it increments
		 * its integer by 1.
		 * @param e
		 */
		public void pick(Experiment e)
		{
			int index = m_experiments.indexOf(e);
			if (index < 0)
			{
				m_d++;
			}
			else
			{
				m_experiments.remove(index);
				m_d = 0;
			}
		}
				
		@Override
		public String toString()
		{
			return m_object + m_experiments.toString() + "\u21a6" + m_d;
		}

		@Override
		public int compareTo(ObjectTriplet p)
		{
			return p.m_d - m_d;
		}
		
		@Override
		public int hashCode()
		{
			return m_object.hashCode();
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof ObjectTriplet))
			{
				return false;
			}
			return m_object.equals(((ObjectTriplet) o).m_object);
		}
	}
}
