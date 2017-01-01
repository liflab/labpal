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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.labpal.Experiment;

/**
 * Table whose rows and columns are populated from the parameters of a set
 * of experiments
 * @author Sylvain Hallé
 */
public class ExperimentTable extends Table
{
	/**
	 * The list of experiments in this table. Note that we use a list,
	 * and not a set, as we need the experiments to be enumerated in the
	 * same order every time. Otherwise, the <i>n</i>-th "row" of the
	 * table would not always refer to the same data point.
	 */
	public List<Experiment> m_experiments;

	/**
	 * The dimensions of this table
	 */
	public String[] m_dimensions;

	/**
	 * The type of each column in the table
	 */
	//public Class<? extends Comparable<?>>[] m_columnTypes;

	public ExperimentTable(String ... dimensions)
	{
		super();
		m_experiments = new ArrayList<Experiment>();
		m_dimensions = dimensions;
	}	

	/**
	 * Adds a new experiment to the table
	 * @param e The experiment to read from
	 */
	public void add(Experiment e)
	{
		m_experiments.add(e);
	}

	@Override
	public DataTable getConcreteTable()
	{
		return getConcreteTable(m_dimensions);
	}

	@Override
	public DataTable getConcreteTable(String ... ordering)
	{
		DataTable mt = new DataTable(ordering);
		for (Experiment e : m_experiments)
		{
			List<TableEntry> entries = getEntries(e, ordering);
			mt.addAll(entries);
		}
		return mt;
	}

	@Override
	public Comparable<?> get(int col, int row)
	{
		int exp_count = 0;
		for (Experiment e : m_experiments)
		{
			int num_entries = getEntryCount(e, m_dimensions);
			if (row > exp_count + num_entries)
			{
				// We must look further; skip this experiment
				exp_count += num_entries;
				continue;
			}
			// The entry we want is contained in this experiment
			List<TableEntry> entries = getEntries(e, m_dimensions);
			int index = row - exp_count;
			TableEntry te = entries.get(index);
			String key = m_dimensions[col];
			Object o = te.get(key);
			return (Comparable<?>) o;
		}
		return null;
	}

	/**
	 * Counts the distinct table entries contained in this experiment.
	 * @param e The experiment
	 * @param dimensions The columns to consider when expanding
	 * @return The number of table entries
	 */
	public int getEntryCount(Experiment e, String ... dimensions)
	{
		// We start at 1, since an experiment with no lists still counts for
		// one data point
		int max_len = 1;
		for (String col_name : dimensions)
		{
			Object o = readExperiment(e, col_name);
			if (o instanceof JsonList)
			{
				max_len = Math.max(max_len, ((JsonList) o).size());

			}
		}
		return max_len;
	}

	/**
	 * Expands an experiment into multiple table entries, if the
	 * experiment has parameters whose value is a list instead of a
	 * scalar value. This allows a single experiment to define multiple
	 * data points.
	 * @param e The experiment
	 * @param dimensions The columns to consider when expanding
	 * @return A list of table entries corresponding to the data
	 *   points in the experiment
	 */
	public List<TableEntry> getEntries(Experiment e, String ... dimensions)
	{
		List<TableEntry> entries = new ArrayList<TableEntry>();
		List<String> scalar_columns = new ArrayList<String>();
		Map<String,JsonList> list_columns = new HashMap<String,JsonList>();
		int max_len = 1;
		// First, go through all columns and look for those
		// that contain lists vs. scalar values
		for (String col_name : dimensions)
		{
			Object o = readExperiment(e, col_name);
			if (o instanceof JsonList)
			{
				list_columns.put(col_name, (JsonList) o);
				max_len = Math.max(max_len, ((JsonList) o).size());
			}
			else
			{
				scalar_columns.add(col_name);
			}
		}
		// Now create as many entries as max_len
		for (int i = 0; i < max_len; i++)
		{
			TableEntry te = new TableEntry();
			// Fill each with values of the scalar columns...
			for (String col_name : scalar_columns)
			{
				JsonElement elem = readExperiment(e, col_name);
				if (elem != null)
				{
					te.put(col_name, elem);
				}
				else
				{
					te.put(col_name, JsonNull.instance);
				}
			}
			// ...and the i-th value of each list column
			for (Map.Entry<String,JsonList> map_entry : list_columns.entrySet())
			{
				JsonList list = map_entry.getValue();
				if (i < list.size())
				{
					JsonElement elem = list.get(i);
					if (elem != null)
					{
						te.put(map_entry.getKey(), elem);
					}
					else
					{
						te.put(map_entry.getKey(), JsonNull.instance);
					}
					te.put(map_entry.getKey(), elem);
				}
				else
				{
					// Substitute with null if one of the lists is shorter
					te.put(map_entry.getKey(), JsonNull.instance);
				}
			}
			entries.add(te);
		}
		return entries;
	}

	@Override
	public int getColumnCount()
	{
		return m_dimensions.length;
	}

	@Override
	public Class<? extends Comparable<?>>[] getColumnTypes()
			{
		@SuppressWarnings("unchecked")
		Class<? extends Comparable<?>> types[] = new Class[getColumnCount()];
		for (int i = 0; i < m_dimensions.length; i++)
		{
			types[i] = getColumnTypeFor(m_dimensions[i]);
		}
		return types;
			}

	@Override
	public int getRowCount()
	{
		int size = 0;
		for (Experiment e : m_experiments)
		{
			size += getEntryCount(e);
		}
		return size;
	}

	@Override
	public Class<? extends Comparable<?>> getColumnTypeFor(String col_name)
	{
		// Guess column type
		for (Experiment e : m_experiments)
		{
			Object o = readExperiment(e, col_name);
			Class<? extends Comparable<?>> clazz = getTypeOf(o);
			if (clazz == null || !clazz.equals(JsonList.class))
			{
				return clazz;
			}
			// This parameter is a list: read the list to guess its type
			for (JsonElement elem : (JsonList) o)
			{
				clazz = getTypeOf(elem);
				if (clazz != null)
				{
					return clazz;
				}
			}
		}
		return null;
	}


	@Override
	public String getColumnName(int col)
	{
		if (col < 0 || col >= m_dimensions.length)
		{
			return null;
		}
		return m_dimensions[col];
	}

	@Override
	public int getColumnPosition(String name)
	{
		if (name == null)
		{
			return -1;
		}
		for (int i = 0; i < m_dimensions.length; i++)
		{
			if (m_dimensions[i].compareTo(name) == 0)
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public String[] getColumnNames()
	{
		return m_dimensions;
	}

	@Override
	public TableEntry findEntry(TableEntry e)
	{
		for (Experiment exp : m_experiments)
		{
			boolean found = true;
			for (Entry<String,Object> entry : e.entrySet())
			{
				Object o = readExperiment(exp, entry.getKey());
				if ((o == null && entry.getValue() == null) ||
						o != null && o.equals(entry.getValue()))
				{
					// OK
				}
				else
				{
					found = false;
					break;
				}
			}
			if (found)
			{
				// This experiment has the same key-value pairs as the
				// entry passed as an argument: create an entry from it
				TableEntry te = new TableEntry();
				for (String key : m_dimensions)
				{
					te.put(key, readExperiment(exp, key));
				}
				return te;
			}
		}
		return null;
	}

	/**
	 * Reads data from an experiment. Override this method to transform the
	 * data from an experiment before putting it in the table.
	 * @param e The experiment
	 * @param key The key to read from the experiment
	 * @return The value
	 */
	public JsonElement readExperiment(Experiment e, String key)
	{
		return e.read(key);
	}
}
