/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

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
package ca.uqac.lif.labpal.provenance;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.plot.PlotNode;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.TableCellNode;
import ca.uqac.lif.labpal.table.TableFunctionNode;
import ca.uqac.lif.labpal.table.TableNode;

public class DataTracker
{
	/**
	 * The lab
	 */
	protected Laboratory m_lab;

	/**
	 * Creates a new data tracker
	 */
	public DataTracker(Laboratory lab)
	{
		super();
		m_lab = lab;
	}

	public Object getOwner(String id)
	{
		// Is it a table?
		Table t = TableCellNode.getOwner(m_lab, id);
		if (t != null)
		{
			return t;
		}
		t = TableNode.getOwner(m_lab, id);
		if (t != null)
		{
			return t;
		}
		// Is it an experiment?
		Experiment e = ExperimentValue.getOwner(m_lab, id);
		if (e != null)
		{
			return e;
		}
		// Is it a plot?
		Plot p = PlotNode.getOwner(m_lab, id);
		if (p != null)
		{
			return p;
		}
		return null;
	}

	public NodeFunction getNodeFunction(String datapoint_id)
	{
		Object owner = getOwner(datapoint_id);
		if (owner == null)
		{
			return null;
		}
		if (owner instanceof Table)
		{
			NodeFunction nf = TableCellNode.dependsOn((Table) owner, datapoint_id);
			if (nf != null)
			{
				return nf;
			}
			return new TableFunctionNode((Table) owner, 0, 0);
		}
		if (owner instanceof Experiment)
		{
			return ExperimentValue.dependsOn((Experiment) owner, datapoint_id);
		}
		if (owner instanceof Plot)
		{
			return PlotNode.dependsOn((Plot) owner, datapoint_id);
		}
		return null;
	}

	public ProvenanceNode explain(String datapoint_id)
	{
		NodeFunction nf = getNodeFunction(datapoint_id);
		if (nf == null)
		{
			return BrokenChain.instance;
		}
		return explain(nf);
	}

	/**
	 * Builds a provenance tree for a given data point 
	 * @param id The ID of the current node
	 * @return The root of the provenance tree
	 */
	public ProvenanceNode explain(NodeFunction nf)
	{
		Set<NodeFunction> functions = new HashSet<NodeFunction>();
		return explain(nf, functions, 10);
	}

	/**
	 * Builds a provenance tree for a given data point 
	 * @param nf The ID of the current node
	 * @param added_ids A set of nodes already included in the tree. This
	 *   is to avoid infinite looping due to possible circular dependencies.
	 * @return The root of the provenance tree
	 */
	protected ProvenanceNode explain(NodeFunction nf, Set<NodeFunction> seen_functions, int depth)
	{
		if (depth == 0)
		{
			return InfiniteLoop.instance;
		}
		List<ProvenanceNode> nodes = new LinkedList<ProvenanceNode>();
		if (nf instanceof ExperimentValue)
		{
			// Leaf
			ProvenanceNode pn = new ProvenanceNode(nf);
			nodes.add(pn);
			return pn;
		}
		if (nf instanceof TableFunctionNode)
		{
			ProvenanceNode pn = new ProvenanceNode(nf);
			// TODO: in the case of whole tables, track dependencies of each of its cells
			return pn;
		}
		if (nf instanceof PlotNode)
		{
			ProvenanceNode pn = new ProvenanceNode(nf);
			PlotNode plot_n = (PlotNode) nf;
			Plot p = plot_n.getOwner();
			NodeFunction nf_dep = p.getDependency();
			seen_functions.add(nf_dep);
			pn.addParent(explain(nf_dep, seen_functions, depth - 1));
			return pn;
		}
		if (nf instanceof TableCellNode)
		{
			// Table cell; does it depend on something else?
			ProvenanceNode pn = new ProvenanceNode(nf);
			TableCellNode tcn = (TableCellNode) nf;
			Table t = tcn.getOwner();
			NodeFunction nf_dep = t.getDependency(tcn.getRow(), tcn.getCol());
			/*if (seen_functions.contains(nf_dep))
			{
				// Infinite loop
				return InfiniteLoop.instance;
			}*/
			seen_functions.add(nf_dep);
			pn.addParent(explain(nf_dep, seen_functions, depth - 1));
			return pn;
		}
		if (nf instanceof AggregateFunction)
		{
			ProvenanceNode pn = new ProvenanceNode(nf);
			AggregateFunction af = (AggregateFunction) nf;
			List<NodeFunction> dependencies = af.m_nodes;
			List<ProvenanceNode> parents = new LinkedList<ProvenanceNode>();
			for (NodeFunction par_nf : dependencies)
			{
				/*if (seen_functions.contains(par_nf))
				{
					// Infinite loop
					return InfiniteLoop.instance;
				}*/
				seen_functions.add(par_nf);
				ProvenanceNode par_pn = explain(par_nf);
				parents.add(par_pn);
			}
			pn.addParents(parents);
			return pn;
		}
		return BrokenChain.instance;
	}
}
