package ca.uqac.lif.labpal.provenance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		ProvenanceNodeFactory factory = new ProvenanceNodeFactory();
		return explain(id, ids, factory);
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
	protected ProvenanceNode explain(String id, Set<String> added_ids, ProvenanceNodeFactory factory)
	{
		DataOwner owner = getOwner(id);
		ProvenanceNode pn = factory.newNode(id, owner);
		if (owner != null)
		{
			Set<String> dep_ids = owner.dependsOn(id);
			for (String dep_id : dep_ids)
			{
				if (!added_ids.contains(dep_id))
				{
					ProvenanceNode pn_parent = explain(dep_id, added_ids, factory);
					added_ids.add(dep_id);
					pn.addParent(pn_parent);
				}
			}
		}
		return pn;
	}
}
