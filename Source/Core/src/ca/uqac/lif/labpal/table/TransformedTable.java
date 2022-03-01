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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.dag.LeafCrawler;
import ca.uqac.lif.dag.Node;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Table that produces a spreadsheet by applying a function to the
 * spreadsheets produced by other tables.
 * @author Sylvain Hallé
 */
public class TransformedTable extends Table
{
	/**
	 * The function to apply to the input spreadsheet
	 */
	protected Function m_transformation;

	protected List<Table> m_inputTables;

	/**
	 * Creates a new transformed table.
	 * @param f The function to apply to the input tables
	 * @param tables The input tables
	 */
	public TransformedTable(Function f, Table ... tables)
	{
		super();
		m_transformation = f;
		m_inputTables = Arrays.asList(tables);
	}

	@Override
	protected Spreadsheet calculateSpreadsheet()
	{
		Object[] ins = new Object[m_inputTables.size()];
		for (int i = 0; i < m_inputTables.size(); i++)
		{
			ins[i] = m_inputTables.get(i).getSpreadsheet();
		}
		Object[] out = m_transformation.evaluate(ins);
		if (!(out[0] instanceof Spreadsheet))
		{
			return null;
		}
		return (Spreadsheet) out[0];
	}

	@Override
	public Table dependsOn(int col, int row)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status getStatus()
	{
		return Stateful.getLowestStatus(m_inputTables);
	}

	@Override
	protected PartNode explain(Part d, NodeFactory f)
	{
		if (!(d.head() instanceof NthOutput))
		{
			d = ComposedPart.compose(d, NthOutput.FIRST);
		}
		if (f.hasNodeFor(d, this))
		{
			// The factory already has the node we want to develop, which means
			// it has already been explained
			return f.getPartNode(d, this);
		}
		PartNode root = f.getPartNode(d, this);
		if (Cell.mentionedCell(d) == null)
		{
			// We ask the explanation of the whole table: point to its input tables
			LabelledNode to_add = root;
			if (m_inputTables.size() > 1)
			{
				AndNode and = f.getAndNode();
				root.addChild(and);
				to_add = and;
			}
			for (Table t : m_inputTables)
			{
				to_add.addChild(f.getPartNode(Part.all, t));
			}
			return root;
		}
		if (!(m_transformation instanceof ExplanationQueryable))
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		PartNode f_root = ((ExplanationQueryable) m_transformation).getExplanation(d, f);
		root.addChild(f_root);
		FetchLeaves crawler = new FetchLeaves(f_root);
		crawler.crawl();
		Set<PartNode> leaves = crawler.getLeaves();
		for (PartNode leaf : leaves)
		{
			Part p = leaf.getPart();
			int mentioned_input = NthInput.mentionedInput(p);
			if (mentioned_input < 0 || mentioned_input >= m_inputTables.size())
			{
				leaf.addChild(f.getUnknownNode());
			}
			else
			{
				Table t = m_inputTables.get(mentioned_input);
				Part new_p = NthInput.replaceInByOut(p, 0);
				PartNode sub_root = t.explain(new_p, f);
				leaf.addChild(sub_root);
			}
		}
		return root;
	}

	protected class FetchLeaves extends LeafCrawler
	{
		protected Set<PartNode> m_leaves;

		public FetchLeaves(Node start)
		{
			super(start);
			m_leaves = new HashSet<PartNode>();
		}

		/*@ pure non_null @*/ public Set<PartNode> getLeaves()
		{
			return m_leaves;
		}

		@Override
		protected void visitLeaf(Node n)
		{
			if (n instanceof PartNode)
			{
				m_leaves.add((PartNode) n);
			}
		}

	}

	@Override
	public AtomicFunction duplicate(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Experiment> dependsOn() 
	{
		Set<Experiment> exps = new HashSet<Experiment>();
		for (Table t : m_inputTables)
		{
			exps.addAll(t.dependsOn());
		}
		List<Experiment> sorted_list = new ArrayList<Experiment>(exps.size());
		sorted_list.addAll(exps);
		Collections.sort(sorted_list);
		return sorted_list;
	}
}
