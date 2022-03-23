/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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
package ca.uqac.lif.labpal.experiment;

import ca.uqac.lif.labpal.Identifiable;

/**
 * Exception that can be thrown during the execution of an experiment to signal
 * an error condition.
 * 
 * @author Sylvain Hallé
 * @since 2.0
 */
public class ExperimentException extends Exception implements Identifiable
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 2L;
	
	/**
	 * The ID of this exception. By convention, exceptions take the ID of the
	 * experiment that throws them.
	 */
	protected int m_id = -1;

	/**
	 * Creates an experiment exception by encapsulating another throwable
	 * object.
	 * @param t The throwable object
	 */
	public ExperimentException(Throwable t)
	{
		super(t);
	}
	
	/**
	 * Creates an experiment exception by specifying an error message.
	 * @param message The message
	 */
	public ExperimentException(String message)
	{
		super(message);
	}
	
	/**
	 * Sets the ID of this exception.
	 * @param id The ID
	 * @return This exception
	 */
	public ExperimentException setId(int id)
	{
		m_id = id;
		return this;
	}
	
	/**
	 * Gets the ID of this exception.
	 * @return The ID, or -1 if no ID has yet been assigned to this exception 
	 */
	/*@ pure @*/ public int getId()
	{
		return m_id;
	}
}
