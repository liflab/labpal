package ca.uqac.lif.labpal.provenance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.labpal.GraphvizRenderer;
import ca.uqac.lif.labpal.server.ExplainCallback;
import ca.uqac.lif.labpal.table.TableCellProvenanceNode;

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
		Map<String,Map<String,Set<String>>> highlight_groups = new HashMap<String,Map<String,Set<String>>>();
		ExplainCallback.getHighlightGroups(node, highlight_groups);
		StringBuilder out = new StringBuilder();
		out.append("digraph {\n");
		out.append("node [shape=\"rect\"];\n");
		toDot(node, "", -1, out, highlight_groups);
		out.append("}\n");
		return out.toString();
	}
	
	protected void toDot(ProvenanceNode node, String parent_id, int parent, StringBuilder out, Map<String,Map<String,Set<String>>> highlight_groups)
	{
		int id = m_nodeCounter++;
		String style = styleNode(node);
		String url = ExplainCallback.getDataPointUrl(node, parent_id, highlight_groups);
		out.append(id).append(" [label=\"").append(escape(node.toString())).append("\",href=\"").append(url).append("\"");
		if (!style.isEmpty())
		{
			out.append(",").append(style);
		}
		out.append("];\n");
		if (parent >= 0)
		{
			out.append(parent).append(" -> ").append(id).append(";\n");
		}
		for (ProvenanceNode pn : node.getParents())
		{
			String p_id = node.getDataPointId();
			toDot(pn, p_id, id, out, highlight_groups);
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
		if (n instanceof TableCellProvenanceNode)
		{
			style = "style=filled,fillcolor=blue";
		}
		return style;
	}
}
