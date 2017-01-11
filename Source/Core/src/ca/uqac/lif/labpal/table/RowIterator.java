/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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
package ca.uqac.lif.labpal.table;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RowIterator implements Iterator<Comparable<?>>
{
	/**
	 * The table on which to iterate
	 */
	protected DataTable m_table;

	/**
	 * Index of current column
	 */
	protected int col = 0;

	/**
	 * Index of current row
	 */
	protected int row = 0;

	RowIterator(DataTable table)
	{
		super();
		m_table = table;
		col = 0;
		row = 0;
	}

	/**
	 * Returns {@code true} if the iteration has more elements.
	 * (In other words, returns {@code true} if {@code next}
	 * would return an element rather than throwing an exception.)
	 * @return {@code true} if the iterator has more elements.
	 */
	@Override
	public boolean hasNext()
	{
		return (col < m_table.getColumnCount()) && (row < m_table.getRowCount());
	}

	/**
	 * Returns the next element in the iteration.
	 * @return the next element in the iteration.
	 * @exception NoSuchElementException iteration has no more elements.
	 */
	@Override
	public Comparable<?> next()
	{
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		Comparable<?> value = m_table.get(col, row);
		if (++col >= m_table.getColumnCount()) {
			col = 0;
			++row;
		}
		return value;
	}

	/**
	 * Method that theoretically removes a cell from a data source.
	 * However, this is not supported.
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
