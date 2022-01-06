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

import ca.uqac.lif.petitpoucet.Part;

/**
 * A part designating an element of a laboratory. Possible parts are tables,
 * plots, macros and claims, each referred to by their unique ID.
 * 
 * @author Sylvain Hallé
 */
public abstract class LaboratoryPart implements Part
{
	/**
	 * The ID of the lab's part.
	 */
	protected final int m_id;
	
	public LaboratoryPart(int id)
	{
		super();
		m_id = id;
	}
	
	public int getId()
	{
		return m_id;
	}
	
	@Override
	public LaboratoryPart head()
	{
		return this;
	}
	
	@Override
	public Part tail()
	{
		return null;
	}
	
	@Override
	public boolean appliesTo(Object o)
	{
		return o instanceof Laboratory;
	}
	
	@Override
	public int hashCode()
	{
		return m_id;
	}
	
	/**
	 * A part designating a plot with given ID inside the lab.
	 */
	public static class PlotNumber extends LaboratoryPart
	{
		public PlotNumber(int id)
		{
			super(id);
		}
		
		@Override
		public String toString()
		{
			return "Plot #" + m_id;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof PlotNumber))
			{
				return false;
			}
			return m_id == ((PlotNumber) o).m_id;
		}
	}
	
	/**
	 * A part designating a table with given ID inside the lab.
	 */
	public static class TableNumber extends LaboratoryPart
	{
		public TableNumber(int id)
		{
			super(id);
		}
		
		@Override
		public String toString()
		{
			return "Table #" + m_id;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof TableNumber))
			{
				return false;
			}
			return m_id == ((PlotNumber) o).m_id;
		}
	}
	
	/**
	 * A part designating a macro with given ID inside the lab.
	 */
	public static class MacroNumber extends LaboratoryPart
	{
		public MacroNumber(int id)
		{
			super(id);
		}
		
		@Override
		public String toString()
		{
			return "Macro #" + m_id;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof MacroNumber))
			{
				return false;
			}
			return m_id == ((PlotNumber) o).m_id;
		}
	}
	
	/**
	 * A part designating a claim with given ID inside the lab.
	 */
	public static class ClaimNumber extends LaboratoryPart
	{
		public ClaimNumber(int id)
		{
			super(id);
		}
		
		@Override
		public String toString()
		{
			return "Claim #" + m_id;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof ClaimNumber))
			{
				return false;
			}
			return m_id == ((PlotNumber) o).m_id;
		}
	}
	
}
