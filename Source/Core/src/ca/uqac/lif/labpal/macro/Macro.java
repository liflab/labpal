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

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;

/**
 * Basic class for representing macros
 * @author Sylvain Hallé
 */
public abstract class Macro implements ExplanationQueryable
{
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
	 * Creates a new macro
	 * @param lab The lab this macro is associated with
	 */
	protected Macro(Laboratory lab)
	{
		super();
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
		m_lab = lab;
	}

	/**
	 * Gets the ID associated to the data point
	 * @return The ID
	 */
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
	 * Exports the contents of this data point as a LaTeX command
	 * @return The string defining the command
	 */
	public final String toLatex()
	{
		return toLatex(false);
	}
	
	/**
	 * Exports the contents of this data point as a LaTeX command
	 * @param with_comments Set to {@code true} to add comments before the
	 * command definition
	 * @return The string defining the command
	 */
	public abstract String toLatex(boolean with_comments);
		
	/**
	 * Resets the ID counter for macros
	 */
	public static void resetCounter()
	{
		s_counterLock.lock();
		s_idCounter = 1;
		s_counterLock.unlock();
	}
	
	@Override
	public PartNode getExplanation(Part d)
	{
		return getExplanation(d, NodeFactory.getFactory());
	}

	@Override
	public PartNode getExplanation(Part d, NodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		root.addChild(f.getUnknownNode());
		return root;
	}

}
