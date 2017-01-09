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

import java.util.HashSet;
import java.util.Set;

/**
 * A set of experiments, linked together under a name.
 * Groups have no effect on a lab or its experiments. They
 * are simply there to help organize a set of experiments.
 * Experiments that belong to the same group will be displayed
 * together in the interface.
 * 
 * @author Sylvain Hallé
 *
 */
public class Group
{
	/**
	 * The name of the group
	 */
	protected String m_name;

	/**
	 * A description for that group
	 */
	protected String m_description;

	/**
	 * The set of experiment IDs for this group
	 */
	protected Set<Integer> m_members;
	
	/**
	 * The group's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing group IDs
	 */
	protected static int s_idCounter = 1;

	/**
	 * Creates a new group
	 */
	public Group()
	{
		this("No name");
		m_id = s_idCounter++;
	}

	/**
	 * Creates a new group with a name
	 * @param name The group's name
	 */
	public Group(String name)
	{
		super();
		m_name = name;
		m_description = "";
		m_members = new HashSet<Integer>();
		m_id = s_idCounter++;
	}
	
	/**
	 * Gets the group's ID
	 * @return The ID
	 */
	public int getId()
	{
		return m_id;
	}

	/**
	 * Adds experiments to the group
	 * @param exps One or more experiments
	 * @return This group
	 */
	public Group add(Experiment ... exps)
	{
		if (exps != null)
		{
			for (Experiment e : exps)
			{
				m_members.add(e.getId());
			}
		}
		return this;
	}
	
	/**
	 * Gets the group's name
	 * @return The name
	 */
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Gets the group's description
	 * @return The description
	 */
	public String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Sets the group's description. This description should be valid
	 * HTML: it will not be escaped on display.
	 * @param d The description
	 * @return This group
	 */
	public Group setDescription(String d)
	{
		if (d != null)
		{
			m_description = d;
		}
		return this;
	}

	/**
	 * Checks if an experiment belongs to this group
	 * @param e The experiment
	 * @return true if it is part of the group, false otherwise
	 */
	public boolean belongsTo(Experiment e)
	{
		if (e == null)
		{
			return false;
		}
		return m_members.contains(e.getId());
	}
	
	/**
	 * Checks if an experiment belongs to this group
	 * @param id The experiment's ID
	 * @return true if it is part of the group, false otherwise
	 */
	public boolean belongsTo(int id)
	{
		return m_members.contains(id);
	}
	
	/**
	 * Gets the set of IDs for experiments belonging to this group
	 * @return The set of IDs
	 */
	public Set<Integer> getExperimentIds()
	{
		return m_members;
	}
	
	@Override
	public String toString()
	{
		return m_name;
	}
}
