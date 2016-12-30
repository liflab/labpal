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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;

/**
 * A table made of concrete values
 * @author Sylvain Hallé
 */
public class DataTable extends Table
{
	/**
	 * The set of entries in the table. Note that we use a list,
	 * and not a set, as we need the entries to be enumerated in the
	 * same order every time. Otherwise, the <i>n</i>-th "row" of the
	 * table would not always refer to the same data point.
	 */
	protected List<TableEntry> m_entries;
	
	/**
	 * The preferred ordering to display the entries as a tree
	 */
	protected String[] m_preferredOrdering;
	
	/**
	 * The type of each column in the table
	 */
	public Class<? extends Comparable<?>>[] m_columnTypes;
	
	/**
	 * The symbol used to separate data values in a CSV rendition
	 */
	public static final transient String s_datafileSeparator = ",";
	
	/**
	 * The symbol used to represent missing values in a CSV rendition
	 */
	public static final transient String s_datafileMissing = "?";
	
	/**
	 * The OS-dependent line separator 
	 */
	protected static final String CRLF = System.getProperty("line.separator");

	/**
	 * Creates a new data table
	 * @param ordering
	 * @param types
	 */
	public DataTable(String[] ordering, Class<? extends Comparable<?>>[] types)
	{
		this(ordering, types, null);	
	}

	/**
	 * Creates a new data table
	 * @param ordering
	 * @param types
	 */
	public DataTable(String[] ordering, Type[] types)
	{
		this(ordering, types, null);	
	}
	
	/**
	 * Creates a new data table and fills it with existing data
	 * @param ordering
	 * @param types
	 * @param entries
	 */
	@SuppressWarnings("unchecked")
	DataTable(String[] ordering, Type[] types, Collection<TableEntry> entries)
	{
		super();
		m_entries = new ArrayList<TableEntry>();
		if (entries != null)
		{
			m_entries.addAll(entries);
		}
		m_preferredOrdering = ordering;
		m_columnTypes = new Class[ordering.length];
		for (int i = 0; i < ordering.length; i++)
		{
			if (types[i] == Type.NUMERIC)
			{
				m_columnTypes[i] = Float.class;
			}
			else
			{
				m_columnTypes[i] = String.class;
			}
		}

	}
	
	/**
	 * Creates a new data table and fills it with existing data
	 * @param ordering
	 * @param types
	 * @param entries
	 */
	DataTable(String[] ordering, Class<? extends Comparable<?>>[] types, Collection<TableEntry> entries)
	{
		super();
		m_entries = new ArrayList<TableEntry>();
		if (entries != null)
		{
			m_entries.addAll(entries);
		}
		m_preferredOrdering = ordering;
		m_columnTypes = types;
	}

	/**
	 * Adds a collection of entries to this data table
	 * @param entries The entries
	 */
	public void addAll(Collection<TableEntry> entries)
	{
		m_entries.addAll(entries);
	}
	
	/**
	 * Adds a new entry to the table
	 * @param e The entry
	 */
	public void add(TableEntry e)
	{
		m_entries.add(e);
	}
	
	/**
	 * Gets the contents of this table as a tree
	 * @return A reference to the root node of the tree
	 */
	public TableNode getTree()
	{
		if (m_preferredOrdering != null)
		{
			return getTree(m_preferredOrdering);
		}
		String[] order = null;
		for (TableEntry e : m_entries)
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
	
	protected List<TableNode> getChildren(String[] sort_order, int index, Collection<TableEntry> available_entries)
	{
		List<TableNode> children = new LinkedList<TableNode>();
		if (index >= sort_order.length)
		{
			return children;
		}
		String current_key = sort_order[index];
		Map<JsonElement,Set<TableEntry>> partition = partitionEntries(available_entries, current_key);
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
	protected static Map<JsonElement,Set<TableEntry>> partitionEntries(Collection<TableEntry> available_entries, String key)
	{
		Map<JsonElement,Set<TableEntry>> partition = new HashMap<JsonElement,Set<TableEntry>>();
		for (TableEntry e : available_entries)
		{
			JsonElement o = (JsonElement) e.get(key);
			Set<TableEntry> value_set;
			if (partition.containsKey(o))
			{
				value_set = partition.get(o);
			}
			else
			{
				value_set = new HashSet<TableEntry>();
			}
			value_set.add(e);
			partition.put(o, value_set);
		}
		return partition;
	}
	
	/**
	 * Produces a flat HTML rendition of the table
	 * @return A string containing the HTML code for the table
	 */
	public String toHtml()
	{
		if (m_preferredOrdering != null)
		{
			return toHtml(m_preferredOrdering);
		}
		String[] order = null;
		for (TableEntry e : m_entries)
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
	 * Produces a flat HTML rendition of the table, by ordering the
	 * columns in a specific way
	 * @param sort_order An array of column names specifying the order
	 *  in which they should be shown
	 * @return A string containing the HTML code for the table
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

	@Override
	public Comparable<?> get(int col, int row)
	{
		if (row < 0 || row >= m_entries.size() || col < 0 || col >= m_preferredOrdering.length)
		{
			// Out of bounds
			return null;
		}
		TableEntry e = m_entries.get(row);
		String key = m_preferredOrdering[col];
		if (!e.containsKey(key))
		{
			// This entry does not contain the key we are looking for
			return null;
		}
		Object o = e.get(key);
		if (o instanceof JsonNumber)
		{
			// Cast JsonNumbers as numbers
			return ((JsonNumber) o).numberValue().floatValue();
		}
		return (Comparable<?>) o;
	}

	@Override
	public int getColumnCount()
	{
		return m_preferredOrdering.length;
	}

	@Override
	public Class<? extends Comparable<?>>[] getColumnTypes()
	{
		return m_columnTypes;
	}

	@Override
	public int getRowCount()
	{
		return m_entries.size();
	}

	@Override
	public Class<? extends Comparable<?>> getColumnTypeFor(String col_name)
	{
		int pos = getColumnPosition(col_name);
		if (pos < 0)
		{
			return null;
		}
		return m_columnTypes[pos];
	}

	@Override
	public DataTable getConcreteTable(String[] ordering)
	{
		return new DataTable(ordering, m_columnTypes, m_entries);
	}

	@Override
	public String getColumnName(int col)
	{
		if (col < 0 || col >= m_preferredOrdering.length)
		{
			return null;
		}
		return m_preferredOrdering[col];
	}

	@Override
	public int getColumnPosition(String name)
	{
		for (int i = 0; i < m_preferredOrdering.length; i++)
		{
			if (m_preferredOrdering[i].compareTo(name) == 0)
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public String[] getColumnNames()
	{
		return m_preferredOrdering;
	}

	@Override
	public TableEntry findEntry(TableEntry e)
	{
		for (TableEntry tab_e : m_entries)
		{
			for (Entry<String,Object> kv : e.entrySet())
			{
				String key = kv.getKey();
				if (tab_e.containsKey(key) && tab_e.get(key).equals(e.get(key)))
				{
					return tab_e;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the list of entries if this table
	 * @return The entries
	 */
	public List<TableEntry> getEntries()
	{
		return m_entries;
	}
	
	/**
	 * Returns the contents of the table as a CSV string
	 * @return The CSV contents
	 */
	public String toCsv()
	{
		return toCsv(m_preferredOrdering, s_datafileSeparator, s_datafileMissing);
	}

	/**
	 * Returns the contents of the table as a CSV string
	 * @param separator The symbol used as the separator for values
	 * @param missing The symbol used for missing data
	 * @return The CSV contents
	 */

	public String toCsv(String separator, String missing)
	{
		return toCsv(m_preferredOrdering, separator, missing);
	}
	
	/**
	 * Returns the contents of the table as a CSV string
	 * @param ordering The ordering of the columns
	 * @param separator The symbol used as the separator for values
	 * @param missing The symbol used for missing data
	 * @return The CSV contents
	 */
	public String toCsv(String[] ordering, String separator, String missing)
	{
		StringBuilder out = new StringBuilder();
		for (TableEntry tab_e : m_entries)
		{
			for (int i = 0; i < ordering.length; i++)
			{
				if (i > 0)
				{
					out.append(separator);
				}
				if (tab_e.containsKey(ordering[i]) && tab_e.get(ordering[i]) != null)
				{
					Object o = tab_e.get(ordering[i]);
					out.append(o.toString());
				}
				else
				{
					out.append(missing);
				}
			}
			out.append(CRLF);
		}
		return out.toString();
	}

	@Override
	public DataTable getConcreteTable()
	{
		return getConcreteTable(m_preferredOrdering);
	}
}
