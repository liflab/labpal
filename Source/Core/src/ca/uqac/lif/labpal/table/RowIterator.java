package ca.uqac.lif.labpal.table;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RowIterator implements Iterator<Comparable<?>>
{
	protected Table m_table;

	/**
	 * Index of current column
	 */
	protected int col = 0;

	/**
	 * Index of current row
	 */
	protected int row = 0;

	RowIterator(Table table)
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
