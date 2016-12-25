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
package ca.uqac.lif.labpal.table;

import ca.uqac.lif.labpal.NumberHelper;

/**
 * Replaces the content of each entry by its fraction of the
 * sum of all values for the column
 */
public class NormalizeColumns extends TableTransform 
{
	public NormalizeColumns(Table t)
	{
		super(t);
	}

	@Override
	public ConcreteTable getConcreteTable()
	{
		ConcreteTable in_table = m_inputTable.getConcreteTable();
		ConcreteTable out_table = new ConcreteTable(in_table.m_columnHeaders, in_table.m_lineHeaders);
		if (in_table.m_values.length == 0)
		{
			return out_table;
		}
		for (int j = 0; j < in_table.m_values[0].length; j++)
		{
			float total = 0;
			for (int i = 0; i < in_table.m_values.length; i++)
			{
				if (NumberHelper.isNumeric(in_table.m_values[i][j]))
				{
					total += Float.parseFloat(in_table.m_values[i][j]);
				}
			}
			for (int i = 0; i < in_table.m_values.length; i++)
			{
				if (NumberHelper.isNumeric(in_table.m_values[i][j]) && total != 0)
				{
					out_table.m_values[i][j] = Float.toString(Float.parseFloat(in_table.m_values[i][j]) / total);
				}
				else
				{
					out_table.m_values[i][j] = "?";
				}
				
			}
		}
		return out_table;
	}
}
