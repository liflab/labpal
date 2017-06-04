/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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

/**
 * Message indicating to the user that the lab's environment has problems.
 * @see Laboratory#isEnvironmentOk()
 * @author Sylvain Hallé
 */
public class EnvironmentMessage
{
	/**
	 * Levels of severity for the environmental problems
	 */
	public static enum Severity {FATAL, WARNING};

	/**
	 * The message to be displayed to the user
	 */
	protected String m_message;
	
	/**
	 * The level of severity associated to this message
	 */
	protected Severity m_severity;
		
	/**
	 * Creates a new environment message
	 * @param message The message to be displayed to the user
	 * @param s The level of severity associated to this message
	 */
	public EnvironmentMessage(String message, Severity s)
	{
		super();
		m_message = message;
		m_severity = s;
	}
	
	/**
	 * Gets the message to be displayed to the user
	 * @return The message
	 */
	public String getMessage()
	{
		return m_message;
	}
	
	/**
	 * Gets the level of severity associated to this message
	 * @return The level
	 */
	public Severity getSeverity()
	{
		return m_severity;
	}
	
	@Override
	public String toString()
	{
		switch (m_severity)
		{
		case FATAL:
			return "FATAL: " + m_message;
		case WARNING:
			return "WARNING: " + m_message;
		}
		return m_message;
	}
}
