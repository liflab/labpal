/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2018 Sylvain Hallé

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

import java.util.List;
import java.util.regex.Pattern;

import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.OwnershipManager;

/**
 * Provenance function that links to a macro
 * @author Sylvain Hallé
 */
public class MacroNode implements NodeFunction 
{
	protected final Macro m_macro;
	
	protected final String m_key;

	public MacroNode(Macro m, String key)
	{
		super();
		m_macro = m;
		m_key = key;
	}
	
	public MacroNode(Macro m)
	{
		this(m, "");
	}

	/**
	 * Gets the identifier of a macro
	 * @param m The macro
	 * @param key The key, in case the macro has multiple key-value
	 *   pairs
	 * @return The identifier
	 */
	public static String getDatapointId(Macro m, String key)
	{
		if (m instanceof MacroMap)
		{
			MacroMap mm = (MacroMap) m;
			int index = mm.m_names.indexOf(key.intern());
			return "M" + m.getId() + NodeFunction.s_separator + index;
		}
		// We add a ".0" suffix so that a PDF viewer does not take the
		// hyperlink for a filename
		return "M" + m.getId() + ".0";
	}

	@Override
	public String toString()
	{
		if (m_key == null || m_key.isEmpty())
		{
			return "Macro #" + m_macro.getId();
		}
		else
		{
			return "Value of " + m_key + " in Macro #" + m_macro.getId();
		}
	}

	@Override
	public String getDataPointId()
	{
		if (m_key == null || m_key.isEmpty())
		{
			return "M" + m_macro.getId() + NodeFunction.s_separator + "0";
		}
		else
		{
			int index = ((MacroMap) m_macro).m_names.indexOf(m_key.intern());
			return "M" + m_macro.getId() + NodeFunction.s_separator + index;
		}
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
			// Wrong macro
			return null;
		}
		if (m instanceof MacroScalar)
		{
			return new MacroNode(m);
		}
		int index = Integer.parseInt(parts[1]);
		List<String> names = ((MacroMap) m).getNames();
		if (index < 0 || index >= names.size())
		{
			// Wrong index
			return null;
		}
		return new MacroNode(m, names.get(index));
	}

	/**
	 * Gets the owner of a datapoint
	 * @param lab
	 * @param datapoint_id
	 * @return The owner, or {@code null} if this object could not
	 * find the owner
	 */
	public static Macro getOwner(OwnershipManager lab, String datapoint_id)
	{
		if (!datapoint_id.startsWith("M"))
			return null;
		String[] parts = datapoint_id.split(Pattern.quote(NodeFunction.s_separator));
		return (Macro) lab.getObjectWithId(parts[0]);
	}

	public Macro getOwner()
	{
		return m_macro;
	}

	@Override
	public int hashCode()
	{
		return m_macro.getId() + m_key.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof MacroNode))
		{
			return false;
		}
		MacroNode tcn = (MacroNode) o;
		return tcn.m_macro.getId() == m_macro.getId() 
				&& tcn.m_key.compareTo(m_key) == 0;
	}
}
