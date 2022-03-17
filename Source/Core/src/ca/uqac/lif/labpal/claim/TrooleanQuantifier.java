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
package ca.uqac.lif.labpal.claim;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.dag.LeafCrawler.LeafFetcher;
import ca.uqac.lif.dag.Node;
import ca.uqac.lif.labpal.claim.Troolean.Value;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentException;
import ca.uqac.lif.petitpoucet.function.vector.VectorOutputFunction;

public abstract class TrooleanQuantifier extends TrooleanConnective
{
	/**
	 * The condition to evaluate on each element of the collection.
	 */
	/*@ non_null @*/ protected final Function m_condition;
	
	/**
	 * The list of individual function instances that have been evaluated on the
	 * elements of the input collection.
	 */
	/*@ non_null @*/ protected final List<Function> m_conditions;
	
	/**
	 * Creates a new instance of the quantifier.
	 * @param condition The condition to evaluate on each element of the input
	 */
	public TrooleanQuantifier(/*@ non_null @*/ Function condition)
	{
		super(1);
		m_condition = condition;
		m_conditions = new ArrayList<Function>();
	}
	
	@Override
	public Object[] getValue(Object ... inputs)
	{
		m_trues.clear();
		m_falses.clear();
		m_inconclusives.clear();
		m_conditions.clear();
		if (!(inputs[0] instanceof List))
		{
			throw new InvalidArgumentException("Argument must be a list");
		}
		List<?> list = (List<?>) inputs[0];
		for (int i = 0; i < list.size(); i++)
		{
			Function c_i = m_condition.duplicate(false);
			Object v = c_i.evaluate(list.get(i))[0];
			m_conditions.add(c_i);
			if (Troolean.Value.TRUE.equals(v))
			{
				m_trues.add(i);
			}
			else if (Troolean.Value.FALSE.equals(v))
			{
				m_falses.add(i);
			}
			else
			{
				m_inconclusives.add(i);
			}
		}
		return new Object[] {getVerdict(m_trues, m_falses, m_inconclusives)};
	}
	
	@Override
	protected LabelledNode process(Part p, List<Integer> list, LabelledNode conn, NodeFactory f)
	{
		if (list.isEmpty())
		{
			return null;
		}
		if (list.size() == 1)
		{
			return processChild(p, f, list.get(0));
		}
		for (int i : list)
		{
			conn.addChild(processChild(p, f, i));
		}
		return conn;
	}
	
	protected LabelledNode processChild(Part p, NodeFactory f, int i)
	{
		Function c_i = m_conditions.get(i);
		NodeFactory sub_factory = f.getFactory(p, c_i);
		PartNode sub_root = ((ExplanationQueryable) c_i).getExplanation(p, sub_factory);
		LeafFetcher lf = new LeafFetcher(sub_root);
		lf.crawl();
		for (Node leaf : lf.getLeaves())
		{
			if (!(leaf instanceof PartNode))
			{
				continue;
			}
			PartNode pn_leaf = (PartNode) leaf;
			if (pn_leaf.getSubject() != c_i)
			{
				continue;
			}
			Part pnp_leaf = pn_leaf.getPart();
			Part new_part = VectorOutputFunction.replaceInputByElement(pnp_leaf, 0, i);
			PartNode pn_child = f.getPartNode(new_part, this);
			pn_leaf.addChild(pn_child);
		}
		return sub_root;
	}
	
	/**
	 * Universal quantifier on a Troolean condition expressed over a collection
	 * of objects.
	 */
	public static class AllObjects extends TrooleanQuantifier
	{
		/**
		 * Creates a new instance of the quantifier.
		 * @param condition The condition to evaluate on each element of the input
		 */
		public AllObjects(Function condition)
		{
			super(condition);
		}

		@Override
		protected Value getVerdict(List<Integer> trues, List<Integer> falses, List<Integer> inconclusives)
		{
			if (!falses.isEmpty())
			{
				return Value.FALSE;
			}
			if (!inconclusives.isEmpty())
			{
				return Value.INCONCLUSIVE;
			}
			return Value.TRUE;
		}

		@Override
		protected List<Integer> getOrList()
		{
			return m_falses;
		}

		@Override
		protected List<Integer> getAndList()
		{
			return m_trues;
		}

		@Override
		public AllObjects duplicate(boolean with_state)
		{
			AllObjects f = new AllObjects(m_condition.duplicate(with_state));
			if (with_state)
			{
				f.m_falses.addAll(m_falses);
				f.m_trues.addAll(m_trues);
				f.m_inconclusives.addAll(m_inconclusives);
				f.m_conditions.addAll(m_conditions);
			}
			return f;
		}
		
		@Override
		public String toString()
		{
			return "All";
		}
	}
	
	/**
	 * Existential quantifier on a Troolean condition expressed over a collection
	 * of objects.
	 */
	public static class SomeObject extends TrooleanQuantifier
	{
		/**
		 * Creates a new instance of the quantifier.
		 * @param condition The condition to evaluate on each element of the input
		 */
		public SomeObject(Function condition)
		{
			super(condition);
		}

		@Override
		protected Value getVerdict(List<Integer> trues, List<Integer> falses, List<Integer> inconclusives)
		{
			if (!falses.isEmpty())
			{
				return Value.FALSE;
			}
			if (!inconclusives.isEmpty())
			{
				return Value.INCONCLUSIVE;
			}
			return Value.TRUE;
		}

		@Override
		protected List<Integer> getOrList()
		{
			return m_trues;
		}

		@Override
		protected List<Integer> getAndList()
		{
			return m_falses;
		}

		@Override
		public SomeObject duplicate(boolean with_state)
		{
			SomeObject f = new SomeObject(m_condition.duplicate(with_state));
			if (with_state)
			{
				f.m_falses.addAll(m_falses);
				f.m_trues.addAll(m_trues);
				f.m_inconclusives.addAll(m_inconclusives);
				f.m_conditions.addAll(m_conditions);
			}
			return f;
		}
		
		@Override
		public String toString()
		{
			return "Some";
		}
	}
}
