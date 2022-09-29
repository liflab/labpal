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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An ordered group of lab objects.
 * @author Sylvain Hallé
 *
 * @param <T> The type of the objects in the group
 */
public abstract class Group<T extends Comparable<T>> implements Dependent<T>
{
	/**
	 * A numerical ID for groups.
	 */
	protected int m_id; 
	
	/**
	 * The set of objects contained in this group.
	 */
	/*@ non_null @*/ protected List<T> m_objects;
	
	/**
	 * The group's name.
	 */
	/*@ non_null @*/ protected String m_name;
	
	/**
	 * The group's description.
	 */
	/*@ non_null @*/ protected String m_description;
	
	/**
	 * Creates a new group.
	 * @param name The group's name
	 * @param description The group's textual description
	 */
	public Group(/*@ non_null @*/ String name, /*@ non_null @*/ String description)
	{
		super();
		m_name = name;
		m_description = description;
		m_objects = new ArrayList<T>();
	}
	
	/**
	 * Creates a new group with no description.
	 * @param name The group's name
	 */
	public Group(/*@ non_null @*/ String name)
	{
		this(name, "");
	}
	
	/**
	 * Adds objects to this group.
	 * @param objects The objects to add
	 * @return This group
	 */
	@SuppressWarnings("unchecked")
	public Group<T> add(T ... objects)
	{
		for (T o : objects)
		{
			if (!m_objects.contains(o))
			{
				m_objects.add(o);
			}
		}
		return this;
	}
	
	/**
	 * Gets the numerical ID of this group
	 * @return The ID
	 */
	public int getId()
	{
		return m_id;
	}
	
	/**
	 * Sets the numerical ID of this group.
	 * @param id The ID
	 * @return This group
	 */
	public Group<T> setId(int id)
	{
		m_id = id;
		return this;
	}
	
	/**
	 * Adds objects to this group.
	 * @param objects The objects to add
	 * @return This group
	 */
	public Group<T> add(Collection<? extends T> objects)
	{
		m_objects.addAll(objects);
		return this;
	}
	
	
	/**
	 * Gets the textual description of this group.
	 * @return The description
	 */
	/*@ pure non_null @*/ public String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Gets the name of this group.
	 * @return The name
	 */
	/*@ pure non_null @*/ public String getName()
	{
		return m_name;
	}
	
	/**
	 * Gets a sorted list of the objects contained within this group.
	 * @return The list of objects
	 */
	/*@ pure non_null @*/ public List<T> getSortedObjects()
	{
		List<T> list = new ArrayList<T>(m_objects.size());
		list.addAll(m_objects);
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Gets the list of objects contained within this group.
	 * @return The set of objects
	 */
	/*@ pure non_null @*/ public List<T> getObjects()
	{
		return m_objects;
	}
	
	/**
	 * Determines if an object belongs to this group.
	 * @param o The object
	 * @return <tt>true</tt> if the object belongs to the group, <tt>false</tt>
	 * otherwise
	 */
	/*@ pure @*/ public boolean belongsTo(T o)
	{
		return m_objects.contains(o);
	}

	@Override
	/*@ pure @*/ public Collection<T> dependsOn()
	{
		List<T> list = new ArrayList<T>(m_objects.size());
		list.addAll(m_objects);
		Collections.sort(list);
		return list;
	}
}
