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
 * An experiment factory producing experiments that are all instances of the
 * same class.
 * @author Sylvain Hallé
 * @since 3.0
 */
public class SingleClassExperimentFactory<E extends Experiment> extends ExperimentFactory<E>
{
	/**
	 * The class of the experiments to be instantiated.
	 */
	protected Class<E> m_class;

	/**
	 * The experiment constructor taking a point as its argument.
	 */
	protected Constructor<E> m_pointConstructor;

	/**
	 * The experiment constructor taking a point as its argument.
	 */
	protected Constructor<E> m_emptyConstructor;
	
	public SingleClassExperimentFactory(Laboratory lab, Class<E> clazz)
	{
		super(lab);
		m_class = clazz;
		try 
		{
			m_pointConstructor = clazz.getConstructor(Point.class);
		} 
		catch (NoSuchMethodException | SecurityException e) 
		{
			// Do nothing
			m_pointConstructor = null;
		}
		try 
		{
			m_emptyConstructor = clazz.getConstructor(Void.class);
		} 
		catch (NoSuchMethodException | SecurityException e) 
		{
			// Do nothing
			m_emptyConstructor = null;
		}

	}

	@Override
	protected Constructor<E> getPointConstructor(Point p)
	{
		return m_pointConstructor;
	}

	@Override
	protected Constructor<E> getEmptyConstructor(Point p)
	{
		return m_emptyConstructor;
	}

	@Override
	protected Class<E> getClass(Point p)
	{
		return m_class;
	}
}
