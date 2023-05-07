/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2022 Sylvain Hallé

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
package ca.uqac.lif.labpal.macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.labpal.Identifiable;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.latex.LatexExportable;
import ca.uqac.lif.labpal.table.LatexTableRenderer;
import ca.uqac.lif.labpal.util.FileHelper;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;

/**
 * A generic object producing key-value pairs computed over the contents of a
 * lab.
 * @author Sylvain Hallé
 * @since 2.7
 */
public abstract class MultiMacro implements ExplanationQueryable, Identifiable, Stateful, LatexExportable
{
	/**
	 * Resets the ID counter for macros.
	 */
	public static final void resetCounter()
	{
		s_idCounter = 1;
	}
	
	/**
	 * The macro's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing macro IDs
	 */
	private static int s_idCounter = 1;

	/**
	 * A lock for accessing the counter
	 */
	private static Lock s_counterLock = new ReentrantLock();
	
	/**
	 * The lab associated to the macro
	 */
	protected final Laboratory m_lab;
	
	/**
	 * The names of the data points
	 */
	protected List<String> m_names;
	
	/**
	 * A description for the macro itself.
	 */
	protected String m_description;
	
	/**
	 * A description for each data point inside the macro.
	 */
	protected Map<String,String> m_descriptions;
	
	/**
	 * Creates a new macro
	 * @param lab The lab this macro is associated with
	 */
	protected MultiMacro(Laboratory lab, String ... names)
	{
		super();
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
		m_lab = lab;
		m_descriptions = new HashMap<String,String>();
		m_description = "";
		m_names = new ArrayList<String>(names.length);
		for (String name : names)
		{
			m_names.add(name.intern());
			m_descriptions.put(name, "");
		}
	}

	/**
	 * Gets the ID associated to the data point
	 * @return The ID
	 */
	@Override
	public int getId()
	{
		return m_id;
	}
	
	/**
	 * Gets the laboratory associated to the macro
	 * @return The lab
	 */
	public Laboratory getLaboratory()
	{
		return m_lab;
	}
	
	/**
	 * Adds a new named data point and its description
	 * @param name The name
	 * @param description The description
	 * @return This map
	 */
	public MultiMacro add(String name, String description)
	{
		m_names.add(name);
		m_descriptions.put(name, description);
		return this;
	}
	
	/**
	 * Gets the ordered list of data point names defined in this macro
	 * @return The list of names
	 */
	public List<String> getNames()
	{
		return m_names;
	}
	
	/**
	 * Gets the description associated to the macro as a whole.
	 * @param name The name of the data point
	 * @return The description
	 */
	/*@ non_null @*/ public String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Gets the description associated to a data point
	 * @param name The name of the data point
	 * @return The description, or the empty string if the data point
	 * does not exist
	 */
	public String getDescription(String name)
	{
		if (m_descriptions.containsKey(name))
		{
			return m_descriptions.get(name);
		}
		return "";
	}
	
	/**
	 * Sets a description for a data point.
	 * @param name The name of the data point
	 * @param description The description
	 * @return This macro
	 */
	/*@ non_null @*/ public MultiMacro setDescription(String name, String description)
	{
		m_descriptions.put(name, description);
		return this;
	}
	
	/**
	 * Sets a description for the macro as a whole.
	 * @param description The description
	 * @return This macro
	 */
	/*@ non_null @*/ public MultiMacro setDescription(String description)
	{
		m_description = description;
		return this;
	}
	
	/**
	 * Gets a map of all the values computed for each named data point in this
	 * macro
	 * @return The map
	 */
	public final Map<String,Object> getValues()
	{
		Map<String,Object> map = new HashMap<String,Object>();
		for (String name : m_names)
		{
			map.put(name, null);
		}
		computeValues(map);
		return map;
	}
	
	/**
	 * Populates the map of all the values computed for each named
	 * data point in this macro
	 * @param map A map, pre-filled with all the defined keys, each
	 * temporarily associated to the null value
	 */
	public abstract void computeValues(Map<String,Object> map);
	
	@Override
	public String toLatex()
	{
		boolean with_comments = true;
		StringBuilder out = new StringBuilder();
		Map<String,Object> values = getValues();
		for (int i = 0; i < m_names.size(); i++)
		{
			String name = m_names.get(i);
			Object value = values.get(name);
			String description = getDescription(name);
			if (with_comments)
			{
				out.append("% ").append(name).append(FileHelper.CRLF);
				out.append("% ").append(description).append(FileHelper.CRLF);
			}
			out.append("\\newcommand{\\").append(name).append("}{\\href{").append("M" + m_id + ":" + i).append("}{");
			if (value == null)
			{
				out.append("null");
			}
			else
			{
				out.append(LatexTableRenderer.escape(value.toString()));	
			}
			out.append("}}").append(FileHelper.CRLF).append(FileHelper.CRLF);
		}
		return out.toString();
	}
	
	@Override
	public PartNode getExplanation(Part d)
	{
		return getExplanation(d, RelationNodeFactory.getFactory());
	}

	@Override
	public PartNode getExplanation(Part d, RelationNodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		root.addChild(f.getUnknownNode());
		return root;
	}
}
