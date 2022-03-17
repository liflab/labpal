/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2022 Sylvain Hallé

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

import ca.uqac.lif.dag.Node;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.dag.LeafCrawler.LeafFetcher;
import ca.uqac.lif.labpal.claim.Troolean.Value;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;

/**
 * A claim expressed as a
 * <a href="https://github.com/liflab/petitpoucet">Petit Poucet</a> function.
 * By expressing a claim in such a way, explainability of the result is
 * directly provided by Petit Poucet's explainability capabilities.
 * @author Sylvain Hallé
 * @since 3.0
 */
public abstract class FunctionClaim extends Claim
{
	/**
	 * The condition to be evaluated.
	 */
	/*@ non_null @*/ protected final Function m_condition;
	
	/**
	 * The input given to the claim the last time it was evaluated.
	 */
	/*@ null @*/ protected Object[] m_input;

	/**
	 * Creates a new function claim.
	 * @param condition The condition to be evaluated
	 */
	public FunctionClaim(/*@ non_null @*/ Function condition)
	{
		super();
		m_condition = condition;
	}

	@Override
	public final PartNode getExplanation(Part part)
	{
		return getExplanation(part, NodeFactory.getFactory());
	}

	@Override
	public PartNode getExplanation(Part part, NodeFactory factory)
	{
		if (m_input == null)
		{
			// Claim needs to be calculated once before an explanation is available
			evaluate();
		}
		PartNode root = ((ExplanationQueryable) m_condition).getExplanation(part, factory);
		if (m_input == null || !(m_input[0] instanceof List))
		{
			return root;
		}
		List<?> in_list = (List<?>) m_input[0];
		LeafFetcher lf = new LeafFetcher(root);
		lf.crawl();
		for (Node leaf : lf.getLeaves())
		{
			if (!(leaf instanceof PartNode))
			{
				continue;
			}
			PartNode pn_leaf = (PartNode) leaf;
			Part pnp_leaf = pn_leaf.getPart();
			int elem_index = NthElement.mentionedElement(pnp_leaf);
			if (elem_index >= 0)
			{
				if (elem_index < in_list.size())
				{
					Part new_part = getElementPart(pnp_leaf);
					pn_leaf.addChild(factory.getPartNode(new_part, in_list.get(elem_index)));
				}
				else
				{
					pn_leaf.addChild(factory.getUnknownNode());
				}
			}
		}
		return root;
	}

	@Override
	/*@ non_null @*/ public Value evaluate()
	{
		m_condition.reset();
		m_input = getInput();
		Object[] out = m_condition.evaluate(m_input);
		if (!(out[0] instanceof Value))
		{
			// A claim that does not produce a value is a failed claim
			return Value.FALSE;	
		}
		return (Value) out[0];
	}
	
	protected Part getElementPart(Part p)
	{
		if (p instanceof NthInput)
		{
			return Part.all;
		}
		if (p instanceof ComposedPart)
		{
			List<Part> desigs = new ArrayList<Part>();
			ComposedPart cp = (ComposedPart) p;
			for (int i = 0; i < cp.size(); i++)
			{
				Part in_p = cp.get(i);
				if (in_p instanceof NthElement)
				{
					break;
				}
				desigs.add(in_p);
			}
			return ComposedPart.compose(desigs);
		}
		return Part.nothing;
	}

	
	/**
	 * Provides the input(s) to be given to the function that composes the claim.
	 * @return An array of objects, corresponding to the arguments of the
	 * function
	 */
	protected abstract Object[] getInput();

}
