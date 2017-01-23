/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

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
package ca.uqac.lif.labpal.provenance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.labpal.table.ExperimentTable;

public class DataTracker
{
	/**
	 * A map keeping track of who owns what data point
	 */
	protected Map<String,DataOwner> m_owners;
	
	/**
	 * Creates a new data tracker
	 */
	public DataTracker()
	{
		super();
		m_owners = new HashMap<String,DataOwner>();
	}
	
	public void setOwner(String prefix, DataOwner owner)
	{
		m_owners.put(prefix, owner);
	}
		
	/**
	 * Retrieves the owner of a data point
	 * @param id The id for the data point
	 * @return The owner, or {@code null} if no such data point exists
	 */
	public DataOwner getOwner(String id)
	{
		for (Map.Entry<String,DataOwner> me : m_owners.entrySet())
		{
			if (id.startsWith(me.getKey()))
			{
				return me.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Builds a provenance tree for a given data point 
	 * @param id The ID of the data point
	 * @return The root of the provenance tree
	 */
	public ProvenanceNode explain(String id)
	{
		Set<String> ids = new HashSet<String>();
		ids.add(id);
		return explain(id, ids);
	}

	/**
	 * Builds a provenance tree for a given data point 
	 * @param id The ID of the data point
	 * @param added_ids A set of IDs already included in the tree. This
	 *   is to avoid infinite looping due to possible circular dependencies.
	 * @param factory A factory for creating instances of provenance nodes
	 *   with unique IDs
	 * @return The root of the provenance tree
	 */
	protected ProvenanceNode explain(String id, Set<String> added_ids)
	{
		ProvenanceNode dep = null;
		DataOwner owner = getOwner(id);
		if (owner != null)
		{
			dep = owner.dependsOn(id);
			List<ProvenanceNode> parents = dep.getParents();
			int i = 0;
			for (ProvenanceNode dep_id : parents)
			{
				if (!added_ids.contains(dep_id))
				{
					String datapoint_id = dep_id.getDataPointId();
					if (!added_ids.contains(datapoint_id))
					{
						added_ids.add(datapoint_id);
						ProvenanceNode pn_parent = explain(datapoint_id, added_ids);
						if (pn_parent != null)
						{
							dep.replaceParent(i, pn_parent);
						}
					}
				}
				i++;
			}
		}
		return dep;
	}
}
