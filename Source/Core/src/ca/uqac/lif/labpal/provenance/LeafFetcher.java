package ca.uqac.lif.labpal.provenance;

import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.dag.LeafCrawler;
import ca.uqac.lif.dag.Node;

public class LeafFetcher extends LeafCrawler
{
	/*@ non_null @*/protected Set<Node> m_leaves;
	
	public LeafFetcher(/*@ non_null @*/ Node start) 
	{
		super(start);
		m_leaves = new HashSet<Node>();
	}

	@Override
	protected void visitLeaf(Node n)
	{
		m_leaves.add(n);
	}
	
	/*@ pure non_null @*/ public Set<Node> getLeaves()
	{
		return m_leaves;
	}
}
