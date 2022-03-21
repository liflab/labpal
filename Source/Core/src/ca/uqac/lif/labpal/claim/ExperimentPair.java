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

import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.vector.ParameterizedVectorFunction;

/**
 * An object containing a reference to two experiments. It is useful in claims
 * that involve a comparison between experiments.
 * @author Sylvain Hallé
 * @since 3.0
 */
public class ExperimentPair
{
	/**
	 * The first experiment of the pair.
	 */
	/*@ non_null @*/ protected final Experiment m_first;
	
	/**
	 * The second experiment of the pair.
	 */
	/*@ non_null @*/ protected final Experiment m_second;
	
	/**
	 * Creates a new experiment pair.
	 * @param first The first experiment of the pair
	 * @param second The second experiment of the pair
	 */
	public ExperimentPair(/*@ non_null @*/ Experiment first, /*@ non_null @*/ Experiment second)
	{
		super();
		m_first = first;
		m_second = second;
	}
	
	@Override
	public int hashCode()
	{
		return m_first.hashCode() + m_second.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof ExperimentPair))
		{
			return false;
		}
		ExperimentPair ep = (ExperimentPair) o;
		return ep.m_first.equals(m_first) && ep.m_second.equals(m_second);
	}
	
	@Override
	public String toString()
	{
		return "(" + m_first.toString() + "," + m_second.toString() + ")";
	}
	
	/**
	 * Function extracting the first element of an experiment pair.
	 */
	public static class First extends AtomicFunction
	{
		/**
		 * Creates a new instance of the function.
		 */
		public First()
		{
			super(1, 1);
		}

		@Override
		public First duplicate(boolean with_state)
		{
			return new First();
		}

		@Override
		protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
		{
			if (!(inputs[0] instanceof ExperimentPair))
			{
				throw new InvalidArgumentException("Expected an experiment pair");
			}
			ExperimentPair ep = (ExperimentPair) inputs[0];
			return new Object[] {ep.m_first};
		}
		
		@Override
		public PartNode getExplanation(Part p, NodeFactory f)
		{
			PartNode root = f.getPartNode(p, this);
			Part new_p = NthOutput.replaceOutByIn(p, 0);
			root.addChild(f.getPartNode(ParameterizedVectorFunction.replaceInputByElement(new_p, 0, 0), this));
			return root;
		}
		
		@Override
		public String toString()
		{
			return "1st";
		}
	}
	
	/**
	 * Function extracting the first element of an experiment pair.
	 */
	public static class Second extends AtomicFunction
	{
		/**
		 * Creates a new instance of the function.
		 */
		public Second()
		{
			super(1, 1);
		}

		@Override
		public Second duplicate(boolean with_state)
		{
			return new Second();
		}

		@Override
		protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
		{
			if (!(inputs[0] instanceof ExperimentPair))
			{
				throw new InvalidArgumentException("Expected an experiment pair");
			}
			ExperimentPair ep = (ExperimentPair) inputs[0];
			return new Object[] {ep.m_second};
		}
		
		@Override
		public PartNode getExplanation(Part p, NodeFactory f)
		{
			PartNode root = f.getPartNode(p, this);
			Part new_p = NthOutput.replaceOutByIn(p, 0);
			root.addChild(f.getPartNode(ParameterizedVectorFunction.replaceInputByElement(new_p, 0, 1), this));
			return root;
		}
		
		@Override
		public String toString()
		{
			return "2nd";
		}
	}
}
