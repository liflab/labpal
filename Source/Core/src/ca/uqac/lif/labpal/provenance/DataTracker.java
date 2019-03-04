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
import ca.uqac.lif.labpal.macro.Macro;
import ca.uqac.lif.labpal.macro.MacroNode;
import ca.uqac.lif.mtnp.plot.Plot;
import ca.uqac.lif.mtnp.plot.PlotNode;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TableCellNode;
import ca.uqac.lif.mtnp.table.TableFunctionNode;
import ca.uqac.lif.mtnp.table.TableNode;
import ca.uqac.lif.petitpoucet.AggregateFunction;
import ca.uqac.lif.petitpoucet.BrokenChain;
import ca.uqac.lif.petitpoucet.InfiniteLoop;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.OwnershipManager;
import ca.uqac.lif.petitpoucet.ProvenanceNode;

/**
 * Tracks the ownership of each data point produced by a lab.
 * This is the class taking care of the LabPal Datapoint Identifiers
 * (LDI).
 * @author Sylvain Hallé
 */
public class DataTracker
{
	/**
	 * The lab
	 */
  /*@ non_null @*/ protected OwnershipManager m_lab;
  
  /**
   * The maximum depth of a provenance tree
   */
  protected static final transient int s_maxDepth = 10;

	/**
	 * Creates a new data tracker
	 * @param lab The lab this tracker is associated with
	 */
	public DataTracker(/*@ non_null @*/ OwnershipManager lab)
	{
		super();
		m_lab = lab;
	}

	/**
	 * Gets the owner of a data point
	 * @param id The data point ID
	 * @return The owner; maybe <tt>null</tt> if no owner can be found
	 */
	/*@ pure null @*/ public Object getOwner(String id)
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
		// Is it a macro?
		Macro m = MacroNode.getOwner(m_lab, id);
		if (m != null)
		{
			return m;
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
		if (owner instanceof Macro)
		{
			return MacroNode.dependsOn((Macro) owner, datapoint_id);
		}
		return null;
	}

	/**
	 * Produces a provenance tree for a given LDI
	 * @param datapoint_id The LDI
	 * @return A reference to the root of the provenance tree
	 */
	/*@ non_null @*/ public ProvenanceNode explain(String datapoint_id)
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
	 * @param nf A node function
	 * @return The root of the provenance tree
	 */
	public ProvenanceNode explain(NodeFunction nf)
	{
		Set<NodeFunction> functions = new HashSet<NodeFunction>();
		return explain(nf, functions, s_maxDepth);
	}

	/**
	 * Builds a provenance tree for a given data point 
	 * @param nf The ID of the current node
	 * @param seen_functions A set of nodes functions already included in the
	 *   tree. This is to avoid infinite looping due to possible circular
	 *   dependencies.
	 * @param depth A counter that is being decreased every time the method is
	 * called recursively. This can be used to specify the maximum depth of
	 * the provenance tree.
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
		if (nf instanceof MacroNode)
		{
			ProvenanceNode pn = new ProvenanceNode(nf);
			MacroNode plot_n = (MacroNode) nf;
			Macro m = plot_n.getOwner();
			NodeFunction nf_dep = m.getDependency();
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
			List<NodeFunction> dependencies = af.getDependencyNodes();
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
