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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ca.uqac.lif.dag.LeafCrawler.LeafFetcher;
import ca.uqac.lif.dag.Node;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.claim.ExperimentPair;
import ca.uqac.lif.labpal.claim.FunctionClaim;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;

/**
 * A claim expressed as a Petit Poucet function over experiment pairs.
 * @author Sylvain Hallé
 * @since 3.0
 */
public abstract class PairClaim extends FunctionClaim
{
	/**
	 * The list of experiment pairs that are part of the claim.
	 */
	/*@ non_null @*/ private List<ExperimentPair> m_pairs;

	/**
	 * The set of experiments on which this claim depends. This set is calculated
	 * automatically by traversing the list of experiment pairs.
	 */
	/*@ non_null @*/ private Collection<Stateful> m_dependencies;
		
	/**
	 * Creates a new pair claim instance.
	 * @param f The function to be evaluated on the list of experiment pairs
	 */
	public PairClaim(/*@ non_null @*/ Function f)
	{
		super(f);
		m_pairs = null;
		m_dependencies = null;
	}
		
	/**
	 * Adds the experiments contained in the experiment pairs as dependencies of
	 * this claim.
	 */
	private void addDependencies()
	{
		m_dependencies = new HashSet<Stateful>();
		for (ExperimentPair ep : m_pairs)
		{
			m_dependencies.add(ep.m_first);
			m_dependencies.add(ep.m_second);
		}
	}
	
	/**
	 * Populates the list of experiment pairs that are part of the claim.
	 * @param list A list, to which experiment pairs should be added as required
	 */
	protected abstract void addPairs(/*@ non_null @*/ List<ExperimentPair> list);
	
	@Override
	/*@ pure non_null @*/ public Collection<Stateful> dependsOn()
	{
		if (m_pairs == null)
		{
			m_pairs = new ArrayList<ExperimentPair>();
			addPairs(m_pairs);
			addDependencies();
		}
		return m_dependencies;
	}

	@Override
	protected Object[] getInput()
	{
		if (m_pairs == null)
		{
			m_pairs = new ArrayList<ExperimentPair>();
			addPairs(m_pairs);
			addDependencies();
		}
		return new Object[] {m_pairs};
	}
	
	@Override
	public PartNode getExplanation(Part part, RelationNodeFactory factory)
	{
		PartNode root = super.getExplanation(part, factory);
		LeafFetcher lf = new LeafFetcher(root);
		lf.crawl();
		for (Node leaf : lf.getLeaves())
		{
			if (!(leaf instanceof PartNode))
			{
				continue;
			}
			PartNode pn_leaf = (PartNode) leaf;
			if (!(pn_leaf.getSubject() instanceof ExperimentPair))
			{
				continue;
			}
			ExperimentPair ep = (ExperimentPair) pn_leaf.getSubject();
			Part pnp_leaf = pn_leaf.getPart();
			int index = NthElement.mentionedElement(pnp_leaf);
			Experiment subject = ep.m_first;
			if (index == 1)
			{
				subject = ep.m_second;
			}
			Part new_p = getElementPart(pnp_leaf);
			PartNode new_leaf = factory.getPartNode(ComposedPart.compose(new_p), subject);
			pn_leaf.addChild(new_leaf);
		}
		return root;
	}
}
