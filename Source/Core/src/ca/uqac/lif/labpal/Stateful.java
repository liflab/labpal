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
package ca.uqac.lif.labpal;

import java.util.Collection;

/**
 * Interface implemented by runnable objects whose internal state follows a
 * lifecycle. At any moment, a stateful object can be in one of seven states:
 * <ul>
 * <li><em>ready</em>: the object is ready to be run</li>
 * <li><em>uninitialized</em>: the object needs to be prepared before being
 * allowed to run, and this preparation has not been done yet</li>
 * <li><em>preparing</em>: the object is preparing itself to be run</li>
 * <li><em>running</em>: the object is running</li>
 * <li><em>done</em>: the object has successfully completed its execution</li>
 * <li><em>failed</em>: the object's execution is finished and has resulted
 * in an error</li>
 * <li><em>interrupted</em>: the object's execution has been forcefully
 * interrupted before its completion</li>
 * </ul> 
 * <p>
 * The object can evolve from one state to another according to the sequence
 * illustrated below. Purple circles are the object's possible initial states,
 * and cyan circles are the object's possible final states.
 * <p>
 * <img src="{@docRoot}/doc-files/Stateful.png" alt="Lifecycle graph" />
 * <p>
 * The interface declares three methods:
 * <ul>
 * <li>{@link #getStatus()} provides a means to query the state of the object
 * at any moment;</li>
 * <li>{@link #reset()} puts the object's internal state back into its
 * original initial state;</li>
 * <li>{@link #getProgression()} provides an estimate of the object's
 * progression, which can be used as a form of "progress bar"</li>
 * </ul>
 * <p>
 * Classes that are not runnable may also implement the {@link Stateful}
 * interface if they themselves depend on other stateful objects.
 *  
 * @author Sylvain Hallé
 * @since 3.0
 */
public interface Stateful 
{
	public enum Status {UNINITIALIZED, PREPARING, READY, RUNNING, DONE, INTERRUPTED, FAILED}
	
	/**
	 * Gets the current state of the object.
	 * @return The state
	 */
	/*@ non_null @*/ public Status getStatus();
	
	/**
	 * Resets the object back to its initial state.
	 */
	public void reset();
	
	/**
	 * Gets the progression of the object in its lifecycle. It is up to each
	 * object to define an appropriate progression measure, provided some
	 * conventions are followed:
	 * <ul>
	 * <li>an object in the <em>uninitialized</em> state should advertise a
	 * progression of 0</li>
	 * <li>an object in the <em>done</em> state should advertise a progression
	 * of 1</li>
	 * <li>progression is monotonic: successive calls to the method
	 * should produce a non-decreasing sequence of values</li>
	 * </ul>
	 * @return A fraction between 0 and 1 representing the object's estimated
	 * progression along its lifecycle
	 */
	public float getProgression();
	
	/**
	 * Compares two states and returns the one considered the "lowest",
	 * according to an implicit ordering of object states. This ordering
	 * is as follows:
	 * <p>
	 * running &lt; preparing &lt; failed &lt; interrupted &lt; uninitialized &lt; ready &lt; done
	 * @param s1 The first state
	 * @param s2 The second state
	 * @return The lowest of the two states
	 */
	private static Status getLowestStatus(Status s1, Status s2)
	{
		if  (s1 == Status.RUNNING || s2 == Status.RUNNING)
		{
			return Status.RUNNING;
		}
		if (s1 == Status.PREPARING || s2 == Status.PREPARING)
		{
			return Status.PREPARING;
		}
		if (s1 == Status.FAILED || s2 == Status.FAILED)
		{
			return Status.FAILED;
		}
		if (s1 == Status.INTERRUPTED || s2 == Status.INTERRUPTED)
		{
			return Status.INTERRUPTED;
		}
		if (s1 == Status.UNINITIALIZED || s2 == Status.UNINITIALIZED)
		{
			return Status.UNINITIALIZED;
		}
		if (s1 == Status.READY || s2 == Status.READY)
		{
			return Status.READY;
		}
		return Status.DONE;
	}
	
	/**
	 * Returns the "lowest" state of a collection of stateful objects, according
	 * to an implicit ordering of object states.
	 * @see #getLowestStatus(Status, Status)
	 */
	public static Status getLowestStatus(Collection<? extends Stateful> objects)
	{
		Status status = Status.DONE;
		for (Stateful s : objects)
		{
			status = getLowestStatus(status, s.getStatus());
			if (status == Status.RUNNING)
			{
				// Shortcut: no use in iterating further, already at lowest
				return status;
			}
		}
		return status;
	}
	
	/**
	 * Gets a status value based on a string expression.
	 * @param s The string
	 * @return The status
	 */
	public static Status getStatus(String s)
	{
		switch (s)
		{
		case "UNINITIALIZED":
			return Status.UNINITIALIZED;
		case "PREPARING":
			return Status.PREPARING;
		case "READY":
			return Status.READY;
		case "RUNNING":
			return Status.RUNNING;
		case "DONE":
			return Status.DONE;
		case "INTERRUPTED":
			return Status.INTERRUPTED;
		case "FAILED":
			return Status.FAILED;
		default:
			return Status.UNINITIALIZED;
		}
	}

}
