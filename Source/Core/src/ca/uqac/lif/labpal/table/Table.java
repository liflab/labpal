/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

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
package ca.uqac.lif.labpal.table;

import java.util.Vector;

import ca.uqac.lif.labpal.Laboratory;

/**
 * A two-dimensional array of values. Tables can be passed to
 * {@link Plot} objects to generate graphics, or be passed through
 * {@link TableTransform} objects to perform transformations on them.
 */
public abstract class Table 
{
	/**
	 * The table's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing table IDs
	 */
	protected static int s_idCounter = 1;
	
	/**
	 * The laboratory this table is assigned to
	 */
	protected transient Laboratory m_lab;
	
	/**
	 * A description for this table
	 */
	protected String m_description = "";
	
	/**
	 * The table's title
	 */
	protected String m_title;
	
	public Table()
	{
		this("Untitled");
	}
	
	public Table(String title)
	{
		super();
		m_id = s_idCounter++;
		m_title = title;
	}
	
	/**
	 * Gets the table's ID
	 * @return The ID
	 */
	public Integer getId()
	{
		return m_id;
	}
	
	/**
	 * Sets the table's title
	 * @param title The title
	 * @return This table
	 */
	public Table setTitle(String title)
	{
		if (title != null)
		{
			m_title = title;
		}
		return this;
		
	}
	
	/**
	 * Sets the table's description
	 * @param description The description
	 * @return This table
	 */
	public Table setDescription(String description)
	{
		if (description != null)
		{
			m_description = description;
		}
		return this;
	}
	
	/**
	 * Gets the table's description
	 * @return The description
	 */
	public String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Gets the table's title
	 * @return The title
	 */
	public String getTitle()
	{
		return m_title;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || ! (o instanceof Table))
		{
			return false;
		}
		return m_id == ((Table) o).m_id;
	}
	
	@Override
	public int hashCode()
	{
		return m_id;
	}
	
	/**
	 * Assigns this table to a laboratory
	 * @param a The lab
	 * @return This table
	 */
	public Table assignTo(Laboratory a)
	{
		return this;
	}
	
	public abstract Vector<String> getXValues();
	
	public abstract ConcreteTable getConcreteTable();
}
