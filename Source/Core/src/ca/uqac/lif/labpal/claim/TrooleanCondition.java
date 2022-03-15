package ca.uqac.lif.labpal.claim;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.dag.Node;
import ca.uqac.lif.labpal.provenance.LeafFetcher;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthOutput;

/**
 * Lifts a Boolean condition <i>f</i> into a Troolean condition. The function
 * returns <em>inconclusive</em> whenever one of its inputs is null. Otherwise,
 * it evaluates <i>f</i> on the inputs, and converts its true/false Boolean
 * output into the corresponding Troolean value. 
 * @author Sylvain Hallé
 */
public class TrooleanCondition extends AtomicFunction
{
	/*@ non_null @*/ protected final Function m_function;
	
	/*@ non_null @*/ protected final List<Integer> m_nullInputs;
	
	/**
	 * Creates a new Troolean condition out of a Boolean condition.
	 * @param f A n:1 function that produces a Boolean value.
	 */
	public TrooleanCondition(/*@ non_null @*/ Function f)
	{
		super(f.getInputArity(), 1);
		m_function = f;
		m_nullInputs = new ArrayList<Integer>(f.getInputArity());
	}

	@Override
	public TrooleanCondition duplicate(boolean with_state)
	{
		TrooleanCondition tc = new TrooleanCondition(m_function.duplicate(with_state));
		if (with_state)
		{
			tc.m_nullInputs.addAll(m_nullInputs);
		}
		return tc;
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		m_nullInputs.clear();
		for (int i = 0; i < inputs.length; i++)
		{
			if (inputs[i] == null)
			{
				m_nullInputs.add(i);
			}
		}
		Troolean.Value v = Troolean.Value.INCONCLUSIVE;
		if (m_nullInputs.isEmpty())
		{
			Boolean b = (Boolean) m_function.evaluate(inputs)[0];
			if (Boolean.TRUE.equals(b))
			{
				v = Troolean.Value.TRUE;
			}
			else if (Boolean.FALSE.equals(b))
			{
				v = Troolean.Value.FALSE;
			}
		}
		return new Object[] {v};
	}
	
	@Override
	public void reset()
	{
		m_function.reset();
		m_nullInputs.clear();
	}
	
	@Override
	public PartNode getExplanation(Part p, NodeFactory f)
	{
		PartNode root = f.getPartNode(p, this);
		if (m_nullInputs.isEmpty())
		{
			NodeFactory sub_factory = f.getFactory(p, m_function);
			PartNode sub_root = ((ExplanationQueryable) m_function).getExplanation(p, sub_factory);
			root.addChild(sub_root);
			LeafFetcher lf = new LeafFetcher(sub_root);
			lf.crawl();
			for (Node leaf : lf.getLeaves())
			{
				if (!(leaf instanceof PartNode))
				{
					continue;
				}
				PartNode pn_leaf = (PartNode) leaf;
				Part pnp_leaf = pn_leaf.getPart();
				if (pn_leaf.getSubject() != m_function)
				{
					continue;
				}
				pn_leaf.addChild(f.getPartNode(pnp_leaf, this));
			}
		}
		else
		{
			LabelledNode to_add = root;
			if (m_nullInputs.size() > 1)
			{
				OrNode or = f.getOrNode();
				to_add.addChild(or);
				to_add = or;
			}
			for (int i : m_nullInputs)
			{
				Part new_p = NthOutput.replaceOutByIn(p, i);
				to_add.addChild(f.getPartNode(new_p, this));
			}
		}
		return root;
	}
	
	@Override
	public String toString()
	{
		return "T" + m_function.toString();
	}
}
