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
package ca.uqac.lif.labpal.experiment;

import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.Function;

/**
 * Exception that is thrown based on the evaluation of a
 * <a href="https://github.com/liflab/petitpoucet">PetitPoucet</a>
 * {@link Function}.
 * The function assertion has the special characteristic that it can throw
 * itself on a call to {@link #evaluate(Object...) evaluate()}.
 * 
 * @author Sylvain Hallé
 * @since 3.0
 */
public class FunctionAssertion extends ExperimentException implements ExplanationQueryable
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The function whose evaluation may trigger the throwing of the
	 * exception.
	 */
	/*@ non_null @*/ protected final Function m_function;
	
	/**
	 * Creates a new instance of the assertion.
	 * @param message The message to be displayed if the exception is thrown.
	 * It should describe textually the condition whose violation triggers
	 * the throwing of the exception.
	 * @param f The function to evaluate. It should return a Boolean value.
	 */
	public FunctionAssertion(/*@ non_null @*/ String message, /*@ non_null @*/ Function f)
	{
		super(message);
		m_function = f;
	}
	
	/**
	 * Evaluates the function associated to this exception. The method returns
	 * without doing anything if the function produces <tt>true</tt> as its
	 * output value. In any other case, the method throws the exception on which
	 * it was called.
	 * @param arguments The arguments that should be passed to the function
	 * @throws FunctionAssertion
	 */
	public void evaluate(Object ... arguments) throws FunctionAssertion
	{
		Object[] out = m_function.evaluate(arguments);
		if (!Boolean.TRUE.equals(out[0]))
		{
			throw this;
		}
	}
	
	/**
	 * Resets the internal state of the function associated to this exception.
	 */
	public void reset()
	{
		m_function.reset();
	}

	@Override
	public PartNode getExplanation(Part part)
	{
		return getExplanation(part, RelationNodeFactory.getFactory());
	}

	@Override
	public PartNode getExplanation(Part part, RelationNodeFactory factory)
	{
		PartNode root = factory.getPartNode(part, this);
		if (!(m_function instanceof ExplanationQueryable))
		{
			root.addChild(factory.getUnknownNode());
			return root;
		}
		PartNode sub_root = ((ExplanationQueryable) m_function).getExplanation(part, factory);
		root.addChild(sub_root);
		return root;	
	}
}
