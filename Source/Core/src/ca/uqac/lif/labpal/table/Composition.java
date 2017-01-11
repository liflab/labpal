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

/**
 * A table transformation made by chaining multiple transformations
 * one after the other.
 * @author Sylvain Hallé
 */
public class Composition implements TableTransformation
{
	/**
	 * The list of transformations to perform
	 */
	protected final TableTransformation[] m_transformations;
	
	/**
	 * Creates a new composition
	 * @param transformations The list of table transformations to apply
	 */
	public Composition(TableTransformation ... transformations)
	{
		super();
		m_transformations = transformations;
	}

	@Override
	public DataTable transform(DataTable ... tables)
	{
		DataTable t = tables[0];
		for (TableTransformation trans : m_transformations)
		{
			t = trans.transform(t);
		}
		return t;
	}

}
