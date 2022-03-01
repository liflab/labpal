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
 * Interface implemented by objects whose state depends on other objects in a
 * lab. For example, a table and an assistant run each depend on a set of
 * experiments, while a plot depends on a table.
 * @author Sylvain Hallé
 *
 * @param <T> The type of the dependencies
 * @since 3.0
 */
public interface Dependent<T>
{
	/**
	 * Gets the other objects of type T this object depends on.
	 * @return The collection of other objects
	 */
	/*@ non_null @*/ public Collection<T> dependsOn();
}
