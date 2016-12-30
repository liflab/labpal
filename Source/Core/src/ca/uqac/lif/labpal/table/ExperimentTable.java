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
import java.util.List;
import java.util.Map.Entry;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.labpal.Experiment;

/**
 * Table whose rows and columns are made from the parameters of a set
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
	
	/**
	 * Gets a concrete multidimensional table from the experiments'
	 * data
	 * @return The table
	 */
	public DataTable getConcreteTable()
	{
		return getConcreteTable(m_dimensions);
	}
	
	/**
	 * Gets a concrete multidimensional table from the experiments'
	 * data
	 * @param ordering The ordering of the dimensions
	 * @return The table
	 */
	@SuppressWarnings("unchecked")
	public DataTable getConcreteTable(String[] ordering)
	{
		Class<? extends Comparable<?>> new_types[] = new Class[m_dimensions.length];
		for (int i = 0; i < ordering.length; i++)
		{
			int pos = getColumnPosition(ordering[i]);
			if (pos < 0)
			{
				new_types[i] = null;
			}
			else
			{
				new_types[i] = getColumnTypeFor(pos);
			}
		}
		DataTable mt = new DataTable(ordering, new_types);
		for (Experiment e : m_experiments)
		{
			TableEntry entry = new TableEntry();
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
	public Comparable<?> get(int col, int row)
	{
		int exp_count = 0;
		for (Experiment e : m_experiments)
		{
			if (exp_count == row)
			{
				String key = m_dimensions[col];
				Object o = e.read(key);
				if (o == null)
				{
					return null;
				}
				if (o instanceof JsonNumber)
				{
					// Cast JsonNumbers as numbers
					return ((JsonNumber) o).numberValue().floatValue();
				}
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
		return m_experiments.size();
	}

	@Override
	public Class<? extends Comparable<?>> getColumnTypeFor(String col_name)
	{
		// Guess column type
		for (Experiment e : m_experiments)
		{
			Object o = e.read(col_name);
			if (o != null)
			{
					if (o instanceof JsonNumber || o instanceof Number)
					{
						return Float.class;
					}
					else
					{
						return String.class;
					}
			}
		}
		/*
		for (int i = 0; i < m_dimensions.length; i++)
		{
			if (m_dimensions[i].compareTo(col_name) == 0)
			{
				return m_columnTypes[i];
			}
		}
		*/
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
				Object o = exp.read(entry.getKey());
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
					te.put(key, exp.read(key));
				}
				return te;
			}
		}
		return null;
	}
}
