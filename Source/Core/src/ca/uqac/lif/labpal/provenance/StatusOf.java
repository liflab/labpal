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
package ca.uqac.lif.labpal.provenance;

import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.petitpoucet.Part;

/**
 * A part that refers to the status of a {@link Stateful} object.
 * @author Sylvain Hallé
 * @since 3.0
 */
public class StatusOf implements Part
{
	/**
	 * A static reference to a single instance of the part.
	 */
	/*@ non_null @*/ public static final transient StatusOf instance = new StatusOf();
	
	/**
	 * Creates a new instance of the part.
	 */
	private StatusOf()
	{
		super();
	}
	
	@Override
	public boolean appliesTo(Object o)
	{
		return o instanceof Stateful;
	}

	@Override
	public Part head()
	{
		return this;
	}

	@Override
	public Part tail()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return "Status of";
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof StatusOf))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return 0;
	}	
}
