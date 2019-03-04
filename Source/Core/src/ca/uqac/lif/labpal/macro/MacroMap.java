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
package ca.uqac.lif.labpal.macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.mtnp.table.rendering.LatexTableRenderer;

/**
 * A collection of named data points computed in a single pass
 * @author Sylvain Hallé
 */
public abstract class MacroMap extends Macro 
{
	/**
	 * The names of the data points
	 */
	protected List<String> m_names;
	
	/**
	 * A description for each data point
	 */
	protected Map<String,String> m_descriptions;
	
	/**
	 * Creates a new macro map with given data point names
	 * @param lab The lab this macro is associated with
	 * @param names The names
	 */
	public MacroMap(Laboratory lab, String ... names)
	{
		super(lab);
		m_descriptions = new HashMap<String,String>();
		m_names = new ArrayList<String>(names.length);
		for (String name : names)
		{
			m_names.add(name.intern());
			m_descriptions.put(name, "");
		}
	}
	
	/**
	 * Adds a new named data point and its description
	 * @param name The name
	 * @param description The description
	 * @return This map
	 */
	public MacroMap add(String name, String description)
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
	 * Sets a description for a data point
	 * @param name The name of the data point
	 * @param description The description
	 * @return This map
	 */
	public MacroMap setDescription(String name, String description)
	{
		m_descriptions.put(name, description);
		return this;
	}
	
	/**
	 * Gets a map of all the values computed for each named data point in this
	 * macro
	 * @return The map
	 */
	public final Map<String,JsonElement> getValues()
	{
		Map<String,JsonElement> map = new HashMap<String,JsonElement>();
		for (String name : m_names)
		{
			map.put(name, JsonNull.instance);
		}
		computeValues(map);
		return map;
	}
	
	/**
	 * Populates the map of all the values computed for each named
	 * data point in this macro
	 * @param map A map, pre-filled with all the defined keys, each
	 * temporarily associated to the {@code JsonNull} value
	 */
	public abstract void computeValues(Map<String,JsonElement> map);
	
	@Override
	public String toLatex(boolean with_comments)
	{
		StringBuilder out = new StringBuilder();
		Map<String,JsonElement> values = getValues();
		for (String name : m_names)
		{
			JsonElement value = values.get(name);
			String description = getDescription(name);
			if (with_comments)
			{
				out.append("% ").append(name).append(FileHelper.CRLF);
				out.append("% ").append(description).append(FileHelper.CRLF);
			}
			out.append("\\newcommand{\\").append(name).append("}{\\href{").append(MacroNode.getDatapointId(this, name)).append("}{");
			if (value instanceof JsonString)
			{
				out.append(LatexTableRenderer.escape(((JsonString) value).stringValue()));
			}
			else
			{
				out.append(LatexTableRenderer.escape(value.toString()));
			}
			out.append("}}").append(FileHelper.CRLF).append(FileHelper.CRLF);
		}
		return out.toString();
	}
}
