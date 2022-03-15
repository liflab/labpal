/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2022 Sylvain Hallé

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
package ca.uqac.lif.labpal.macro;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.assistant.ExperimentSelector;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;

/**
 * Macro producing values based on a set of experiments from a lab.
 * @author Sylvain Hallé
 * @since 3.0
 */
public class ExperimentMacro extends Macro implements Dependent<Experiment>
{
	protected transient Set<Experiment> m_experiments;
	
	public ExperimentMacro(Laboratory lab, String ... names)
	{
		super(lab, names);
	}
	
	/**
	 * Adds experiments to the macro.
	 * @param experiments The experiments
	 * @return This macro
	 */
	/*@ non_null @*/ public ExperimentMacro add(Experiment ... experiments)
	{
		m_experiments = new HashSet<Experiment>(experiments.length);
		for (Experiment e : experiments)
		{
			m_experiments.add(e);
		}
		return this;
	}
	
	/**
	 * Adds experiments to the macro.
	 * @param experiments The experiments
	 * @return This macro
	 */
	/*@ non_null @*/ public ExperimentMacro add(Collection<Experiment> experiments)
	{
		m_experiments = new HashSet<Experiment>(experiments.size());
		m_experiments.addAll(experiments);
		return this;
	}
	
	/**
	 * Adds experiments to the macro.
	 * @param selector A selector picking experiments from a lab
	 * @return This macro
	 */
	/*@ non_null @*/ public ExperimentMacro add(ExperimentSelector selector)
	{
		m_experiments = new HashSet<Experiment>();
		m_experiments.addAll(selector.select());
		return this;
	}
	
	/**
	 * Adds experiments to the macro.
	 * @param r A region describing the experiments to add
	 * @param f A factory to obtain experiment instances
	 * @return This macro
	 */
	/*@ non_null @*/ public ExperimentMacro add(Region r, ExperimentFactory<Experiment> f)
	{
		m_experiments = new HashSet<Experiment>();
		for (Point p : r.allPoints())
		{
			Experiment e = f.get(p);
			if (e != null)
			{
				m_experiments.add(e);
			}
		}
		return this;
	}
	
	/**
	 * Populates the map of all the values computed for each named
	 * data point in this macro
	 * @param map A map, pre-filled with all the defined keys, each
	 * temporarily associated to the null value
	 */
	@Override
	public final void computeValues(Map<String,Object> map)
	{
		computeValues(m_experiments, map);
	}
	
	/**
	 * Populates the map of all the values computed for each named
	 * data point in this macro. This method should be overridden to calculate
	 * the appropriate values.
	 * @param experiments The set of experiments associated to this macro
	 * @param map A map, pre-filled with all the defined keys, each
	 * temporarily associated to the null value
	 */
	public void computeValues(Set<Experiment> experiments, Map<String,Object> map)
	{
		// Do nothing
	}
	
	/**
	 * Gets the parts of each experiment that are actually accessed by the
	 * macro to compute its values. This method can be overridden to provide a
	 * more fine-grained explanation of the macro's output than the default
	 * (which links to experiments as a whole).
	 * @return An array of parts. Defaults to a singleton containing the part
	 * "all".
	 */
	protected Part[] getExperimentParts()
	{
		return new Part[] {Part.all};
	}

	@Override
	public Status getStatus() 
	{
		return Stateful.getLowestStatus(m_experiments);
	}
	
	@Override
	public PartNode getExplanation(Part d, NodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		LabelledNode to_add = root;
		Part[] parts = getExperimentParts();
		if (m_experiments.size() > 1 || parts.length > 1)
		{
			AndNode and = f.getAndNode();
			root.addChild(and);
			to_add = and;
		}
		for (Experiment e : m_experiments)
		{
			for (Part p : parts)
			{
				to_add.addChild(f.getPartNode(p, e));
			}
		}
		return root;
	}
	
	@Override
	public float getProgression()
	{
		float prog = 0;
		for (Experiment e : m_experiments)
		{
			prog += e.getProgression();
		}
		return prog / (float) m_experiments.size();
	}

	@Override
	public void reset() 
	{
		for (Experiment e : m_experiments)
		{
			e.reset();
		}
	}

	@Override
	public Collection<Experiment> dependsOn() 
	{
		return m_experiments;
	}

	@Override
	public String getNickname()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
