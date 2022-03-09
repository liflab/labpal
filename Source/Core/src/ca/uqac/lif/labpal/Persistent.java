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
package ca.uqac.lif.labpal;

/**
 * Interface implemented by objects that can save and restore their internal
 * state based on objects they can read and write to. Persistence can be seen
 * as a weaker form of serialization: it can only update the state of an
 * already instantiated object, but cannot be used to create an instance of
 * an object from scratch.
 * 
 * @author Sylvain Hallé
 */
public interface Persistent
{
	/**
	 * Produces an object encapsulating the subject's state.
	 * @return An object containing the state of the subject
	 * @throws PersistenceException Thrown if the internal state of the subject
	 * cannot be updated
	 */
	public Object saveState() throws PersistenceException;
	
	/**
	 * Updates the internal state of the subject based on the content of an
	 * object.
	 * @param o The object
	 * @throws PersistenceException Thrown if the subject state cannot
	 * be updated based on the contents of <tt>o</tt>
	 */
	public void loadState(Object o) throws PersistenceException;
	
	/**
	 * Exception thrown by methods of the {@link Persistent} interface.
	 */
	public static class PersistenceException extends Exception
	{
		/**
		 * Dummy UID.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Creates a new persistence exception from another throwable object.
		 * @param t The throwable
		 */
		public PersistenceException(Throwable t)
		{
			super(t);
		}
		
		/**
		 * Creates a new persistence exception and assigns it a descriptive
		 * message.
		 * @param message The message
		 */
		public PersistenceException(String message)
		{
			super(message);
		}
	}
}
