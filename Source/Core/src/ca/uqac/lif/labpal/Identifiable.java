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
 * Interface implemented by objects that are uniquely identified with a
 * number.
 * @author Sylvain Hallé
 * @since 3.0
 */
public interface Identifiable 
{
	/**
	 * Gets the numerical identifier of this object.
	 * @return The identifier
	 */
	public int getId();
	
	/**
	 * In a collection of identifiable objects, find an object with the same ID
	 * as another identifiable object.
	 * @param i The object
	 * @param collection The collection
	 * @return The identifiable with the same ID, or <tt>null</tt> if no object
	 * exists with that ID
	 */
	/*@ null @*/ public static Identifiable find(Identifiable i, Collection<? extends Identifiable> collection)
	{
		int id = i.getId();
		for (Identifiable c_i : collection)
		{
			if (c_i.getId() == id)
			{
				return c_i;
			}
		}
		return null;
	}
}
