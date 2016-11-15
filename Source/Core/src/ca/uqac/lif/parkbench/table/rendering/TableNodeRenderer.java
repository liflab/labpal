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
package ca.uqac.lif.parkbench.table.rendering;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.parkbench.table.TableNode;

/**
 * Renders a result tree as a string of a given format. Different
 * subclasses of this class will render the tree differently.
 * 
 * @author Sylvain Hallé
 */
public abstract class TableNodeRenderer
{
	/**
	 * Renders a results tree
	 * @param node The root of the results tree
	 * @param sort_order The order in which the keys are expanded in the tree
	 * @return The rendering of that tree
	 */
	public String render(TableNode node, String[] sort_order)
	{
		int width = sort_order.length;
		StringBuilder out = new StringBuilder();
		startStructure(out);
		startKeys(out);
		for (String key : sort_order)
		{
			printKey(out, key);
		}
		endKeys(out);
		startBody(out);
		startRow(out, width);
		List<JsonElement> values = new ArrayList<JsonElement>();
		renderRecursive(node, values, out, width);
		endRow(out, width);
		endBody(out);
		endStructure(out);
		return out.toString();
	}
	
	protected void renderRecursive(TableNode cur_node, List<JsonElement> values, StringBuilder out, int max_depth)
	{
		if (values.size() > 0)
		{
			printCell(out, values, cur_node.countLeaves(), max_depth);			
		}
		boolean first_child = true;
		for (TableNode child : cur_node.m_children)
		{
			values.add((JsonElement) child.getValue());
			if (first_child)
			{
				first_child = false;
			}
			else
			{
				endRow(out, max_depth);
				startRow(out, max_depth);
				for (int i = 0; i < values.size() - 1; i++)
				{
					printRepeatedCell(out, values, i, max_depth);
				}
			}
			renderRecursive(child, values, out, max_depth);
			values.remove(values.size() - 1);
		}
	}
	
	/**
	 * Generates the portion of the output corresponding to the start
	 * of the structure to render
	 * @param out The string builder to which the rendering is written
	 */
	public abstract void startStructure(StringBuilder out);
	
	/**
	 * Generates the portion of the output corresponding to the start
	 * of the part of the structure where the tree's keys are enumerated
	 * @param out The string builder to which the rendering is written
	 */
	public abstract void startKeys(StringBuilder out);
	
	/**
	 * Prints a key
	 * @param out The string builder to which the rendering is written
	 */
	public abstract void printKey(StringBuilder out, String key);
	
	/**
	 * Generates the portion of the output corresponding to the end
	 * of the part of the structure where the tree's keys are enumerated
	 * @param out The string builder to which the rendering is written
	 */
	public abstract void endKeys(StringBuilder out);
	
	/**
	 * Generates the portion of the output corresponding to the start
	 * of the body (i.e. where the data values will be written)
	 * @param out The string builder to which the rendering is written
	 */
	public abstract void startBody(StringBuilder out);
	
	/**
	 * Generates the portion of the output corresponding to the start
	 * of a "row". Depending on the output format, a row may mean
	 * different things.
	 * @param out The string builder to which the rendering is written
	 * @param max_depth The number of columns in a row of the structure
	 */
	public abstract void startRow(StringBuilder out, int max_depth);
	
	/**
	 * Prints a cell
	 * @param out The string builder to which the rendering is written
	 * @param values The list of values in this row of the structure
	 * @param nb_children The number of children this cell has in the tree
	 * @param max_depth The number of columns in a row of the structure
	 */
	public abstract void printCell(StringBuilder out, List<JsonElement> values, int nb_children, int max_depth);
	
	/**
	 * Prints a cell whose value has already been printed in a previous row
	 * of the structure
	 * @param out The string builder to which the rendering is written
	 * @param values The list of values in this row of the structure
	 * @param index The index of the cell in the current row
	 * @param max_depth The number of columns in a row of the structure
	 */
	public abstract void printRepeatedCell(StringBuilder out, List<JsonElement> values, int index, int max_depth);
	
	/**
	 * Generates the portion of the output corresponding to the end
	 * of a "row". Depending on the output format, a row may mean
	 * different things.
	 * @param out The string builder to which the rendering is written
	 * @param max_depth The number of columns in a row of the structure
	 */
	public abstract void endRow(StringBuilder out, int max_depth);
	
	/**
	 * Generates the portion of the output corresponding to the end
	 * of the body (i.e. where the data values will be written)
	 * @param out The string builder to which the rendering is written
	 */
	public abstract void endBody(StringBuilder out);
	
	/**
	 * Generates the portion of the output corresponding to the end
	 * of the structure to render
	 * @param out The string builder to which the rendering is written
	 */
	public abstract void endStructure(StringBuilder out);
	
}
