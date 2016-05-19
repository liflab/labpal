/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé

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
package ca.uqac.lif.parkbench.table;

import java.util.Vector;

/**
 * Takes an input table and applies a transformation to produce a new table.
 */
public abstract class TableTransform extends Table
{
	protected Table m_inputTable;
	
	public TableTransform(Table t)
	{
		super();
		m_inputTable = t;
	}
	
	@Override
	public Vector<String> getXValues()
	{
		return getConcreteTable().getXValues();
	}
}

