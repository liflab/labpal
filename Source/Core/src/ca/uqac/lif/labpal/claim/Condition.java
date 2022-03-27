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
package ca.uqac.lif.labpal.claim;

import ca.uqac.lif.labpal.claim.Troolean.Value;

/**
 * An object that provides a Troolean value when queried.
 * @author Sylvain Hallé
 * @since 3.0
 */
public interface Condition
{
	/**
	 * Evaluates the condition.
	 * @return The return value of the condition
	 */
	public Value evaluate();
}
