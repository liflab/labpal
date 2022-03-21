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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.labpal.region.Region;

/**
 * Factory object that creates instances of experiments based on regions,
 * and adds them to an existing lab. A factory is used through its method
 * {@link #get(Region)}, which fetches an existing instance of an experiment
 * matching a given region, or creates a new one if such experiment does
 * not exist.
 * <p>
 * The factory uses reflection on the provided experiment class to create
 * new instances by calling a constructor taking a {@link Point} as its
 * only argument. This behavior can be modified by overriding method
 * {@link #createExperiment(Point)} with whatever logic a user needs.
 * 
 * @author Sylvain Hallé
 *
 * @param <E> The class of the experiments that are to be created
 * 
 * @since 2.10
 */
public abstract class ExperimentFactory<E extends Experiment>
{
	/**
	 * The lab to which experiments are added when the factory is called.
	 */
	/*@ non_null @*/ protected final Laboratory m_lab;

	/**
	 * A map between points and experiment instances created by this factory.
	 * This map is used as a cache, to avoid creating two instances of the
	 * same experiment. 
	 */
	/*@ non_null @*/ protected final Map<Point,E> m_added;

	public ExperimentFactory(/*@ non_null @*/ Laboratory lab)
	{
		super();
		m_lab = lab;
		m_added = new HashMap<Point,E>();
	}

	/**
	 * Gets a set of experiments corresponding to all points in a given region.
	 * @param r The region
	 * @return The set of experiments
	 */
	/*@ non_null @*/ public Set<E> get(/*@ non_null @*/ Region r)
	{
		Set<E> experiments = new HashSet<E>();
		for (Point p : r.allPoints())
		{
			E e = get(p);
			if (e != null)
			{
				experiments.add(e);
			}
		}
		return experiments;
	}

	/**
	 * Gets an experiment corresponding to a point. If the experiment has not
	 * yet been added to the lab, adds the experiment to it.
	 * @param p The point
	 * @return The experiment
	 */
	/*@ null @*/ public final E get(/*@ non_null @*/ Point p)
	{
		Point pp = project(p);
		if (pp == null)
		{
			return null;
		}
		if (m_added.containsKey(pp))
		{
			return m_added.get(pp);
		}
		E e = createExperiment(pp);
		m_added.put(pp, e);
		if (e != null)
		{
			m_lab.add(e);
		}
		return e;
	}

	/**
	 * Performs an optional projection on a point before passing it to the
	 * experiment's constructor. By default, this method simply return the
	 * input point without modification. It can be overridden in the case where
	 * an experiment does not require all the dimensions of a point in order
	 * to be instantiate.
	 * @param p The point
	 * @return The projected point
	 */
	/*@ pure null @*/ protected Point project(Point p)
	{
		return p;
	}

	/**
	 * Creates a new experiment instance based on the values contained within a
	 * point. This method does so by looking for a constructor in the
	 * experiment class that takes a point as an argument, and calls this
	 * constructor with the point.
	 * @param p The point
	 * @return The experiment instance
	 */
	/*@ pure null @*/ protected E createExperiment(Point p)
	{
		Constructor<? extends E> point_constructor = getPointConstructor(p);
		
		if (point_constructor != null)
		{
			try
			{
				return point_constructor.newInstance(p);
			}
			catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
			{
				return null;
			}
		}
		Constructor<? extends E> empty_constructor = getEmptyConstructor(p);
		if (empty_constructor != null)
		{
			try
			{
				E e = empty_constructor.newInstance();
				for (String d : p.getDimensions())
				{
					e.writeInput(d, p.get(d));
				}
				return e;
			}
			catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
			{
				return null;
			}
		}
		try
		{
			Class<? extends E> clazz = getClass(p);
			if (clazz == null)
			{
				return null;
			}
			@SuppressWarnings("deprecation")
			E e = clazz.newInstance();
			for (String d : p.getDimensions())
			{
				e.writeInput(d, p.get(d));
			}
			return e;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets the constructor taking a {@link Point} as argument for the experiment
	 * corresponding to a given point.
	 * @param p The point
	 * @return The constructor, or <tt>null</tt> if no such constructor exists
	 */
	/*@ null @*/ protected abstract Constructor<? extends E> getPointConstructor(Point p);
	
	/**
	 * Gets the no-args constructor for the experiment corresponding to a given
	 * point.
	 * @param p The point
	 * @return The constructor, or <tt>null</tt> if no such constructor exists
	 */
	/*@ null @*/ protected abstract Constructor<? extends E> getEmptyConstructor(Point p);
	
	/**
	 * Gets the class for the experiment corresponding to a given point.
	 * @param p The point
	 * @return The class, or <tt>null</tt> if no class corresponds to this point
	 */
	/*@ null @*/ protected abstract Class<? extends E> getClass(Point p);
}
