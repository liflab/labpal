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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.labpal.Identifiable;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.latex.LatexExportable;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;

/**
 * An object that calculates and returns a value.
 * @author Sylvain Hallé
 */
public abstract class Macro implements Comparable<Macro>, ExplanationQueryable, Identifiable, Stateful, LatexExportable
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
	 * A counter for auto-incrementing macro IDs.
	 */
	private static int s_idCounter = 1;

	/**
	 * A lock for accessing the counter.
	 */
	private static Lock s_counterLock = new ReentrantLock();

	/**
	 * The lab associated to the macro.
	 */
	/*@ non_null @*/ protected final Laboratory m_lab;

	/**
	 * The macro's name.
	 */
	/*@ non_null @*/ protected String m_name;

	/**
	 * The macro's nickname.
	 */
	/*@ non_null @*/ protected String m_nickname;

	/**
	 * Creates a new macro.
	 * @param lab The lab associated to the macro
	 * @param nickname The macro's nickname
	 */
	public Macro(/*@ non_null @*/ Laboratory lab, /*@ non_null @*/ String name, /*@ non_null @*/ String nickname)
	{
		super();
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
		m_lab = lab;
		m_name = name;
		m_nickname = nickname;
	}

	/**
	 * Gets the macro's name.
	 * @return The name
	 */
	/*@ pure non_null @*/ public String getName()
	{
		return m_name;
	}
	
	@Override
	/*@ pure @*/ public int getId()
	{
		return m_id;
	}

	@Override
	/*@ pure non_null @*/ public String getNickname()
	{
		return m_nickname;
	}
	
	@Override
	public int compareTo(Macro m)
	{
		if (m == null)
		{
			return -1;
		}
		return m_name.compareTo(m.getName());
	}
	
	@Override
	public PartNode getExplanation(Part part)
	{
		return getExplanation(part, RelationNodeFactory.getFactory());
	}
	
	@Override
	public String toLatex()
	{
		StringBuilder out = new StringBuilder();
		Object value = getValue();
		
		out.append("\\newcommand{\\").append(m_nickname).append("}{");
		if (value == null)
		{
			out.append("null");
		}
		else
		{
			out.append(LatexExportable.escape(getValue().toString()));
		}
		out.append("}");
		return out.toString();
	}

	/**
	 * Gets the value produced by this macro.
	 * @return The value
	 */
	public abstract Object getValue();
	
	/**
	 * Gets the description for this macro.
	 * @return The description
	 */
	public String getDescription()
	{
		return "";
	}
}
