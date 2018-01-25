/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2018 Sylvain Hallé

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An object that checks an assertion on the contents of a lab.
 * A claim can be a conclusion on the lab's results that one want to
 * confirm, an integrity check to make sure the results are valid, or
 * anything else. Claims come in three levels: <tt>OK</tt> means that
 * the claim is verified; <tt>WARNING</tt> means that the claim is not
 * verified, but that it is not considered as a threat to the results.
 * Finally, a claim that invalidates the results returns the status
 * <tt>FAIL</tt>.
 * <p>
 * Optionally, when a claim is not verified, it can include
 * {@link Explanation}s of its "falsehood". These explanations intend to
 * point where in the lab are the experiments, tables or other objects
 * that cause the claim to be false.
 * 
 * @author Sylvain Hallé
 */
public abstract class Claim 
{
	/**
	 * The lab on which this claim operates
	 */
	private transient Laboratory m_lab;
	
	/**
	 * The result of checking the claim on the lab
	 */
	public static enum Result {OK, WARNING, FAIL, UNKNOWN}
	
	/**
	 * The last result obtained when checking the claim
	 */
	private transient Result m_lastResult;
	
	/**
	 * A short name for the claim
	 */
	private transient String m_name = "";
	
	/**
	 * A textual description for the claim
	 */
	private transient String m_description = "";
	
	/**
	 * A set of explanations for the falsehood of this claim
	 */
	private transient Set<Explanation> m_explanations;
	
	/**
	 * A counter for claim IDs
	 */
	private static int s_idCounter = 1;
	
	/**
	 * A lock for updating the static ID counter
	 */
	private static Lock s_idCounterLock = new ReentrantLock();
	
	/**
	 * The ID associated to this particular claim
	 */
	private int m_id;
	
	/**
	 * Creates a new empty claim
	 */
	protected Claim()
	{
		this(null, "");
	}
	
	/**
	 * Creates a new claim and associates it to a laboratory
	 * @param lab The lab to associate the claim to
	 */
	public Claim(Laboratory lab, String name)
	{
		super();
		s_idCounterLock.lock();
		m_id = s_idCounter++;
		s_idCounterLock.unlock();
		m_lab = lab;
		m_name = name;
		m_explanations = new HashSet<Explanation>();
	}
	
	/**
	 * Gets the ID of this claim
	 * @return The ID
	 */
	public final int getId()
	{
		return m_id;
	}
	
	/**
	 * Checks the claim on the lab it has been assigned to
	 * @return The result of checking the claim
	 */
	public final Result check()
	{
		m_explanations.clear();
		m_lastResult = verify(m_lab);
		return m_lastResult;
	}
	
	/**
	 * Checks this claim on a lab
	 * @param lab The lab passed as parameter
	 * @return The result of checking the claim
	 */
	public abstract Result verify(Laboratory lab);
	
	/**
	 * Adds an explanation for the falsehood of this claim
	 * @param e An explanation
	 */
	protected final void addExplanation(Explanation e)
	{
		m_explanations.add(e);
	}
	
	/**
	 * Gets the textual description for this claim
	 * @return The description
	 */
	public String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Sets a textual description for this claim
	 * @param description The description
	 * @return This claim
	 */
	public Claim setDescription(String description)
	{
		m_description = description;
		return this;
	}
	
	/**
	 * Gets the short name for this claim
	 * @return The name
	 */
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Sets a short name for this claim
	 * @param name The name
	 * @return This claim
	 */
	public Claim setName(String name)
	{
		m_name = name;
		return this;
	}
	
	/**
	 * Gets the set of explanations for the falsehood of this claim  
	 * @return A set of explanations
	 */
	public final Set<Explanation> getExplanation()
	{
		return m_explanations;
	}
	
	@Override
	public int hashCode()
	{
		return m_id;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof Claim))
			return false;
		return m_id == ((Claim) o).m_id;
	}
	
	@Override
	public String toString()
	{
		return m_name;
	}
	
	/**
	 * A description of the cause for the falsehood of some claim.
	 * An explanation typically has a textual description, as well as
	 * a set of objects (tables, experiments or plots) involved in the
	 * falsehood of a claim.
	 */
	public static class Explanation
	{
		/**
		 * A set of objects (tables, experiments or plots) involved in the
		 * falsehood of a claim
		 */
		protected List<Object> m_faultyObjects;
		
		/**
		 * A textual description of the explanation
		 */
		protected String m_description;
		
		/**
		 * Creates a new explanation
		 * @param description The textual description of the explanation
		 */
		public Explanation(String description)
		{
			super();
			m_faultyObjects = new ArrayList<Object>();
			m_description = description;
		}
		
		/**
		 * Adds one or more objects to this explanation
		 * @param objects The objects to add
		 * @return This explanation
		 */
		public Explanation add(Object ... objects)
		{
			for (Object o : objects)
			{
				m_faultyObjects.add(o);
			}
			return this;
		}
		
		@Override
		public String toString()
		{
			return m_description;
		}

		/**
		 * Gets the textual description of the explanation
		 * @return The description
		 */
		public Object getDescription()
		{
			return m_description;
		}

		/**
		 * Gets the objects associated to this explanation
		 * @return The objects
		 */
		public List<Object> getObjects()
		{
			return m_faultyObjects;
		}
	}
}
