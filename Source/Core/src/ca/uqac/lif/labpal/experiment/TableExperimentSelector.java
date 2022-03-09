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
import ca.uqac.lif.labpal.table.Table;

/**
 * Selects the experiments necessary to render a table.
 * @author Sylvain Hallé
 *
 */
public class TableExperimentSelector extends ConcreteExperimentSelector 
{
	/**
	 * The table relative to which the experiments are selected.
	 */
	/*@ non_null @*/ protected final Table m_table;
	
	/**
	 * Creates a new instance of the selector.
	 * @param lab The lab from which to select experiments
	 * @param t The table relative to which the experiments are selected
	 */
	public TableExperimentSelector(/*@ non_null @*/ Laboratory lab, /*@ non_null @*/ Table t)
	{
		super(lab);
		m_table = t;
	}

	@Override
	/*@ non_null @*/ public Collection<Experiment> select()
	{
		return m_table.dependsOn();
	}
}