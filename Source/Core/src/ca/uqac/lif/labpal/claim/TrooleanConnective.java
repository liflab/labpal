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
import ca.uqac.lif.labpal.claim.Troolean.Value;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.NthOutput;

/**
 * A connective for three-valued logic. This class forms the basis of operators
 * {@link And} and {@link Or}.
 * 
 * @author Sylvain Hallé
 */
public abstract class TrooleanConnective extends AtomicFunction
{
	/**
	 * The list of inputs that evaluate to <em>true</em>.
	 */
	/*@ non_null @*/ protected final List<Integer> m_trues;
	
	/**
	 * The list of inputs that evaluate to <em>false</em>.
	 */
	/*@ non_null @*/ protected final List<Integer> m_falses;
	
	/**
	 * The list of inputs that evaluate to <em>inconclusive</em>.
	 */
	/*@ non_null @*/ protected final List<Integer> m_inconclusives;
	
	/**
	 * Creates a new instance of the connective.
	 * @param in_arity The input arity of the function
	 */
	public TrooleanConnective(int in_arity)
	{
		super(in_arity, 1);
		m_trues = new ArrayList<Integer>(in_arity);
		m_falses = new ArrayList<Integer>(in_arity);
		m_inconclusives = new ArrayList<Integer>(in_arity);
	}
	
	@Override
	public Object[] getValue(Object ... inputs)
	{
		m_trues.clear();
		m_falses.clear();
		m_inconclusives.clear();
		for (int i = 0; i < inputs.length; i++)
		{
			if (inputs[i] == Value.FALSE)
			{
				m_falses.add(i);
			}
			else if (inputs[i] == Value.TRUE)
			{
				m_trues.add(i);
			}
			else
			{
				m_inconclusives.add(i);
			}
		}
		Value v = getVerdict(m_trues, m_falses, m_inconclusives);
		return new Object[] {v};
	}
	
	@Override
	public PartNode getExplanation(Part p, RelationNodeFactory f)
	{
		PartNode root = f.getPartNode(p, this);
		List<Integer> or_list = getOrList();
		List<Integer> and_list = getAndList();
		if (!or_list.isEmpty())
		{
			process(root, p, or_list, f.getOrNode(), f);
		}
		else if (!m_inconclusives.isEmpty())
		{
			LabelledNode inconcs = process(p, m_inconclusives, f.getOrNode(), f);
			LabelledNode ands = process(p, and_list, f.getAndNode(), f);
			if (ands != null)
			{
				AndNode and = f.getAndNode();
				and.addChild(inconcs);
				and.addChild(ands);
				root.addChild(and);
			}
			else
			{
				root.addChild(inconcs); // Not null as the list is not empty
			}
		}
		else
		{
			if (or_list.isEmpty() && m_inconclusives.isEmpty())
			{
				// All elements under the AND: just return the whole input
				// instead of conjunction of each part
				Part new_p = NthOutput.replaceOutByIn(p, 0);
				root.addChild(f.getPartNode(new_p, this));
			}
			else
			{
				process(root, p, and_list, f.getAndNode(), f);
			}
		}
		return root;
	}
	
	protected void process(LabelledNode root, Part p, List<Integer> list, LabelledNode conn, RelationNodeFactory f)
	{
		LabelledNode child = process(p, list, conn, f);
		if (child != null)
		{
			root.addChild(child);
		}
	}
	
	protected LabelledNode process(Part p, List<Integer> list, LabelledNode conn, RelationNodeFactory f)
	{
		if (list.isEmpty())
		{
			return null;
		}
		if (list.size() == 1)
		{
			return f.getPartNode(NthOutput.replaceOutByIn(p, list.get(0)), this);
		}
		for (int i : list)
		{
			conn.addChild(f.getPartNode(NthOutput.replaceOutByIn(p, i), this));
		}
		return conn;
	}
	
	/**
	 * Evaluates the connective by looking at the input arguments that take the
	 * values true, false and inconclusive. 
	 * @param trues The list of argument indices that evaluate to true
	 * @param falses The list of argument indices that evaluate to false
	 * @param inconclusives The list of argument indices that evaluate to
	 * inconclusive
	 * @return The value of the connective
	 */
	protected abstract Value getVerdict(List<Integer> trues, List<Integer> falses, List<Integer> inconclusives);
	
	/**
	 * Gets the list of argument indices which, when producing the explanation
	 * graph of the output value, would be placed under an OR node. For
	 * conjunction, this corresponds to arguments that evaluate to false, and
	 * for disjunction, this corresponds to arguments that evaluate to true.
	 * @return The list of argument indices
	 */
	protected abstract List<Integer> getOrList();
	
	/**
	 * Gets the list of argument indices which, when producing the explanation
	 * graph of the output value, would be placed under an AND node. For
	 * conjunction, this corresponds to arguments that evaluate to true, and
	 * for disjunction, this corresponds to arguments that evaluate to false.
	 * @return The list of argument indices
	 */
	protected abstract List<Integer> getAndList();
	
	/**
	 * Function calculating the conjunction of three-valued verdicts. Conjunction
	 * is defined according to the following truth table:
	 * <p>
	 * <table border="1">
	 * <tr><th>&and;</th><th>&bot;</th><th>?</th><th>&top;</th></tr>
	 * <tr><th>&bot;</th><td>&bot;</td><td>&bot;</td><td>&bot;</td></tr>
	 * <tr><th>?</th><td>&bot;</td><td>?</td><td>?</td></tr>
	 * <tr><th>&top;</th><td>&bot;</td><td>?</td><td>&top;</td></tr>
	 * </table>
	 */
	public static class And extends TrooleanConnective
	{
		/**
		 * Creates a new instance of the connective.
		 * @param in_arity The input arity of the function
		 */
		public And(int in_arity)
		{
			super(in_arity);
		}

		@Override
		public And duplicate(boolean with_state)
		{
			And a = new And(getInputArity());
			if (with_state)
			{
				a.m_falses.addAll(m_falses);
				a.m_trues.addAll(m_trues);
				a.m_inconclusives.addAll(m_inconclusives);
			}
			return a;
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
		public String toString()
		{
			return "And";
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
	}
	
	/**
	 * Function calculating the disjunction of three-valued verdicts. Disjunction
	 * is defined according to the following truth table:
	 * <p>
	 * <table border="1">
	 * <tr><th>&or;</th><th>&bot;</th><th>?</th><th>&top;</th></tr>
	 * <tr><th>&bot;</th><td>&bot;</td><td>?</td><td>&top;</td></tr>
	 * <tr><th>?</th><td>?</td><td>?</td><td>&top;</td></tr>
	 * <tr><th>&top;</th><td>&top;</td><td>&top;</td><td>&top;</td></tr>
	 * </table>
	 */
	public class Or extends TrooleanConnective
	{
		/**
		 * Creates a new instance of the connective.
		 * @param in_arity The input arity of the function
		 */
		public Or(int in_arity)
		{
			super(in_arity);
		}

		@Override
		public Or duplicate(boolean with_state)
		{
			Or a = new Or(getInputArity());
			if (with_state)
			{
				a.m_falses.addAll(m_falses);
				a.m_trues.addAll(m_trues);
				a.m_inconclusives.addAll(m_inconclusives);
			}
			return a;
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
		public String toString()
		{
			return "Or";
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
	}
}

