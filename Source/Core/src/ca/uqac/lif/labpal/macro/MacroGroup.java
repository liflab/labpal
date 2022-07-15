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

import ca.uqac.lif.labpal.Group;
import ca.uqac.lif.labpal.Stateful;

/**
 * A group of macros.
 * @author Sylvain Hallé
 */
public class MacroGroup extends Group<Macro> implements Stateful
{
	/**
	 * Creates a new macro group.
	 * @param name The name of the group
	 */
	public MacroGroup(String name)
	{
		super(name);
	}

	@Override
	/*@ pure non_null @*/ public Status getStatus()
	{
		return Stateful.getLowestStatus(m_objects);
	}

	@Override
	/*@ pure @*/ public float getProgression()
	{
		float p = 0;
		float t = 0;
		for (Macro m : m_objects)
		{
			p += m.getProgression();
			t++;
		}
		if (t == 0)
		{
			return 0;
		}
		return p / t;
	}

	@Override
	public void reset()
	{
		for (Macro m : m_objects)
		{
			m.reset();
		}
	}
}
