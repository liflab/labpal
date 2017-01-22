/*
  LabPal, a versatile environment for running experiments on a computer
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
package ca.uqac.lif.labpal.table;

import ca.uqac.lif.labpal.provenance.ProvenanceNode;

/**
 * Table obtained from applying a transformation to other tables
 * @author Sylvain Hallé
 */
public class TransformedTable extends Table 
{
	/**
	 * The tables used as the input for the transformation
	 */
	protected final Table[] m_inputTables;
	
	/**
	 * The table transformation to apply
	 */
	protected final TableTransformation m_transformation;
	
	public TransformedTable(TableTransformation trans, Table ... tables)
	{
		super();
		m_transformation = trans;
		m_inputTables = tables;
	}
	
	@Override
	protected DataTable getDataTable(boolean link_to_experiments, String... ordering) 
	{
		DataTable[] concrete_tables = new DataTable[m_inputTables.length];
		for (int i = 0; i < m_inputTables.length; i++)
		{
			concrete_tables[i] = m_inputTables[i].getDataTable(link_to_experiments);
		}
		return m_transformation.transform(concrete_tables);
	}

	@Override
	protected DataTable getDataTable(boolean link_to_experiments) 
	{
		DataTable[] concrete_tables = new DataTable[m_inputTables.length];
		for (int i = 0; i < m_inputTables.length; i++)
		{
			concrete_tables[i] = m_inputTables[i].getDataTable(link_to_experiments);
		}
		return m_transformation.transform(concrete_tables);
	}
	
	@Override
	public ProvenanceNode dependsOn(Table owner, int row, int col)
	{
		return getDataTable(false).dependsOn(this, row, col);
	}

}
