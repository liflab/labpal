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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.labpal.Laboratory;
import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.DataListener;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.Row;
import de.erichseifert.gral.data.statistics.Statistics;

/**
 * A multi-dimensional array of values. Tables can be passed to
 * {@link Plot} objects to generate graphics.
 */
public abstract class Table implements DataSource
{
	/**
	 * The table's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing table IDs
	 */
	private static int s_idCounter = 1;
	
	/**
	 * A lock for accessing the counter
	 */
	private static Lock s_counterLock = new ReentrantLock();
	
	/**
	 * The data listeners associated to this table
	 */
	protected Set<DataListener> m_dataListeners;
	
	/**
	 * The table's title
	 */
	protected String m_title;
	
	/**
	 * The types of values that a data cell can have
	 */
	public static enum Type {TEXT, NUMERIC};
	
	public Table()
	{
		super();
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
		m_title = "Untitled";
		m_dataListeners = new HashSet<DataListener>();
	}
	
	@Override
	public void removeDataListener(DataListener dataListener)
	{
		m_dataListeners.remove(dataListener);
	}
	
	/**
	 * Assigns this table to a laboratory
	 * @param a The lab
	 * @return This table
	 */
	public Table assignTo(Laboratory a)
	{
		return this;
	}
	
	@Override
	public String getName()
	{
		return m_title;
	}
	
	@Override
	public Row getRow(int row)
	{
		return new Row(this, row);
	}
	
	@Override
	public final Iterator<Comparable<?>> iterator()
	{
		return new RowIterator(this);
	}

	@Override
	public final void addDataListener(DataListener dataListener)
	{
		m_dataListeners.add(dataListener);
	}

	@Override
	public Statistics getStatistics()
	{
		return new Statistics(this);
	}
	
	public final String getDescription()
	{
		return "";
	}
	
	public final void setTitle(String title)
	{
		m_title = title;
	}
	
	public final String getTitle()
	{
		return m_title;
	}
	
	/**
	 * Gets the table's unique ID
	 * @return The ID
	 */
	public final int getId()
	{
		return m_id;
	}
	
	@Override
	public final boolean isColumnNumeric(int columnIndex)
	{
		Class<?> c = getColumnTypeFor(columnIndex);
		return c.isAssignableFrom(Float.class);
	}
	
	/**
	 * Gets the type of the column of given name
	 * @param col_name The name of the column
	 * @return The type, or {@code null} if the column does not exist
	 */
	public final Class<? extends Comparable<?>> getColumnTypeFor(int position)
	{
		String name = getColumnName(position);
		return getColumnTypeFor(name);
	}
	
	@Override
	public final Column getColumn(int col)
	{
		return new Column(this, col);
	}
	
	/**
	 * Gets the type of the column of given name
	 * @param col_name The name of the column
	 * @return The type, or {@code null} if the column does not exist
	 */
	public abstract Class<? extends Comparable<?>> getColumnTypeFor(String col_name);
	
	/**
	 * Gets an instance of {@link DataTable} from the table's
	 * data, using the columns specified in the argument
	 * @param ordering The columns to use
	 * @return The table
	 */
	public abstract DataTable getConcreteTable(String ... ordering);

	/**
	 * Gets an instance of {@link DataTable} from the table's
	 * data, using the default column ordering
	 * @return The table
	 */
	public abstract DataTable getConcreteTable();
	
	/**
	 * Gets the name of the column at a given position in the table
	 * @param col The position
	 * @return The column's name, or null if the index is out of bounds
	 */
	public abstract String getColumnName(int col);
	
	/**
	 * Gets the position of the column of a given name in the table
	 * @param col The name
	 * @return The column's position, or -1 if the name was not found
	 */
	public abstract int getColumnPosition(String name);
	
	/**
	 * Gets the names of all the columns in the table
	 * @return An array of names
	 */
	public abstract String[] getColumnNames();
	
	/**
	 * Finds an entry with the same key-value pairs as the entry given
	 * as an argument
	 * @param e The entry
	 * @return The entry found, or {@code null} if none found
	 */
	public abstract TableEntry findEntry(TableEntry e);
	
	/**
	 * Casts a value as a number or an instance of {@code Comparable}
	 * @param o The value
	 * @return The cast value
	 */
	public static final Comparable<?> castValue(Object o)
	{
		if (o == null)
		{
			return null;
		}
		if (o instanceof JsonNumber)
		{
			return ((JsonNumber) o).numberValue().floatValue();
		}
		return (Comparable<?>) o;
	}
	
	@Override
	public String toString()
	{
		return getConcreteTable().toString();
	}

	/**
	 * Guess the type of a column in a table, by looking at the type of one
	 * of its values. The type is defined as follows:
	 * <ul>
	 * <li>Any JsonNumber and any Number will return {@code Float}</li>
	 * <li>A JsonList will return its own type</li>
	 * <li>A null object returns null</li>
	 * <li>Anything else returns {@code String}</li>
	 * </ul>
	 * @param o The value to look at
	 * @return The type
	 */
	public static Class<? extends Comparable<?>> getTypeOf(Object o)
	{
		if (o == null)
		{
			return null;
		}
		if (o instanceof JsonNumber || o instanceof Number)
		{
			return Float.class;
		}
		else if (o instanceof JsonList)
		{
			return JsonList.class;
		}
		return String.class;
	}

}
