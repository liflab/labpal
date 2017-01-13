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

import java.util.Map.Entry;

import ca.uqac.lif.json.JsonNumber;

/**
 * Replaces the content of each entry by its fraction of the
 * sum of all values for the row
 */
public class NormalizeRows implements TableTransformation 
{
	public NormalizeRows()
	{
		super();
	}

	@Override
	public DataTable transform(DataTable ... tables)
	{
		DataTable in_table = tables[0];
		DataTable out_table = new DataTable();
		if (in_table.getRowCount() == 0)
		{
			return out_table;
		}
		for (TableEntry te : in_table.getEntries())
		{
			float total = 0;
			for (Object o : te.values())
			{
				if (o instanceof Number)
				{
					total += ((Number) o).floatValue();
				}
				if (o instanceof JsonNumber)
				{
					total += ((JsonNumber) o).numberValue().floatValue();
				}
			}
			TableEntry new_entry = new TableEntry();
			for (Entry<String,Object> map_entry : te.entrySet())
			{
				String key = map_entry.getKey();
				Object o = map_entry.getValue();
				if (o instanceof Number)
				{
					new_entry.put(key, ((Number) o).floatValue() / total);
				}
				else if (o instanceof JsonNumber)
				{
					new_entry.put(key, ((JsonNumber) o).numberValue().floatValue() / total);
				}
				else
				{
					new_entry.put(key, o);
				}
			}
			out_table.add(new_entry);
		}
		return out_table;
	}
}
