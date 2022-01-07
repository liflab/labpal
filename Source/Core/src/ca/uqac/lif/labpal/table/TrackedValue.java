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
package ca.uqac.lif.labpal.table;

import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;

/**
 * A value to which a hard-coded object/part pair is specified as its
 * explanation.
 * @author Sylvain Hallé
 */
public class TrackedValue implements ExplanationQueryable
{
	private final Object m_value;
	
	private final Part m_provenance;
	
	private final Object m_subject;
	
	public TrackedValue(Object value, Part provenance, Object subject)
	{
		super();
		m_value = value;
		m_provenance = provenance;
		m_subject = subject;
	}
	
	/*@ pure @*/ public Object getSubject()
	{
		return m_subject;
	}
	
	/*@ pure @*/ public Part getPart()
	{
		return m_provenance;
	}
	
	/*@ pure @*/ public Object getValue()
	{
		return m_value;
	}

	@Override
	public PartNode getExplanation(Part part)
	{
		return getExplanation(part, NodeFactory.getFactory());
	}

	@Override
	public PartNode getExplanation(Part part, NodeFactory factory)
	{
		PartNode root = factory.getPartNode(part, factory);
		root.addChild(factory.getPartNode(m_provenance, m_subject));
		return root;
	}
	
	@Override
	public String toString()
	{
		if (m_value == null)
		{
			return null;
		}
		return m_value.toString();
	}
	
	@Override
	public int hashCode()
	{
		return m_subject.hashCode() + m_provenance.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof TrackedValue))
		{
			return false;
		}
		TrackedValue tv = (TrackedValue) o;
		return m_subject.equals(tv.m_subject) && m_provenance.equals(tv.m_provenance);
	}
}
