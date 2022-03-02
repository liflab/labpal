package ca.uqac.lif.labpal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Group<T extends Comparable<T>> implements Dependent<T>
{
	/**
	 * A numerical ID for groups.
	 */
	protected int m_id; 
	
	/**
	 * The set of objects contained in this group.
	 */
	/*@ non_null @*/ protected Set<T> m_objects;
	
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
		m_objects = new HashSet<T>();
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
			m_objects.add(o);
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
	public Group<T> add(Collection<T> objects)
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
	 * Gets the set of objects contained within this group.
	 * @return The set of objects
	 */
	/*@ pure non_null @*/ public Set<T> getObjects()
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
