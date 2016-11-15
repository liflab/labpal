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
package ca.uqac.lif.parkbench.table;

import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.parkbench.Experiment;
import ca.uqac.lif.parkbench.table.MultidimensionalTable.Entry;

public class ExperimentMultidimensionalTable 
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
	 * The table's title
	 */
	protected String m_title;
	
	/**
	 * The dimensions of this table
	 */
	public String[] m_dimensions;
	
	public Set<Experiment> m_experiments;
	
	public ExperimentMultidimensionalTable(String[] dimensions)
	{
		super();
		m_id = s_idCounter++;
		m_experiments = new HashSet<Experiment>();
		m_dimensions = dimensions;
		m_title = "Untitled";
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
}
