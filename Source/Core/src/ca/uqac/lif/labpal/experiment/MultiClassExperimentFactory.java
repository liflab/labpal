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

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.region.Point;

/**
 * Experiment factory producing experiments that are instances of various
 * classes. The instances must have a common {@link Experiment} ancestor
 * class, which is the type declared as the output type of the factory.
 * 
 * @author Sylvain Hallé
 *
 * @param <E> The class of the experiments that are to be created
 */
public class MultiClassExperimentFactory<E extends Experiment> extends ExperimentFactory<E>
{
	/**
	 * The dimension of a {@link Point} to look for in order to determine what
	 * experiment class should be used.
	 */
	/*@ non_null @*/ protected final String m_dimension;
	
	public MultiClassExperimentFactory(/*@ non_null @*/ Laboratory lab, /*@ non_null @*/ String dimension)
	{
		super(lab);
		m_dimension = dimension;
	}

	@Override
	protected Constructor<? extends E> getPointConstructor(Point p)
	{
		Class<? extends E> clazz = getClass(p);
		if (clazz == null)
		{
			return null;
		}
		try 
		{
			return clazz.getConstructor(Point.class);
		} 
		catch (NoSuchMethodException | SecurityException e) 
		{
			return null;
		}
	}

	@Override
	protected final Constructor<? extends E> getEmptyConstructor(Point p)
	{
		Class<? extends E> clazz = getClass(p);
		if (clazz == null)
		{
			return null;
		}
		try 
		{
			return clazz.getConstructor(Void.class);
		} 
		catch (NoSuchMethodException | SecurityException e) 
		{
			return null;
		}
	}

	@Override
	protected final Class<? extends E> getClass(Point p)
	{
		return getClassFor(p.getString(m_dimension));
	}
	
	protected Class<? extends E> getClassFor(String dimension)
	{
		return null;
	}
}
