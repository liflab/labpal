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

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.provenance.ExperimentValue;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.mtnp.table.PrimitiveValue;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TableEntry;
import ca.uqac.lif.mtnp.table.TempTable;

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
	 * Creates an empty table with a given list of column names
	 * @param dimensions The column names
	 */
	public ExperimentTable(String ... dimensions)
	{
		super();
		m_experiments = new ArrayList<Experiment>();
		m_dimensions = dimensions;
	}	

	/**
	 * Adds a new experiment to the table
	 * @param e The experiment to read from
	 * @return This table
	 */
	public ExperimentTable add(Experiment e)
	{
		m_experiments.add(e);
		return this;
	}

	@Override
	public TempTable getDataTable(boolean temporary)
	{
		return getDataTable(temporary, m_dimensions);
	}

	@Override
	protected TempTable getDataTable(boolean temporary, String ... ordering)
	{
		TempTable mt;
		if (temporary)
		{
			mt = new TempTable(getId(), ordering);
			mt.setId(getId());
		}
		else
		{
			mt = new TempTable(getId(), ordering);
		}
		int row_nb = 0;
		for (Experiment e : m_experiments)
		{
			List<TableEntry> entries = getEntries(false, e, row_nb, ordering);
			mt.addAll(entries);
			row_nb += entries.size();
		}
		return mt;
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
	 * @param temporary Set to <tt>true</tt> if the table is a temporary
	 * table, i.e. a table that is not associated with the lab
	 * @param e The experiment
	 * @param row_start Unused; will likely be deprecated in a future
	 * version
	 * @param dimensions The columns to consider when expanding
	 * @return A list of table entries corresponding to the data
	 *   points in the experiment
	 */
	public List<TableEntry> getEntries(boolean temporary, Experiment e, int row_start, String ... dimensions)
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
					te.put(col_name, jsonToPrimitive(elem), new ExperimentValue(e, col_name));
				}
				else
				{
					//Don't write anything if the parameter is not there
					//te.put(col_name, PrimitiveValue.getInstance(null));
				}
			}
			// ...and the i-th value of each list column
			for (Map.Entry<String,JsonList> map_entry : list_columns.entrySet())
			{
				String key = map_entry.getKey();
				JsonList list = map_entry.getValue();
				if (i < list.size())
				{
					JsonElement elem = list.get(i);
					if (elem != null)
					{
						te.put(key, jsonToPrimitive(elem), new ExperimentValue(e, key, i));
					}
					else
					{
						//te.put(key, JsonNull.instance);
					}
				}
				else
				{
					// Substitute with null if one of the lists is shorter
					//te.put(key, JsonNull.instance);
				}
			}
			entries.add(te);
		}
		return entries;
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

	@Override
	public NodeFunction getDependency(int row, int col)
	{
		TempTable dt = getDataTable(false);
		return dt.getDependency(row, col);
	}
	
	public static PrimitiveValue jsonToPrimitive(JsonElement e)
	{
		if (e instanceof JsonNumber)
		{
			return PrimitiveValue.getInstance(((JsonNumber) e).numberValue());
		}
		if (e instanceof JsonString)
		{
			return PrimitiveValue.getInstance(((JsonString) e).stringValue());
		}
		return null;
		
	}

}
