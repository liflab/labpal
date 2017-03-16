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

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNull;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.mtnp.table.rendering.LatexTableRenderer;

/**
 * A named, user-defined data point computed from the contents of a
 * laboratory
 * @author Sylvain Hallé
 */
public class MacroScalar extends Macro
{
	/**
	 * A name associated to the data point
	 */
	protected String m_name;
	
	/**
	 * A description associated to the data point
	 */
	protected String m_description;
	
	/**
	 * Creates a new macro of given name
	 * @param name The name
	 */
	public MacroScalar(Laboratory lab, String name)
	{
		this(lab, name, "");
	}
	
	/**
	 * Creates a new macro of given name
	 * @param name The name
	 * @param description The description
	 */
	public MacroScalar(Laboratory lab, String name, String description)
	{
		super(lab);
		m_name = name;
		m_description = description;
	}
	
	/**
	 * Gets the name associated to the data point
	 * @return The name
	 */
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Sets the name associated to the data point
	 * @param name The name
	 * @return This data point
	 */
	public Macro setName(String name)
	{
		if (name != null && !name.isEmpty())
		{
			m_name = name;
		}
		return this;
	}
	
	/**
	 * Gets the description associated to the data point
	 * @return The description
	 */
	public String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Sets the description associated to the data point
	 * @param description The description
	 * @return This data point
	 */
	public Macro setDescription(String description)
	{
		if (description != null && !description.isEmpty())
		{
			m_description = description;
		}
		return this;
	}
	
	/**
	 * Computes the value of this data point
	 * @return The value
	 */
	public JsonElement getValue()
	{
		return JsonNull.instance;
	}
	
	/**
	 * Exports the contents of this data point as a LaTeX command
	 * @return The string defining the command
	 */
	public String toLatex(boolean with_comments)
	{
		StringBuilder out = new StringBuilder();
		if (with_comments)
		{
			out.append("% ").append(m_name).append(FileHelper.CRLF);
			out.append("% ").append(m_description).append(FileHelper.CRLF);
		}
		JsonElement value = getValue();
		out.append("\\newcommand{\\").append(m_name).append("}{\\href{").append(MacroNode.getDatapointId(this, "")).append("}{");
		if (value instanceof JsonString)
		{
			out.append(LatexTableRenderer.escape(((JsonString) value).stringValue()));
		}
		else
		{
			out.append(LatexTableRenderer.escape(value.toString()));
		}
		out.append("}}").append(FileHelper.CRLF).append(FileHelper.CRLF);
		return out.toString();
	}
}
