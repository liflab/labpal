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
package ca.uqac.lif.labpal.assistant;

import ca.uqac.lif.labpal.claim.Condition;
import ca.uqac.lif.labpal.claim.Troolean;

/**
 * A {@link Runnable} object that can start another {@link Runnable} based on
 * the result of evaluating a {@link Condition}. If a non-null condition is
 * provided, upon a call to {@link #run()}, the object will first call
 * {@link Condition#evaluate() evaluate()} on the condition, and proceed to run
 * only if its return value is <em>inconclusive</em>. Otherwise, it exits
 * immediately without running the underlying object.
 * <p>
 * It is this object that makes it possible to start a run of experiments that
 * will proceed until a given claim takes on a definitive Boolean value.
 * 
 * @author Sylvain Hallé
 * @since 3.0
 */
public class ConditionalRunnable implements Runnable
{
	/**
	 * The runnable object to run conditionally.
	 */
	/*@ non_null @*/ protected final Runnable m_runnable;

	/**
	 * The condition to evaluate to decide whether to run the runnable.
	 */
	/*@ null @*/ protected final Condition m_condition;

	/**
	 * Creates a new instance of conditional runnable.
	 * @param r The runnable object
	 * @param c The condition. This condition may be null, in which case the
	 * object runs unconditionally.
	 */
	public ConditionalRunnable(/*@ non_null @*/ Runnable r, /*@ null @*/ Condition c)
	{
		super();
		m_runnable = r;
		m_condition = c;
	}

	/**
	 * Runs the object if the provided condition evaluates to
	 * <em>inconclusive</em>.
	 */
	public void run()
	{
		if (m_condition != null && m_condition.evaluate() != Troolean.Value.INCONCLUSIVE)
		{
			// Condition evaluates to something else than inconclusive:
			// do not run
			return;
		}
		m_runnable.run();
	}
}
