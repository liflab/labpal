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
package ca.uqac.lif.labpal.util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Enumerates the permutations of an array of objects. The iterator follows
 * a non-recursive implementation of
 * <a href="https://en.wikipedia.org/wiki/Heap's_algorithm">Heap's
 * algorithm</a>.
 * @author Sylvain Hallé
 *
 * @param <T> The type of the objects to enumerate
 */
public class PermutationIterator<T> implements Iterable<T[]>, Iterator<T[]>
{
	/**
	 * Creates a permutation iterator for string objects.
	 * @param objects The strings to permute
	 * @return The new permutation iterator instance
	 */
	/*@ non_null @*/ public static PermutationIterator<String> permute(String ... objects)
	{
		return new PermutationIterator<String>(objects);
	}
	
	/**
	 * Creates a permutation iterator for numbers.
	 * @param objects The numbers to permute
	 * @return The new permutation iterator instance
	 */
	/*@ non_null @*/ public static PermutationIterator<Number> permute(Number ... objects)
	{
		return new PermutationIterator<Number>(objects);
	}
	
	protected final T[] m_objects;
	
	protected final int[] m_counter;
	
	protected int m_i;
	
	protected boolean m_done;
	
	@SuppressWarnings("unchecked")
	public PermutationIterator(T ... objects)
	{
		super();
		m_objects = objects;
		m_counter = new int[m_objects.length];
		for (int i = 0; i < m_counter.length; i++)
		{
			m_counter[i] = 0;
		}
		m_i = 0;
		m_done = false;
	}
	
	@Override
	public boolean hasNext()
	{
		return !m_done;
	}

	@Override
	public T[] next()
	{
		T[] to_output = Arrays.copyOf(m_objects, m_objects.length);
		while (m_i < m_objects.length)
		{
			if (m_counter[m_i] < m_i)
			{
				if (m_i % 2 == 0)
				{
					swap(m_objects, 0, m_i);
				}
				else
				{
					swap(m_objects, m_counter[m_i], m_i);
				}
				m_counter[m_i]++;
				m_i = 0;
				return to_output;
			}
			else
			{
				m_counter[m_i] = 0;
				m_i++;
			}
		}
		m_done = true;
		return to_output;
	}

	@Override
	public Iterator<T[]> iterator()
	{
		return this;
	}
	
	/**
	 * Swaps two elements of an array.
	 * @param a The array
	 * @param x The index of the first element to swap
	 * @param y The index of the second element to swap
	 */
	protected void swap(T[] a, int x, int y)
	{
		T o = a[x];
		a[x] = a[y];
		a[y] = o;
	}

}
