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

import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.labpal.Formatter;
import ca.uqac.lif.labpal.provenance.AggregateFunction;
import ca.uqac.lif.labpal.provenance.NodeFunction;

/**
 * Computes the sum of each column
 * @author Sylvain Hallé
 */
public class ColumnSum implements TableTransformation
{
	/**
	 * A single instance of this table transformation
	 */
	public static final ColumnSum instance = new ColumnSum();
	
	ColumnSum()
	{
		super();
	}
	
	/**
	 * Gets an instance of this table transformation
	 * @return An instance
	 */
	public static ColumnSum get()
	{
		return instance;
	}
	
	@Override
	public TempTable transform(TempTable... tables) 
	{
		TempTable table = tables[0];
		String[] col_names = table.getColumnNames();
		float[] col_sum = new float[col_names.length];
		TempTable out_table = new TempTable(table.m_id, col_names);
		int row = 0;
		@SuppressWarnings("unchecked")
		Set<NodeFunction>[] deps = new Set[col_names.length];
		for (int col = 0; col < col_names.length; col++)
		{
			deps[col] = new HashSet<NodeFunction>();
		}
		for (TableEntry te : table.getEntries())
		{
			for (int col = 0; col < col_names.length; col++)
			{
				Object o = te.get(col_names[col]);
				if (o == null)
					continue;
				JsonElement je = Formatter.jsonCast(o);
				if (je instanceof JsonNumber)
				{
					float val = ((JsonNumber) je).numberValue().floatValue();
					col_sum[col] += val;
					deps[col].add(new TableCellNode(table, row, col));
				}
			}
			row++;
		}
		TableEntry te = new TableEntry();
		for (int col = 0; col < col_names.length; col++)
		{
			te.put(col_names[col], new JsonNumber(col_sum[col]));
			AggregateFunction af = new AggregateFunction("The sum of column " + col + " in Table #" + table.m_id, deps[col]);
			te.addDependency(col_names[col], af);
		}
		out_table.add(te);
		return out_table;
	}

}
