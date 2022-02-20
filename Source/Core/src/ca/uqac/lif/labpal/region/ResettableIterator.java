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
package ca.uqac.lif.labpal.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An iterator over a collection of elements with the additional property
 * that the iteration can be restarted from the beginning at any point. To
 * this end, the class provides an extra method called {@link #reset()}.
 * 
 * @since 3.0
 *
 * @param <T> The type of the objects to iterate over
 */
class ResettableIterator<T> implements Iterable<T>, Iterator<T>
{
	/**
	 * The list of elements to iterate over. The elements are stored in a
	 * list to guarantee the same order of iteration every time.
	 */
	/*@ non_null @*/ private final List<T> m_elements;

	/**
	 * The index of the element in the list that has been returned by the
	 * last call to {@link #next()}. Resetting this index to -1 has the
	 * effect of restarting the iteration from the beginning.
	 */
	private int m_index;

	/**
	 * Creates a new instance of the iterator.
	 * @param elements The list of objects to iterate over
	 */
	public ResettableIterator(Collection<T> elements)
	{
		super();
		m_elements = new ArrayList<T>(elements.size());
		m_elements.addAll(elements);
		m_index = -1;
	}
	
	/**
	 * Resets the iteration from the first element.
	 */
	public void reset()
	{
		m_index = -1;
	}

	@Override
	public boolean hasNext() 
	{
		return m_index < m_elements.size() - 1;
	}

	@Override
	public T next()
	{
		m_index++;
		return m_elements.get(m_index);
	}

	@Override
	public Iterator<T> iterator()
	{
		return new ResettableIterator<T>(m_elements);
	}
}