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
import java.util.List;

import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentValue;
import ca.uqac.lif.labpal.provenance.StatusOf;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;

/**
 * A function that fetches the value of a parameter in an experiment. The
 * function is particular in that, for an output parameter, it returns
 * <tt>null</tt> if the experiment is in any state except <em>done</em>. This
 * is to indicate that the value pointed to should not be trusted.
 * @author Sylvain Hallé
 */
public class ValueOf extends AtomicFunction
{
	/**
	 * The name of the parameter to fetch in an experiment.
	 */
	/*@ non_null @*/ protected final String m_parameter;

	/**
	 * The ExperimentValue part used in explanations of this function.
	 */
	/*@ non_null @*/ protected final ExperimentValue m_valuePart;

	/**
	 * A flag indicating whether the experiment was in the <em>done</em> state
	 * when the function was called. The explanation of the output depends on
	 * this information.
	 */
	protected boolean m_wasDefined;

	/**
	 * Creates a new instance of the function.
	 * @param parameter The name of the parameter to fetch in an experiment
	 */
	public ValueOf(String parameter)
	{
		super(1, 1);
		m_parameter = parameter;
		m_valuePart = new ExperimentValue(parameter);
		m_wasDefined = false;
	}

	@Override
	public AtomicFunction duplicate(boolean with_state)
	{
		return new ValueOf(m_parameter);
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Experiment))
		{
			throw new InvalidArgumentException("Argument should be an experiment");
		}
		Object[] out = new Object[1];
		Experiment e = (Experiment) inputs[0];
		if (/*e.isInput(m_parameter) ||*/ e.getStatus() == Status.DONE)
		{
			out[0] = e.read(m_parameter);
			m_wasDefined = true;
		}
		else
		{
			out[0] = null;
			m_wasDefined = false;			
		}
		return out;
	}

	@Override
	public PartNode getExplanation(Part p, NodeFactory f)
	{
		PartNode root = f.getPartNode(p, this);
		int out_nb = NthOutput.mentionedOutput(p);
		if (out_nb != 0)
		{
			root.addChild(f.getUnknownNode());
		}
		else
		{
			if (m_wasDefined)
			{
				Part new_p = ComposedPart.compose(m_valuePart, NthOutput.replaceOutByIn(p, 0));
				root.addChild(f.getPartNode(new_p, this));
			}
			else
			{
				// If the experiment was not in the DONE state, the function returned
				// null and the explanation is the experiment's status
				root.addChild(f.getPartNode(ComposedPart.compose(StatusOf.instance, NthInput.FIRST), this));
			}
		}
		return root;
	}

	@Override
	public void reset()
	{
		super.reset();
		m_wasDefined = false;
	}

	protected Part deleteOut(Part p)
	{
		if (p instanceof ComposedPart)
		{
			ComposedPart cd = (ComposedPart) p;
			List<Part> desigs = new ArrayList<>();
			boolean replaced = false;
			for (int i = 0 ; i < cd.size(); i++)
			{
				Part in_d = cd.get(i);
				if (in_d instanceof NthOutput && !replaced)
				{
					replaced = true;
				}
				else
				{
					desigs.add(in_d);
				}
			}
			return ComposedPart.compose(desigs);
		}
		else
		{
			return m_valuePart;
		}
	}
	
	@Override
	public String toString()
	{
		return m_parameter;
	}

}
