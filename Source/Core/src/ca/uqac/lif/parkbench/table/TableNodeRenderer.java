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

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.json.JsonElement;

public abstract class TableNodeRenderer
{
	public String render(TableNode node, String[] sort_order)
	{
		StringBuilder out = new StringBuilder();
		startStructure(out);
		startKeys(out);
		for (String key : sort_order)
		{
			printKey(out, key);
		}
		endKeys(out);
		startBody(out);
		startRow(out);
		List<JsonElement> values = new ArrayList<JsonElement>();
		renderRecursive(node, values, out, sort_order.length);
		endRow(out);
		endBody(out);
		endStructure(out);
		return out.toString();
	}
	
	protected void renderRecursive(TableNode cur_node, List<JsonElement> values, StringBuilder out, int max_depth)
	{
		if (values.size() > 0)
		{
			printCell(out, values, cur_node.countLeaves());			
		}
		boolean first_child = true;
		for (TableNode child : cur_node.m_children)
		{
			values.add((JsonElement) child.m_value);
			if (first_child)
			{
				first_child = false;
			}
			else
			{
				endRow(out);
				startRow(out);
				for (int i = 0; i < values.size() - 1; i++)
				{
					printRepeatedCell(out, values, i);
				}
			}
			renderRecursive(child, values, out, max_depth);
			values.remove(values.size() - 1);
		}
	}
	
	public abstract void startStructure(StringBuilder out);
	
	public abstract void startKeys(StringBuilder out);
	
	public abstract void printKey(StringBuilder out, String key);
	
	public abstract void endKeys(StringBuilder out);
	
	public abstract void startBody(StringBuilder out);
	
	public abstract void startRow(StringBuilder out);
	
	public abstract void printCell(StringBuilder out, List<JsonElement> values, int nb_children);
	
	public abstract void printRepeatedCell(StringBuilder out, List<JsonElement> values, int index);
	
	public abstract void endRow(StringBuilder out);
	
	public abstract void endBody(StringBuilder out);
	
	public abstract void endStructure(StringBuilder out);
	
}
