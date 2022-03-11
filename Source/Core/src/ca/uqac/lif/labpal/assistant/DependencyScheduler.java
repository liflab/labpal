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

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.experiment.DependencyExperimentSelector;
import ca.uqac.lif.labpal.experiment.Experiment;

/**
 * Common ancestor to schedulers whose processing is based on the
 * objects which depend on the experiments in the queue.
 * @since 3.0 
 * @author Sylvain Hallé
 *
 */
public abstract class DependencyScheduler implements ExperimentScheduler
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
	public DependencyScheduler(Collection<? extends Dependent<?>> ... objects)
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
	 * Creates a list associating lab objects with the subset of a given list of
	 * experiments on which these objects depend. Each element of the list
	 * contains an object, the sublist of experiments it depends on, and an
	 * integer (set to 0).
	 * @param experiments The list of experiments
	 * @param objects A list of collections of dependent objects
	 * @return The list of triplets
	 */
	@SuppressWarnings("unchecked")
	protected List<ObjectTriplet> getDependencies(List<Experiment> experiments, Collection<? extends Dependent<?>> ... objects)
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
					pairs.add(getObjectTriplet(o, deps));
				}
			}
		}
		return pairs;
	}
	
	/**
	 * Creates a list associating lab objects the  experiments on which these
	 * objects depend. Each element of the list contains an object, the list of
	 * experiments it depends on, and an integer (set to 0).
	 * @param objects A list of collections of dependent objects
	 * @return The list of triplets
	 */
	@SuppressWarnings("unchecked")
	protected List<ObjectTriplet> getDependencies(Collection<? extends Dependent<?>> ... objects)
	{
		List<ObjectTriplet> pairs = new ArrayList<ObjectTriplet>();
		for (Collection<? extends Dependent<?>> o_set : objects)
		{
			for (Dependent<?> o : o_set)
			{
				List<Experiment> deps = DependencyExperimentSelector.getDependencyList(o);
				if (!deps.isEmpty())
				{
					pairs.add(getObjectTriplet(o, deps));
				}
			}
		}
		return pairs;
	}
	
	/**
	 * Gets an instance of object triplet for an object and its list of
	 * experiment dependencies.
	 * @param o The object
	 * @param dependencies The list of that objet's dependencies
	 * @return An object triplet
	 */
	protected abstract ObjectTriplet getObjectTriplet(Object o, List<Experiment> dependencies);
	
	/**
	 * Orders a list of experiments.
	 * @param experiments The original list of experiments
	 * @param objects A list of collections of dependent objects
	 * @return The reordered list
	 */
	@SuppressWarnings("unchecked")
	protected abstract List<Experiment> scheduleFromList(List<Experiment> experiments, Collection<? extends Dependent<?>> ... objects);
	
	/**
	 * Association between an object, a list of experiments and an integer.
	 */
	protected static class ObjectTriplet
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
		 * Creates a new object triplet.
		 * @param o The object in the object triplet
		 * @param exps The experiments this object depends on
		 */
		public ObjectTriplet(/*@ non_null @*/ Object o, /*@ non_null @*/ List<Experiment> exps)
		{
			super();
			m_object = o;
			m_experiments = exps;
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
	
	@Override
	public void restart()
	{
		// Nothing to do
	}
}
