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

/**
 * Exception that can be thrown during the execution of an experiment to signal
 * an error condition.
 * 
 * @author Sylvain Hallé
 * @since 2.0
 */
public class ExperimentException extends Exception
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 2L;

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
}
