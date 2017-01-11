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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.FileHelper;
import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.DataListener;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.Row;
import de.erichseifert.gral.data.statistics.Statistics;

/**
 * A table made of concrete values
 * @author Sylvain Hallé
 */
public class DataTable extends Table implements DataSource
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
	 * The symbol used to separate data values in a CSV rendition
	 */
	public static final transient String s_datafileSeparator = ",";
	
	/**
	 * The symbol used to represent missing values in a CSV rendition
	 */
	public static final transient String s_datafileMissing = "?";
	
	/**
	 * The data listeners associated to this table
	 */
	protected Set<DataListener> m_dataListeners;

	/**
	 * Creates a new data table
	 * @param ordering The ordering of the columns in this table. This array
	 * should contain column names
	 */
	public DataTable(String ... ordering)
	{
		this(null, ordering);	
	}
	
	/**
	 * Creates a new data table and fills it with existing data
	 * @param entries
	 * @param ordering
	 */
	DataTable(Collection<TableEntry> entries, String ... ordering)
	{
		super();
		m_entries = new ArrayList<TableEntry>();
		if (entries != null)
		{
			m_entries.addAll(entries);
		}
		m_preferredOrdering = ordering;
		m_dataListeners = new HashSet<DataListener>();
	}
	
	@Override
	public void removeDataListener(DataListener dataListener)
	{
		m_dataListeners.remove(dataListener);
	}
	
	@Override
	public Row getRow(int row)
	{
		return new Row(this, row);
	}
	
	@Override
	public final Iterator<Comparable<?>> iterator()
	{
		return new RowIterator(this);
	}

	@Override
	public final void addDataListener(DataListener dataListener)
	{
		m_dataListeners.add(dataListener);
	}

	@Override
	public Statistics getStatistics()
	{
		return new Statistics(this);
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
		out.append("<table border=\"1\">").append(FileHelper.CRLF).append("<thead>").append(FileHelper.CRLF);
		for (String key : sort_order)
		{
			out.append("<th>").append(key).append("</th>");
		}
		out.append("</thead>").append(FileHelper.CRLF).append("<tbody>").append(FileHelper.CRLF).append("<tr>").append(FileHelper.CRLF);
		toHtml(node, out, 0, sort_order.length);
		out.append("</tr>").append(FileHelper.CRLF).append("</tbody>").append(FileHelper.CRLF).append("</table>");
		return out.toString();
	}
	
	/**
	 * Produces a flat HTML rendition of the table
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
				out.append("</tr>").append(FileHelper.CRLF).append("<tr>");
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
		if (o instanceof JsonNull)
		{
			return null;
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
		@SuppressWarnings("unchecked")
		Class<? extends Comparable<?>>[] types = new Class[m_preferredOrdering.length];
		for (int i = 0; i < m_preferredOrdering.length; i++)
		{
			String key = m_preferredOrdering[i];
			types[i] = getColumnTypeFor(key);
		}
		return types;
		//return m_columnTypes;
	}

	@Override
	public int getRowCount()
	{
		return m_entries.size();
	}

	/**
	 * Gets the type of the column of given name
	 * @param col_name The name of the column
	 * @return The type, or {@code null} if the column does not exist
	 */
	public Class<? extends Comparable<?>> getColumnTypeFor(String col_name)
	{
		for (TableEntry e : m_entries)
		{
			if (!e.containsKey(col_name))
			{
				continue;
			}
			Object elem = e.get(col_name);
			if (elem == null)
			{
				continue;
			}
			if (elem instanceof JsonNumber || elem instanceof Number)
			{
				return Float.class;
			}
		}
		return String.class;
		//return m_columnTypes[pos];
	}

	@Override
	public DataTable getDataTable(String ... ordering)
	{
		return new DataTable(m_entries, ordering);
	}

	/**
	 * Gets the name of the column at a given position in the table
	 * @param col The position
	 * @return The column's name, or null if the index is out of bounds
	 */
	public String getColumnName(int col)
	{
		if (col < 0 || col >= m_preferredOrdering.length)
		{
			return null;
		}
		return m_preferredOrdering[col];
	}

	/**
	 * Gets the position of the column of a given name in the table
	 * @param name The name
	 * @return The column's position, or -1 if the name was not found
	 */
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

	/**
	 * Gets the names of all the columns in the table
	 * @return An array of names
	 */
	public String[] getColumnNames()
	{
		return m_preferredOrdering;
	}

	/**
	 * Finds an entry with the same key-value pairs as the entry given
	 * as an argument
	 * @param e The entry
	 * @return The entry found, or {@code null} if none found
	 */
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
			out.append(FileHelper.CRLF);
		}
		return out.toString();
	}

	@Override
	public DataTable getDataTable()
	{
		return getDataTable(m_preferredOrdering);
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < m_preferredOrdering.length; i++)
		{
			if (i > 0)
			{
				out.append(s_datafileSeparator);
			}
			out.append(m_preferredOrdering[i]);
		}
		out.append(FileHelper.CRLF).append("---").append(FileHelper.CRLF);
		out.append(toCsv());
		return out.toString();
	}
	
	@Override
	public final boolean isColumnNumeric(int columnIndex)
	{
		Class<?> c = getColumnTypeFor(columnIndex);
		return c.isAssignableFrom(Float.class);
	}
	
	/**
	 * Gets the type of the column of given name
	 * @param position The position of the column, starting at 0 for the
	 *   first column
	 * @return The type, or {@code null} if the column does not exist
	 */
	public final Class<? extends Comparable<?>> getColumnTypeFor(int position)
	{
		String name = getColumnName(position);
		return getColumnTypeFor(name);
	}
	
	@Override
	public final Column getColumn(int col)
	{
		return new Column(this, col);
	}
	
	@Override
	public String getName()
	{
		return m_title;
	}

}
