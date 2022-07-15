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
package ca.uqac.lif.labpal.provenance;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.dag.Node;
import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.dag.Pin;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.petitpoucet.GraphUtilities;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;

/**
 * Utility methods to manipulate and simplify lineage graphs for function
 * circuits.
 * @author Sylvain Hallé
 *
 */
public class LabPalLineageGraphUtilities
{
	/**
	 * Private constructor.
	 */
	private LabPalLineageGraphUtilities()
	{
		super();
	}

	/**
	 * Simplifies a lineage graph. In addition to the simplifications performed
	 * by {@link GraphUtilities#simplify(Node)}, this method also deletes from
	 * the graph all the leaves that do not refer to either an input or an output
	 * of a function (i.e. constants).
	 * @param graph The graph to simplify
	 * @return The simplified graph
	 */
	public static Node simplify(Node graph)
	{
		deleteConstantLeaves(graph);
		Node new_graph = GraphUtilities.simplify(graph);
		deleteConstantLeaves(new_graph);
		Node n_new_graph = GraphUtilities.simplify(new_graph);
		return n_new_graph;
	}

	/**
	 * Simplifies a lineage graph. In addition to the simplifications performed
	 * by {@link GraphUtilities#simplify(Node)}, this method also deletes from
	 * the graph all the leaves that do not refer to either an input or an output
	 * of a function (i.e. constants).
	 * @param roots The roots of the graph to simplify
	 * @return The roots of the simplified graph
	 */
	public static List<Node> simplify(List<Node> roots)
	{
		List<Node> out = GraphUtilities.simplify(roots);
		for (Node root : roots)
		{
			deleteConstantLeaves(root);
		}
		// We run another pass of simplify, since deleting leaves
		// may make some nodes redundant
		return GraphUtilities.simplify(out);
	}

	/**
	 * Cuts a node from its parents if it is a leaf corresponding to a constant.
	 * @param n The current node
	 */
	protected static void deleteConstantLeaves(Node n)
	{
		if (GraphUtilities.isLeaf(n) && n instanceof PartNode)
		{
			PartNode pn = (PartNode) n;
			Part p = pn.getPart();
			if (!(pn.getSubject() instanceof Experiment) && NthOutput.mentionedOutput(p) < 0 && NthInput.mentionedInput(p) < 0)
			{
				// No part mentioned in this node: hide it
				for (int i = 0; i < n.getInputArity(); i++)
				{
					for (Pin<? extends Node> pin : n.getInputLinks(i))
					{
						Node up_node = pin.getNode();
						NodeConnector.cutFrom(up_node, pin.getIndex(), n, i);
					}
				}
			}
		}
		for (int i = 0; i < n.getOutputArity(); i++)
		{
			// We must first put the pins into a separate list, as recursive calls
			// may delete some of them from the original collection
			List<Pin<? extends Node>> pins = new ArrayList<>(n.getOutputLinks(i).size());
			pins.addAll(n.getOutputLinks(i));
			for (Pin<? extends Node> pin : pins)
			{
				deleteConstantLeaves(pin.getNode());
			}
		}
	}
}