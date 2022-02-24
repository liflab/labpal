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
package ca.uqac.lif.labpal.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentValue;
import ca.uqac.lif.labpal.provenance.TrackedValue;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * A table that produces a spreadsheet by extracting parameter values out of
 * a list of experiments.
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
	protected List<Experiment> m_experiments;

	/**
	 * The dimensions of this table
	 */
	protected String[] m_dimensions;

	/**
	 * A list associating each row of the last computed spreadsheet to the
	 * value and lineage information of each cell.
	 */
	protected List<TableEntry> m_lastEntries;
	
	/**
	 * Creates an empty table with a given list of column names
	 * @param id The unique ID given to this table
	 * @param dimensions The column names
	 */
	public ExperimentTable(String ... dimensions)
	{
		super();
		m_experiments = new ArrayList<Experiment>();
		m_dimensions = dimensions;
		m_lastEntries = null;
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
	protected Spreadsheet calculateSpreadsheet()
	{
		int row_nb = 0;
		m_lastEntries = new ArrayList<TableEntry>();
		for (Experiment e : m_experiments)
		{
			List<TableEntry> entries = getEntries(false, e, m_dimensions);
			m_lastEntries.addAll(entries);
			row_nb += entries.size();
		}
		Spreadsheet out = new Spreadsheet(m_dimensions.length, row_nb + 1);
		for (int col = 0; col < m_dimensions.length; col++)
		{
			out.set(col, 0, m_dimensions[col]);
		}
		for (int row = 0; row < row_nb; row++)
		{
			TableEntry te = m_lastEntries.get(row);
			for (int col = 0; col < m_dimensions.length; col++)
			{
				if (!te.containsKey(m_dimensions[col]))
				{
					out.set(col, row + 1, null);
				}
				else
				{
					out.set(col, row + 1, te.get(m_dimensions[col]).getValue());
				}
			}
		}
		return out;
	}

	/**
	 * Expands an experiment into multiple table entries, if the
	 * experiment has parameters whose value is a list instead of a
	 * scalar value. This allows a single experiment to define multiple
	 * data points.
	 * @param temporary Set to <tt>true</tt> if the table is a temporary
	 * table, i.e. a table that is not associated with the lab
	 * @param e The experiment
	 * @param dimensions The columns to consider when expanding
	 * @return A list of table entries corresponding to the data
	 *   points in the experiment
	 */
	protected List<TableEntry> getEntries(boolean temporary, Experiment e, String ... dimensions)
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
				Object elem = readExperiment(e, col_name);
				if (elem != null)
				{
					te.put(col_name, elem, new ExperimentValue(col_name), e);
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
					Object elem = list.get(i);
					if (elem != null)
					{
						te.put(key, elem, ComposedPart.compose(new NthElement(i), new ExperimentValue(key)), e);
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

	@Override
	protected PartNode explain(Part d, NodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		Cell c = Cell.mentionedCell(d);
		if (m_lastEntries == null || c == null)
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		int row = c.getRow(), col = c.getColumn();
		if (row < 1 || row > m_lastEntries.size() || col < 0 || col >= m_dimensions.length)
		{
			// This cell does not exist or is in row 0 (headers)
			root.addChild(f.getUnknownNode());
			return root;
		}
		TableEntry te = m_lastEntries.get(row - 1); // -1 since 1st row is headers
		String key = m_dimensions[col];
		if (!te.containsKey(key))
		{
			// No registered association
			root.addChild(f.getUnknownNode());
			return root;
		}
		TrackedValue tv = te.get(key);
		PartNode child = f.getPartNode(tv.getPart(), tv.getSubject());
		root.addChild(child);
		return root;
	}

	/**
	 * Reads data from an experiment. Override this method to transform the
	 * data from an experiment before putting it in the table.
	 * @param e The experiment
	 * @param key The key to read from the experiment
	 * @return The value
	 */
	protected static Object readExperiment(Experiment e, String key)
	{
		return e.read(key);
	}

	/**
	 * A map associating each column name with a tracked value.
	 */
	protected static class TableEntry extends HashMap<Object,TrackedValue>
	{
		/**
		 * Dummy UID.
		 */
		private static final long serialVersionUID = 1L;

		public void put(Object key, Object value, Part p, Object subject)
		{
			put(key, new TrackedValue(value, p, subject));
		}

	}

	@Override
	public Table dependsOn(int col, int row)
	{
		// An experiment table does not depend on another table
		return null;
	}

	@Override
	public AtomicFunction duplicate(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
