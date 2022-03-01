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

import java.util.Collection;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;

/**
 * Selects the experiments necessary to render a plot.
 * @author Sylvain Hallé
 *
 */
public class PlotExperimentSelector extends ConcreteExperimentSelector 
{
	/**
	 * The plot relative to which the experiments are selected.
	 */
	/*@ non_null @*/ protected final Plot m_plot;
	
	/**
	 * Creates a new instance of the selector.
	 * @param lab The lab from which to select experiments
	 * @param p The plot relative to which the experiments are selected
	 */
	public PlotExperimentSelector(/*@ non_null @*/ Laboratory lab, /*@ non_null @*/ Plot p)
	{
		super(lab);
		m_plot = p;
	}

	@Override
	/*@ non_null @*/ public Collection<Experiment> select()
	{
		return m_plot.dependsOn().get(0).dependsOn();
	}
}
