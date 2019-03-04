/*
  LabPal, a versatile benchmark environment
  Copyright (C) 2015-2017 Sylvain Hallé

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

import ca.uqac.lif.labpal.GraphvizRenderer;
import ca.uqac.lif.labpal.server.ExplainCallback;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.mtnp.table.TableCellNode;
import ca.uqac.lif.mtnp.table.TableNode;
import ca.uqac.lif.petitpoucet.AggregateFunction;
import ca.uqac.lif.petitpoucet.BrokenChain;
import ca.uqac.lif.petitpoucet.InfiniteLoop;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.ProvenanceNode;

/**
 * Renders a provenance node into a picture
 *  
 * @author Sylvain Hallé
 */
public class DotProvenanceTreeRenderer 
{
	protected int m_nodeCounter;
	
	protected GraphvizRenderer m_renderer;
	
	public DotProvenanceTreeRenderer()
	{
		super();
		m_nodeCounter = 0;
		m_renderer = new GraphvizRenderer();
	}
	
	public String toDot(ProvenanceNode node)
	{
		StringBuilder out = new StringBuilder();
		out.append("digraph {\n");
		out.append("node [shape=\"rect\"];\n");
		toDot(node, "", -1, out);
		out.append("}\n");
		return out.toString();
	}
	
	protected void toDot(ProvenanceNode node, String parent_id, int parent, StringBuilder out)
	{
		int id = m_nodeCounter++;
		String style = styleNode(node);
		String url = WebCallback.htmlEscape(ExplainCallback.getDataPointUrl(node));
		out.append(id).append(" [label=\"").append(escape(node.toString())).append("\",href=\"").append(url).append("\"");
		if (!style.isEmpty())
		{
			out.append(",").append(style);
		}
		out.append("];\n");
		if (parent >= 0)
		{
			out.append(id).append(" -> ").append(parent).append(";\n");
		}
		for (ProvenanceNode pn : node.getParents())
		{
			String p_id = node.getNodeFunction().getDataPointId();
			toDot(pn, p_id, id, out);
		}
	}
	
	public byte[] toImage(ProvenanceNode node, String filetype)
	{
		String instructions = toDot(node);
		return m_renderer.dotToImage(instructions, filetype);
	}
	
	protected static String escape(String s)
	{
		s = s.replaceAll("\\[", "(");
		s = s.replaceAll("\\]", ")");
		s = s.replaceAll("\"", "'");
		return s;
	}
	
	public String styleNode(ProvenanceNode n)
	{
		String style = "";
		NodeFunction nf = n.getNodeFunction();
		if (nf instanceof TableCellNode)
		{
			style = "style=filled,fillcolor=cornflowerblue";
		}
		else if (nf instanceof TableNode)
		{
			style = "style=filled,fillcolor=deepskyblue2";
		}
		else if (nf instanceof AggregateFunction)
		{
			style = "style=filled,fillcolor=deeppink";
		}
		else if (nf instanceof ExperimentValue)
		{
			style = "style=filled,fillcolor=darkgoldenrod1";
		}
		else if (n instanceof BrokenChain)
		{
			style = "style=filled,fillcolor=crimson";
		}
		else if (n instanceof InfiniteLoop)
		{
			style = "style=filled,fillcolor=crimson";
		}
		return style;
	}
}
