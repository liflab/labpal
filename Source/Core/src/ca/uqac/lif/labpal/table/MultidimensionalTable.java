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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonString;

public class MultidimensionalTable 
{
	/**
	 * The set of entries in the table
	 */
	protected Set<Entry> m_entries;
	
	/**
	 * The preferred ordering to display the entries as a tree
	 */
	protected String[] m_preferredOrdering;
	
	public MultidimensionalTable()
	{
		this(null);
	}
	
	public MultidimensionalTable(String[] ordering)
	{
		super();
		m_entries = new HashSet<Entry>();
		m_preferredOrdering = ordering;	
	}
	
	/**
	 * Adds a new entry to the table
	 * @param e The entry
	 */
	public void add(Entry e)
	{
		m_entries.add(e);
	}
	
	public TableNode getTree()
	{
		if (m_preferredOrdering != null)
		{
			return getTree(m_preferredOrdering);
		}
		String[] order = null;
		for (Entry e : m_entries)
		{
			order = new String[e.keySet().size()];
			int i = 0;
			for (String k : e.keySet())
			{
				order[i] = k;
				i++;
			}
			break;
		}
		return getTree(order);
	}
	
	/**
	 * Gets the table's data as a tree, with each node in the form
	 * key&nbsp;=&nbsp;value
	 * @param sort_order The order in which the dimensions of the table
	 *   should be enumerated
	 * @return A tree
	 */
	public TableNode getTree(String[] sort_order)
	{
		List<TableNode> nodes = getChildren(sort_order, 0, m_entries);
		TableNode root = new TableNode("", "");
		root.m_children = nodes;
		return root;
	}
	
	protected List<TableNode> getChildren(String[] sort_order, int index, Set<Entry> available_entries)
	{
		List<TableNode> children = new LinkedList<TableNode>();
		if (index >= sort_order.length)
		{
			return children;
		}
		String current_key = sort_order[index];
		Map<JsonElement,Set<Entry>> partition = partitionEntries(available_entries, current_key);
		List<JsonElement> keys = new LinkedList<JsonElement>();
		keys.addAll(partition.keySet());
		Collections.sort(keys);
		for (Object value : keys)
		{
			TableNode new_node = new TableNode(current_key, value);
			List<TableNode> new_node_children = getChildren(sort_order, index + 1, partition.get(value));
			new_node.m_children.addAll(new_node_children);
			children.add(new_node);
		}
		return children;
	}
	
	/**
	 * Partitions a set of entries into sets, with all entries having the
	 * same value with respect to a key being put into the same set
	 * @param available_entries The set of entries to partition
	 * @param key The key against which to partition the set
	 * @return A map from values to sets of entries
	 */
	protected static Map<JsonElement,Set<Entry>> partitionEntries(Set<Entry> available_entries, String key)
	{
		Map<JsonElement,Set<Entry>> partition = new HashMap<JsonElement,Set<Entry>>();
		for (Entry e : available_entries)
		{
			JsonElement o = (JsonElement) e.get(key);
			Set<Entry> value_set;
			if (partition.containsKey(o))
			{
				value_set = partition.get(o);
			}
			else
			{
				value_set = new HashSet<Entry>();
			}
			value_set.add(e);
			partition.put(o, value_set);
		}
		return partition;
	}
	
	/**
	 * Produces a flat HTML rendition of the table
	 * @return
	 */
	public String toHtml()
	{
		if (m_preferredOrdering != null)
		{
			return toHtml(m_preferredOrdering);
		}
		String[] order = null;
		for (Entry e : m_entries)
		{
			order = new String[e.keySet().size()];
			int i = 0;
			for (String k : e.keySet())
			{
				order[i] = k;
				i++;
			}
			break;
		}
		return toHtml(order);
	}
	
	/**
	 * Produces a flat HTML rendition of the table
	 * @return
	 */
	protected String toHtml(String[] sort_order)
	{
		TableNode node = getTree(sort_order);
		StringBuilder out = new StringBuilder();
		out.append("<table border=\"1\">\n<thead>\n");
		for (String key : sort_order)
		{
			out.append("<th>").append(key).append("</th>");
		}
		out.append("</thead>\n<tbody>\n<tr>\n");
		toHtml(node, out, 0, sort_order.length);
		out.append("</tr>\n</tbody>\n</table>");
		return out.toString();
	}
	
	/**
	 * Produces a flat HTML rendition of the table
	 * @return
	 */
	protected void toHtml(TableNode cur_node, StringBuilder out, int depth, int total_depth)
	{
		if (depth > 0)
		{
			out.append("<td>");
			if (cur_node.m_value instanceof JsonString)
			{
				out.append(((JsonString) cur_node.m_value).stringValue());
			}
			else if (cur_node.m_value instanceof JsonNull)
			{
				out.append("");
			}
			else
			{
				out.append(cur_node.m_value);
			}
			out.append("</td>");
		}
		boolean first_child = true;
		for (TableNode child : cur_node.m_children)
		{
			if (first_child)
			{
				first_child = false;
			}
			else
			{
				out.append("</tr>\n<tr>");
				for (int i = 0; i < depth; i++)
				{
					out.append("<td>-</td>");
				}
			}
			toHtml(child, out, depth + 1, total_depth);
		}
	}
	
	/**
	 * An entry in a multi-dimensional table
	 */
	public static class Entry extends HashMap<String,Object>
	{
		/**
		 * Dummy UID
		 */
		private static final transient long serialVersionUID = 1L;
		
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
			if (o == null || !(o instanceof Entry))
			{
				return false;
			}
			if (o == this)
			{
				return true;
			}
			// TODO
			return false;
		}
	}
}
