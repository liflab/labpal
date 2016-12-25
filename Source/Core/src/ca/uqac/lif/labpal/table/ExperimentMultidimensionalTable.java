/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.table.MultidimensionalTable.Entry;
import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.DataListener;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.Row;
import de.erichseifert.gral.data.statistics.Statistics;

/**
 * A table whose data is fetched from the results of a group of experiments
 * @author Sylvain Hallé
 */
public class ExperimentMultidimensionalTable implements DataSource
{
	/**
	 * The table's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing table IDs
	 */
	protected static int s_idCounter = 1;
	
	/**
	 * A lock for accessing the counter
	 */
	protected static Lock s_counterLock = new ReentrantLock();
	
	/**
	 * The data listeners associated to this table
	 */
	protected Set<DataListener> m_dataListeners;
	
	/**
	 * The table's title
	 */
	protected String m_title;
	
	/**
	 * The dimensions of this table
	 */
	public String[] m_dimensions;
	
	/**
	 * The types of values that a data cell can have
	 */
	public static enum ColumnType {TEXT, NUMERIC};
	
	/**
	 * The type of each column in the table
	 */
	public Class<? extends Comparable<?>>[] m_columnTypes;
	
	/**
	 * The list of experiments in this table. Note that we use a list,
	 * and not a set, as we need the experiments to be enumerated in the
	 * same order every time. Otherwise, the <i>n</i>-th "row" of the
	 * table would not always refer to the same data point.
	 */
	public List<Experiment> m_experiments;
	
	@SuppressWarnings("unchecked")
	public ExperimentMultidimensionalTable(String[] dimensions, ColumnType[] types)
	{
		super();
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
		m_experiments = new ArrayList<Experiment>();
		m_dimensions = dimensions;
		m_title = "Untitled";
		m_dataListeners = new HashSet<DataListener>();
		m_columnTypes = new Class[dimensions.length];
		for (int i = 0; i < dimensions.length; i++)
		{
			if (types[i] == ColumnType.NUMERIC)
			{
				m_columnTypes[i] = Float.class;
			}
			else
			{
				m_columnTypes[i] = String.class;
			}
		}
	}
	
	public String getDescription()
	{
		return "";
	}
	
	public void setTitle(String title)
	{
		m_title = title;
	}
	
	public String getTitle()
	{
		return m_title;
	}
	
	public String[] getDimensions()
	{
		return m_dimensions;
	}
	
	/**
	 * Adds a new experiment to the table
	 * @param e The experiment to read from
	 */
	public void add(Experiment e)
	{
		m_experiments.add(e);
	}
	
	/**
	 * Gets the table's unique ID
	 * @return The ID
	 */
	public int getId()
	{
		return m_id;
	}
	
	/**
	 * Gets a concrete multidimensional table from the experiments'
	 * data
	 * @return The table
	 */
	public MultidimensionalTable getTable()
	{
		return getTable(m_dimensions);
	}
	
	/**
	 * Gets a concrete multidimensional table from the experiments'
	 * data
	 * @param ordering The ordering of the dimensions
	 * @return The table
	 */
	public MultidimensionalTable getTable(String[] ordering)
	{
		MultidimensionalTable mt = new MultidimensionalTable(ordering);
		for (Experiment e : m_experiments)
		{
			Entry entry = new Entry();
			for (String key : m_dimensions)
			{
				JsonElement elem = e.read(key);
				if (elem != null)
				{
					entry.put(key, elem);
				}
				else
				{
					entry.put(key, JsonNull.instance);
				}
			}
			mt.add(entry);
		}
		return mt;
	}

	@Override
	public Iterator<Comparable<?>> iterator()
	{
		return new RowIterator(this);
	}

	@Override
	public void addDataListener(DataListener dataListener)
	{
		m_dataListeners.add(dataListener);
	}

	@Override
	public Comparable<?> get(int col, int row)
	{
		int exp_count = 0;
		for (Experiment e : m_experiments)
		{
			if (exp_count == row)
			{
				String key = m_dimensions[col];
				Object o = e.read(key);
				return (Comparable<?>) o;
			}
			else
			{
				exp_count++;
			}
		}
		return null;
	}

	@Override
	public Column getColumn(int col)
	{
		return new Column(this, col);
	}

	@Override
	public int getColumnCount()
	{
		return m_dimensions.length;
	}

	@Override
	public Class<? extends Comparable<?>>[] getColumnTypes()
	{
		return m_columnTypes;
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
	public int getRowCount()
	{
		return m_experiments.size();
	}

	@Override
	public Statistics getStatistics()
	{
		return new Statistics(this);
	}

	@Override
	public boolean isColumnNumeric(int columnIndex)
	{
		if (columnIndex < 0 || columnIndex >= m_columnTypes.length)
		{
			return false;
		}
		return m_columnTypes[columnIndex].isAssignableFrom(Float.class);
	}

	@Override
	public void removeDataListener(DataListener dataListener)
	{
		m_dataListeners.remove(dataListener);
	}	
}
