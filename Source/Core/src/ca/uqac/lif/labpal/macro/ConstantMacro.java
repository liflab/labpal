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

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;

/**
 * Macro that returns a predefined constant.
 * @author Sylvain Hallé
 */
public class ConstantMacro extends Macro
{
	/**
	 * The predefined constant.
	 */
	/*@ non_null@*/ protected final Object m_value;
	
	/**
	 * The description of this macro.
	 */
	/*@ non_null@*/ protected final String m_description;
	
	public ConstantMacro(Laboratory lab, String name, String nickname, String description, Object value)
	{
		super(lab, name, nickname);
		m_value = value;
		m_description = description;
	}

	@Override
	public Status getStatus()
	{
		return Status.DONE;
	}

	@Override
	public void reset()
	{
		// Nothing to do
	}
	
	@Override
	public String getDescription()
	{
		return m_description;
	}

	@Override
	public float getProgression()
	{
		return 1;
	}

	@Override
	public PartNode getExplanation(Part part, RelationNodeFactory factory)
	{
		PartNode root = factory.getPartNode(part, this);
		PartNode child = factory.getPartNode(Part.all, m_value);
		root.addChild(child);
		return root;
	}

	@Override
	public Object getValue()
	{
		return m_value;
	}
}
