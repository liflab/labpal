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

import java.util.HashMap;
import java.util.Map;

import ca.uqac.lif.labpal.provenance.NodeFunction;

/**
 * An entry in a data table
 */
public class TableEntry extends HashMap<String,Object>
{
	/**
	 * Dummy UID
	 */
	private static final transient long serialVersionUID = 1L;
	
	/**
	 * An (optional) index of the row where this entry is in the table
	 */
	protected int m_rowIndex = -1;
	
	/**
	 * Associates to each key of the map to a set of datapoint IDs
	 */
	private final Map<String,NodeFunction> m_datapointIds;
	
	public TableEntry()
	{
		super();
		m_datapointIds = new HashMap<String,NodeFunction>();
	}
	
	public TableEntry(String key, Object value)
	{
		this(key, value, null);
	}
	
	public TableEntry(String key, Object value, NodeFunction node)
	{
		this();
		put(key, value, node);
	}
	
	public TableEntry(TableEntry e)
	{
		this();
		putAll(e);
		m_datapointIds.putAll(e.m_datapointIds);
	}
	
	public void put(String key, Object value, NodeFunction node)
	{
		put(key, value);
		m_datapointIds.put(key, node);
	}
	
	/**
	 * Gets the index of the row in the table that corresponds to this
	 * table entry.
	 * @return An index, or -1 if no index was given to this entry
	 */
	public int getRowIndex()
	{
		return m_rowIndex;
	}
	
	@Override
	public int hashCode()
	{
		int x = 0;
		for (Object o : values())
		{
			if (o != null)
			{
				x += o.hashCode();
			}
		}
		return x;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof TableEntry))
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}
		TableEntry te = (TableEntry) o;
		if (size() != te.size())
		{
			return false;
		}
		for (Map.Entry<String,Object> entry : entrySet())
		{
			Object e = te.get(entry.getKey());
			if (e == null)
			{
				if (entry.getValue() == null)
					continue; // Both are null
				else
					return false;
			}
			assert e != null;
			if (!e.equals(entry.getValue()))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets the datapoint IDs associated to a key
	 * @param key
	 * @return
	 */
	public NodeFunction getDependency(String key)
	{
		if (!m_datapointIds.containsKey(key))
		{
			return null;
		}
		return m_datapointIds.get(key);
	}
	
	/**
	 * Adds a new data point as a dependency of a datapoint contained in this
	 * table entry.
	 * @param key The key corresponding to the table entry
	 * @param dependency The data point entry to add as a dependency
	 */
	public void addDependency(String key, NodeFunction dependency)
	{
		if (dependency == null)
		{
			System.out.println("BIZ");
		}
		m_datapointIds.put(key, dependency);
	}

}
