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

import java.util.regex.Pattern;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.provenance.NodeFunction;

/**
 * Provenance function that links to a macro
 * @author Sylvain Hallé
 */
public class MacroNode implements NodeFunction 
{
	protected final Macro m_macro;

	public MacroNode(Macro m)
	{
		super();
		m_macro = m;
	}

	/**
	 * Gets the identifier of a table
	 * @param t The table
	 * @return The identifier
	 */
	public static String getDatapointId(Macro m)
	{
		// We add a ".0" suffix so that a PDF viewer does not take the
		// hyperlink for a filename
		return "M" + m.getId() + ".0";
	}

	@Override
	public String toString()
	{
		return "Macro #" + m_macro.getId();
	}

	@Override
	public String getDataPointId()
	{
		return "M" + m_macro.getId() + ".0";
	}

	@Override
	public NodeFunction dependsOn()
	{
		// Can be overridden, but for now, depend on nothing
		return null;
	}
	
	public static NodeFunction dependsOn(Macro m, String datapoint_id)
	{
		// Parse the datapoint ID and call the experiment on the extracted values
		if (!datapoint_id.startsWith("M"))
			return null;
		String[] parts = datapoint_id.split(Pattern.quote(NodeFunction.s_separator));
		int id = Integer.parseInt(parts[0].substring(1).trim());
		if (id != m.getId())
		{
			// Wrong experiment
			return null;
		}
		return new MacroNode(m);
	}

	/**
	 * Gets the owner of a datapoint
	 * @param lab
	 * @param datapoint_id
	 * @return The owner, or {@code null} if this object could not
	 * find the owner
	 */
	public static Macro getOwner(Laboratory lab, String datapoint_id)
	{
		if (!datapoint_id.startsWith("M"))
			return null;
		String[] parts = datapoint_id.split(Pattern.quote(NodeFunction.s_separator));
		int id = Integer.parseInt(parts[0].substring(1).trim());
		return lab.getMacro(id);
	}

	public Macro getOwner()
	{
		return m_macro;
	}

	@Override
	public int hashCode()
	{
		return m_macro.getId();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof MacroNode))
		{
			return false;
		}
		MacroNode tcn = (MacroNode) o;
		return tcn.m_macro.getId() == m_macro.getId();
	}
}
